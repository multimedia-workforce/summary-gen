//
//  MIT License
//
//  Copyright (c) 2025 Elias Engelbert Plank
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
#include <iostream>
#include <vector>

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/opt.h>
#include <libswresample/swresample.h>
}

enum {
    WAVE_SAMPLE_RATE = 16000,
    AVIO_BUFFER_SIZE = 0x1000
};

namespace {

// Custom read function
int av_context_read_packet(void *opaque, uint8_t *buf, int const buf_size) {
    const auto &buffer = *static_cast<std::vector<u8> *>(opaque);
    static size_t read_offset = 0;

    auto const remaining = buffer.size() - read_offset;
    auto const to_read = std::min(static_cast<size_t>(buf_size), remaining);
    if (to_read == 0) {
        return AVERROR_EOF;
    }

    std::memcpy(buf, buffer.data() + read_offset, to_read);
    read_offset += to_read;
    return static_cast<int>(to_read);
}

}// namespace

Result<std::vector<f32>> decode_pcm32(std::vector<u8> const &buffer) {
    // Open input file
    auto *fmt_ctx = avformat_alloc_context();
    if (!fmt_ctx) {
        return std::unexpected("Could not allocate format context.");
    }

    auto avio_buffer = static_cast<u8 *>(av_malloc(AVIO_BUFFER_SIZE));
    if (!avio_buffer) {
        avformat_close_input(&fmt_ctx);
        return std::unexpected("Could not allocate AVIO buffer.");
    }

    // Create custom AVIOContext
    auto *avio_ctx = avio_alloc_context(avio_buffer, AVIO_BUFFER_SIZE, 0, (void *) (&buffer), av_context_read_packet,
                                        nullptr, nullptr);
    if (!avio_ctx) {
        av_free(avio_buffer);
        avformat_close_input(&fmt_ctx);
        return std::unexpected("Could not allocate AVIOContext.");
    }
    fmt_ctx->pb = avio_ctx;

    if (avformat_open_input(&fmt_ctx, nullptr, nullptr, nullptr) < 0) {
        avio_context_free(&avio_ctx);
        avformat_free_context(fmt_ctx);
        return std::unexpected("Could not open input from buffer.");
    }

    if (avformat_find_stream_info(fmt_ctx, nullptr) < 0) {
        avformat_close_input(&fmt_ctx);
        avio_context_free(&avio_ctx);
        return std::unexpected("Could not retrieve stream info.");
    }

    // Locate the best audio stream
    auto const audio_stream_index = av_find_best_stream(fmt_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, nullptr, 0);
    if (audio_stream_index < 0) {
        avformat_close_input(&fmt_ctx);
        avio_context_free(&avio_ctx);
        return std::unexpected("No audio stream found.");
    }
    auto const *audio_stream = fmt_ctx->streams[audio_stream_index];

    // Find the decoder
    auto const *codec = avcodec_find_decoder(audio_stream->codecpar->codec_id);
    if (!codec) {
        avformat_close_input(&fmt_ctx);
        avio_context_free(&avio_ctx);
        return std::unexpected("No suitable decoder found.");
    }

    auto *codec_ctx = avcodec_alloc_context3(codec);
    if (!codec_ctx) {
        avformat_close_input(&fmt_ctx);
        avio_context_free(&avio_ctx);
        return std::unexpected("Could not allocate codec context.");
    }

    if (avcodec_parameters_to_context(codec_ctx, audio_stream->codecpar) < 0) {
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        avio_context_free(&avio_ctx);
        return std::unexpected("Could not copy codec parameters.");
    }

    if (avcodec_open2(codec_ctx, codec, nullptr) < 0) {
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        avio_context_free(&avio_ctx);
        return std::unexpected("Could not open codec.");
    }

    // Set up resampling to PCM float 32-bit, mono, 16kHz
    auto *swr_ctx = swr_alloc();
    if (!swr_ctx) {
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        avio_context_free(&avio_ctx);
        return std::unexpected("Could not allocate SwrContext.");
    }

    constexpr AVChannelLayout out_layout = AV_CHANNEL_LAYOUT_MONO;// Ensure mono output
    if (swr_alloc_set_opts2(&swr_ctx, &out_layout, AV_SAMPLE_FMT_FLT, WAVE_SAMPLE_RATE, &codec_ctx->ch_layout,
                            codec_ctx->sample_fmt, codec_ctx->sample_rate, 0, nullptr) < 0) {
        swr_free(&swr_ctx);
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        avio_context_free(&avio_ctx);
        return std::unexpected("Could not set SwrContext options.");
    }

    if (swr_init(swr_ctx) < 0) {
        swr_free(&swr_ctx);
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        avio_context_free(&avio_ctx);
        return std::unexpected("Could not initialize SwrContext.");
    }

    // Allocate necessary structures
    auto *packet = av_packet_alloc();
    auto *frame = av_frame_alloc();
    if (!packet || !frame) {
        av_packet_free(&packet);
        av_frame_free(&frame);
        swr_free(&swr_ctx);
        avcodec_free_context(&codec_ctx);
        avformat_close_input(&fmt_ctx);
        avio_context_free(&avio_ctx);
        return std::unexpected("Could not allocate AVPacket or AVFrame.");
    }

    std::vector<f32> pcm_data;

    // Read and decode frames
    while (av_read_frame(fmt_ctx, packet) >= 0) {
        if (packet->stream_index == audio_stream_index) {
            if (avcodec_send_packet(codec_ctx, packet) == 0) {
                while (avcodec_receive_frame(codec_ctx, frame) == 0) {
                    auto const nb_samples = swr_get_out_samples(swr_ctx, frame->nb_samples);
                    std::vector<f32> frame_buffer(nb_samples);

                    auto *out_buf = reinterpret_cast<uint8_t *>(frame_buffer.data());
                    auto const converted_samples =
                            swr_convert(swr_ctx, &out_buf, nb_samples, frame->data, frame->nb_samples);

                    if (converted_samples > 0) {
                        // Append converted PCM data
                        pcm_data.insert(pcm_data.end(), frame_buffer.begin(), frame_buffer.begin() + converted_samples);
                    }
                }
            }
        }
        av_packet_unref(packet);
    }

    // **Flush remaining samples** in SwrContext to prevent truncation
    if (auto const nb_samples = swr_get_out_samples(swr_ctx, 0); nb_samples > 0) {
        std::vector<float> frame_buffer(nb_samples);
        auto *out_buf = reinterpret_cast<uint8_t *>(frame_buffer.data());

        auto const flushed_samples = swr_convert(swr_ctx, &out_buf, nb_samples, nullptr, 0);
        if (flushed_samples > 0) {
            pcm_data.insert(pcm_data.end(), frame_buffer.begin(), frame_buffer.begin() + flushed_samples);
        }
    }

    // Cleanup
    av_packet_free(&packet);
    av_frame_free(&frame);
    swr_free(&swr_ctx);
    avcodec_free_context(&codec_ctx);
    avformat_close_input(&fmt_ctx);
    avio_context_free(&avio_ctx);

    return pcm_data;
}
