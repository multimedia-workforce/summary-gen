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

#include "utils/continuation.h"
#include "utils/uuid.h"

namespace {

constexpr auto SAMPLE_RATE = 16000;
constexpr auto CHUNK_DURATION = 30;
constexpr auto CHUNK_SIZE = SAMPLE_RATE * CHUNK_DURATION;

struct TranscribeContext {
    std::string transcription_id;
    std::string user_id;
    grpc::ServerReaderWriter<transcriber::Transcript, transcriber::Chunk> *stream;
    grpc::ClientWriter<persistence::Chunk> *persistence_writer;
};

void handle_segment(whisper_context *ctx, whisper_state *, int n_new, void *user_data) {
    auto *context = static_cast<TranscribeContext *>(user_data);
    const int n_segments = whisper_full_n_segments(ctx);

    for (int i = n_segments - n_new; i < n_segments; ++i) {
        const char *text = whisper_full_get_segment_text(ctx, i);
        spdlog::debug("Writing transcript segment: {}", i);

        transcriber::Transcript transcript;
        transcript.set_id(context->transcription_id);
        transcript.set_text(text);
        context->stream->Write(transcript);

        if (context->persistence_writer) {
            persistence::Chunk persistence_chunk;
            persistence_chunk.set_transcriptid(context->transcription_id);
            persistence_chunk.set_userid(context->user_id);
            persistence_chunk.set_text(text);
            persistence_chunk.set_time(std::time(nullptr));
            context->persistence_writer->Write(persistence_chunk);
        }
    }
}

}// anonymous namespace

TranscriberService::TranscriberService(std::filesystem::path const &model_path,
                                       std::shared_ptr<persistence::Persistence::Stub> stub)
    : m_context{ nullptr },
      m_persistence_stub{ std::move(stub) } {

    whisper_context *context =
            whisper_init_from_file_with_params(model_path.string().c_str(), whisper_context_default_params());

    if (!context) {
        spdlog::error("Failed to initialize whisper context, shutting down.");
        std::exit(1);
    }

    m_context = std::make_unique<utils::ReadWriteLock<WhisperContext>>(context, whisper_free);
}

grpc::Status TranscriberService::transcribe(
        grpc::ServerContext *context,
        grpc::ServerReaderWriter<transcriber::Transcript, transcriber::Chunk> *stream) {
    spdlog::info("Incoming transcribe request");

    grpc::ClientContext persist_context;
    google::protobuf::Empty persist_response;

    auto persist_writer = m_persistence_stub->persistTranscript(&persist_context, &persist_response);
    auto persist_finish = utils::Continuation{ [&persist_writer] {
        if (not persist_writer) {
            return;
        }

        persist_writer->WritesDone();
        persist_writer->Finish();
    } };

    if (!persist_writer) {
        spdlog::error("Cannot persist transcription, unable to establish connection!");
    }

    transcriber::Chunk chunk;
    std::ostringstream data_stream;

    while (stream->Read(&chunk)) {
        const auto &data = chunk.data();
        data_stream.write(data.data(), static_cast<std::streamsize>(data.size()));
    }

    spdlog::info("Finished reading transcribe request");

    const std::string input_video = data_stream.str();
    const auto decoded = decode_pcm32({ input_video.begin(), input_video.end() });

    if (!decoded) {
        spdlog::error("Failed to decode pcm32 from input: {}", decoded.error());
        return grpc::Status{ grpc::StatusCode::UNAVAILABLE,
                             std::format("Failed to decode PCM32: {}", decoded.error()) };
    }

    if (decoded->empty()) {
        spdlog::warn("PCM32 samples are empty");
        return grpc::Status{ grpc::StatusCode::UNAVAILABLE, "Failed to retrieve PCM32 samples" };
    }

    spdlog::info("Decoded {} PCM samples", decoded->size());

    auto const transcription_id = utils::UUID::generate_v4();
    TranscribeContext transcribe_context{ .transcription_id = transcription_id,
                                          .user_id = chunk.userid(),
                                          .stream = stream,
                                          .persistence_writer = persist_writer.get() };

    auto params = whisper_full_default_params(WHISPER_SAMPLING_BEAM_SEARCH);
    params.new_segment_callback_user_data = &transcribe_context;
    params.new_segment_callback = handle_segment;

    // Setting the language to nullptr leads to auto-detect
    params.language = nullptr;
    params.translate = false;

    auto context_lock = m_context->lock();
    for (size_t i = 0; i < decoded->size(); i += CHUNK_SIZE) {
        auto const len = std::min(static_cast<size_t>(CHUNK_SIZE), decoded->size() - i);

        if (whisper_full(context_lock->get(), params, decoded->data() + i, static_cast<int>(len)) != 0) {
            spdlog::error("Failed to transcribe chunk at offset {} ({} samples)", i, len);
            return grpc::Status{ grpc::StatusCode::UNAVAILABLE, "Failed to transcribe audio chunk" };
        }

        spdlog::debug("Transcribed chunk {} ({} samples)", i / CHUNK_SIZE, len);
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
