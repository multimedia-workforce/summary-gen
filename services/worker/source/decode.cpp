//
//  MIT License
//
//  Copyright (c) 2025 multimedia-workforce
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in all
//  copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//  SOFTWARE.

#include "decode.h"

#include <cstring>
#include <fstream>
#include <vector>

// FFMPEG headers for decoding, format parsing, and resampling
extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/opt.h>
#include <libswresample/swresample.h>
}

// Constants for resampling and I/O buffering
enum {
    WAVE_SAMPLE_RATE = 16000,// Target sample rate for output
    AVIO_BUFFER_SIZE = 0x1000// Custom I/O buffer size (4096 bytes)
};

namespace {

// Helper class that wraps custom I/O from a std::vector<u8> into an AVIOContext
class IOContext {
public:
    explicit IOContext(std::vector<uint8_t> input)
        : m_input(std::move(input)),
          m_position(0),
          m_buffer(static_cast<uint8_t *>(av_malloc(AVIO_BUFFER_SIZE))),
          m_context(nullptr) {
        if (not m_buffer) {
            throw std::bad_alloc();// Allocation failed for AVIO buffer
        }

        // Allocate AVIO context with our read and seek callbacks
        m_context =
                avio_alloc_context(m_buffer, AVIO_BUFFER_SIZE, 0, this, &IOContext::read, nullptr, &IOContext::seek);

        if (not m_context) {
            av_free(m_buffer);// Cleanup on failure
        }
    }

    ~IOContext() {
        if (m_context) {
            avio_context_free(&m_context);// Frees both context and buffer
        }
    }

    IOContext(const IOContext &) = delete;
    IOContext &operator=(const IOContext &) = delete;

    [[nodiscard]] AVIOContext *native_handle() const {
        return m_context;
    }

private:
    // Static callback for FFmpeg to read from our input buffer
    static int read(void *opaque, uint8_t *buf, int const buf_size) {
        auto *io = static_cast<IOContext *>(opaque);
        auto const remaining = io->m_input.size() - io->m_position;
        auto const to_copy = std::min(static_cast<size_t>(buf_size), remaining);
        if (to_copy == 0) {
            return AVERROR_EOF;
        }

        std::memcpy(buf, io->m_input.data() + io->m_position, to_copy);
        io->m_position += to_copy;
        return static_cast<int>(to_copy);
    }

    // Static callback for FFmpeg to seek in our buffer
    static int64_t seek(void *opaque, int64_t const offset, int const whence) {
        auto *io = static_cast<IOContext *>(opaque);

        size_t new_pos = 0;
        switch (whence) {
            case SEEK_SET:
                new_pos = static_cast<size_t>(offset);
                break;
            case SEEK_CUR:
                new_pos = static_cast<size_t>(static_cast<int64_t>(io->m_position) + offset);
                break;
            case SEEK_END:
                new_pos = static_cast<size_t>(static_cast<int64_t>(io->m_input.size()) + offset);
                break;
            case AVSEEK_SIZE:
                return static_cast<int64_t>(io->m_input.size());
            default:
                return AVERROR(EINVAL);
        }

        if (new_pos > io->m_input.size()) {
            return AVERROR(EINVAL);
        }

        io->m_position = new_pos;
        return static_cast<int64_t>(new_pos);
    }

    std::vector<uint8_t> m_input;
    size_t m_position;
    uint8_t *m_buffer;
    AVIOContext *m_context;
};

}// anonymous namespace

