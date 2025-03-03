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

#include <spdlog/spdlog.h>

#include "decode.h"
#include "transcriber.h"

TranscriberService::TranscriberService(std::filesystem::path const &model_path) : m_context{ nullptr } {
    auto *context = whisper_init_from_file_with_params(model_path.string().c_str(), whisper_context_default_params());
    if (!context) {
        spdlog::error("Failed to initialize whisper context, shutting down.");
        std::exit(1);
    }

    m_context = std::make_unique<utils::ReadWriteLock<WhisperContext>>(context, whisper_free);
}

grpc::Status TranscriberService::transcribe(grpc::ServerContext *context,
                                            grpc::ServerReaderWriter<Transcript, Chunk> *stream) {
    spdlog::info("Incoming transcribe request");

    Chunk chunk;
    std::ostringstream data_stream;
    while (stream->Read(&chunk)) {
        auto const &data = chunk.data();
        data_stream.write(data.data(), static_cast<std::streamsize>(data.size()));
    }

    spdlog::info("Finished reading transcribe request");

    auto params = whisper_full_default_params(WHISPER_SAMPLING_BEAM_SEARCH);
    params.new_segment_callback_user_data = stream;
    params.new_segment_callback = [](whisper_context *ctx, whisper_state *, int const n_new, void *user_data) {
        auto *server_stream = static_cast<grpc::ServerReaderWriter<Transcript, Chunk> *>(user_data);
        auto const n_segments = whisper_full_n_segments(ctx);
        for (auto i = n_segments - n_new; i < n_segments; i++) {
            spdlog::debug("writing segment: {}", i);
            Transcript transcript{};
            transcript.set_text(whisper_full_get_segment_text(ctx, i));
            server_stream->Write(transcript);
        }
    };

    auto input_video = data_stream.str();
    auto const decoded = decode_pcm32({ input_video.begin(), input_video.end() });
    if (!decoded) {
        spdlog::error("Failed to decode pcm32 from input: {}", decoded.error());
        auto const message = std::format("Failed to decode PCM32: {}", decoded.error());
        return grpc::Status{ grpc::StatusCode::UNAVAILABLE, message };
    }

    auto context_lock = m_context->lock();
    if (whisper_full(context_lock->get(), params, decoded->data(), static_cast<int>(decoded->size())) != 0) {
        spdlog::error("Failed to transcribe audio");
        return grpc::Status{ grpc::StatusCode::UNAVAILABLE, "Failed to transcribe audio" };
    }

    spdlog::info("Transcribe OK.");
    return grpc::Status::OK;
}

grpc::Status TranscriberService::heartbeat(grpc::ServerContext *context,
                                           google::protobuf::Empty const *,
                                           google::protobuf::Empty *) {
    spdlog::info("Incoming transcriber heartbeat, respond with OK");
    return grpc::Status::OK;
}