// Main decoding function
Result<std::vector<f32>> decode_pcm32(std::vector<u8> const &buffer) {
    IOContext const context{ buffer };

    // Allocate and configure format context
    AVFormatContext *fmt_ctx = avformat_alloc_context();
    fmt_ctx->pb = context.native_handle();
    fmt_ctx->flags |= AVFMT_FLAG_CUSTOM_IO;

    // Open input using the custom IO context
    if (avformat_open_input(&fmt_ctx, nullptr, nullptr, nullptr) < 0) {
        avformat_free_context(fmt_ctx);
        return tl::unexpected("Could not open input from buffer.");
    }

    // Parse stream headers
    if (avformat_find_stream_info(fmt_ctx, nullptr) < 0) {
        avformat_close_input(&fmt_ctx);
        return tl::unexpected("Could not retrieve stream info.");
    }

    // Identify the best audio stream
    auto const audio_stream_index = av_find_best_stream(fmt_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);
    if (audio_stream_index < 0) {
        avformat_close_input(&fmt_ctx);
        return tl::unexpected("No audio stream found.");
    }

    auto const *audio_stream = fmt_ctx->streams[audio_stream_index];

    // Find decoder for audio stream
    auto const *codec = avcodec_find_decoder(audio_stream->codecpar->codec_id);
    if (not codec) {
        avformat_close_input(&fmt_ctx);
        return tl::unexpected("No suitable decoder found.");
    }

    // Allocate codec context
    auto *codec_ctx = avcodec_alloc_context3(codec);
    if (not codec_ctx) {
        avformat_close_input(&fmt_ctx);
        return tl::unexpected("Could not allocate codec context.");
    }

    // Copy codec parameters into codec context
    if (avcodec_parameters_to_context(codec_ctx, audio_stream->codecpar) < 0) {
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        return tl::unexpected("Could not copy codec parameters.");
    }

    // Open codec for decoding
    if (avcodec_open2(codec_ctx, codec, nullptr) < 0) {
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        return tl::unexpected("Could not open codec.");
    }

    // Initialize software resampler context for float mono output
    auto *swr_ctx = swr_alloc();
    if (not swr_ctx) {
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        return tl::unexpected("Could not allocate SwrContext.");
    }

    constexpr AVChannelLayout out_layout = AV_CHANNEL_LAYOUT_MONO;

    if (swr_alloc_set_opts2(&swr_ctx, &out_layout, AV_SAMPLE_FMT_FLT, WAVE_SAMPLE_RATE, &codec_ctx->ch_layout,
                            codec_ctx->sample_fmt, codec_ctx->sample_rate, 0, nullptr) < 0 or
        swr_init(swr_ctx) < 0) {
        swr_free(&swr_ctx);
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        return tl::unexpected("Could not configure SwrContext.");
    }

    // Prepare packet and frame containers
    auto *packet = av_packet_alloc();
    auto *frame = av_frame_alloc();
    if (not packet or not frame) {
        av_packet_free(&packet);
        av_frame_free(&frame);
        swr_free(&swr_ctx);
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        return tl::unexpected("Could not allocate AVPacket or AVFrame.");
    }

    std::vector<f32> pcm_data;

    // Lambda for converting and storing decoded frames
    auto resample_and_store = [&](AVFrame const *f) {
        auto const max_out_samples = swr_get_out_samples(swr_ctx, f->nb_samples);
        if (max_out_samples <= 0)
            return;

        std::vector<f32> frame_buffer(max_out_samples);
        auto *out_buf = reinterpret_cast<uint8_t *>(frame_buffer.data());

        auto const **in_buf = const_cast<const uint8_t **>(f->data);
        if (auto const converted_samples = swr_convert(swr_ctx, &out_buf, max_out_samples, in_buf, f->nb_samples);
            converted_samples > 0) {
            pcm_data.insert(pcm_data.end(), frame_buffer.begin(), frame_buffer.begin() + converted_samples);
        }
    };

    // Main decoding loop
    while (av_read_frame(fmt_ctx, packet) >= 0) {
        if (packet->stream_index == audio_stream_index) {
            if (avcodec_send_packet(codec_ctx, packet) == 0) {
                while (avcodec_receive_frame(codec_ctx, frame) == 0) {
                    resample_and_store(frame);
                }
            }
        }
        av_packet_unref(packet);
    }

    // Flush decoder
    avcodec_send_packet(codec_ctx, nullptr);
    while (avcodec_receive_frame(codec_ctx, frame) == 0) {
        resample_and_store(frame);
    }

    // Flush any remaining samples from resampler
    if (auto const remaining = swr_get_out_samples(swr_ctx, 0); remaining > 0) {
        std::vector<f32> flush_buffer(remaining);
        auto *out_buf = reinterpret_cast<uint8_t *>(flush_buffer.data());
        if (auto const flushed = swr_convert(swr_ctx, &out_buf, remaining, nullptr, 0); flushed > 0) {
            pcm_data.insert(pcm_data.end(), flush_buffer.begin(), flush_buffer.begin() + flushed);
        }
    }

    // Free resources
    av_packet_free(&packet);
    av_frame_free(&frame);
    swr_free(&swr_ctx);
    avcodec_free_context(&codec_ctx);
    avformat_close_input(&fmt_ctx);

    return pcm_data;
}