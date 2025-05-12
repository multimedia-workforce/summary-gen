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

#include <spdlog/spdlog.h>

#include "decode.h"
#include "transcriber.h"

#include "utils/continuation.h"
#include "utils/uuid.h"

namespace {

// Whisper expects a sample rate of 16 kHz
constexpr auto SAMPLE_RATE = 16000;

// Whisper can properly handle chunks of 30 seconds
constexpr auto CHUNK_DURATION = 30;

// The chunk size in bytes is determined by the sample rate and chunk duration
constexpr auto CHUNK_SIZE = SAMPLE_RATE * CHUNK_DURATION;

/**
 * The TranscribeContext encapsulates all transcription relevant data in one struct
 * in order for the segment callback of whisper to access all relevant information.
 *
 * This is necessary since whisper only allows passing one user argument to the
 * segment callback.
 */
struct TranscribeContext {
    // The ID of the transcription. This is required for the call to the persistence gRPC service.
    std::string transcription_id;

    // The ID of the user which initiated the transcription. This is required for the call to the persistence gRPC
    // service.
    std::string user_id;

    // The gRPC interface for passing the segments to the caller.
    grpc::ServerReaderWriter<transcriber::Transcript, transcriber::Chunk> *stream;

    // The gRPC interface for writing the newly generated transcription chunks to the persistence service.
    grpc::ClientWriter<persistence::Chunk> *persistence_writer;
};

/**
 * Handles a newly generated segment, which is a transcription chunk
 * @param ctx The context of whisper
 * @param n_new Indicates where the new segments start
 * @param user_data The user data, which is our TranscribeContext handle
 */
void handle_segment(whisper_context *ctx, whisper_state *, int const n_new, void *user_data) {
    // Retrieve the TranscribeContext from the user data argument of whisper
    auto *context = static_cast<TranscribeContext *>(user_data);
    auto const n_segments = whisper_full_n_segments(ctx);

    // Loop over all new segments
    for (int i = n_segments - n_new; i < n_segments; ++i) {
        // Read the newly generated text chunk
        auto const *text = whisper_full_get_segment_text(ctx, i);
        spdlog::debug("Writing transcript segment: {}", i);

        // Prepare the transcript chunk and write it to the caller
        transcriber::Transcript transcript;
        transcript.set_id(context->transcription_id);
        transcript.set_text(text);
        context->stream->Write(transcript);

        // If the persistence writer is configured, write the chunk to persistence
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

    // Load the whisper model from the specified model path with default params
    auto *context = whisper_init_from_file_with_params(model_path.string().c_str(), whisper_context_default_params());
    if (not context) {
        spdlog::error("Failed to initialize whisper context, shutting down.");
        std::exit(1);
    }

    // Initialize our context structure which is a ReadWriteLock to guarantee thread safety
    m_context = std::make_unique<utils::ReadWriteLock<WhisperContext>>(context, whisper_free);
}

grpc::Status TranscriberService::transcribe(
        grpc::ServerContext *context,
        grpc::ServerReaderWriter<transcriber::Transcript, transcriber::Chunk> *stream) {
    spdlog::info("Incoming transcribe request");

    // Initialize the client context required to perform calls to the gRPC persistence service
    grpc::ClientContext persist_context;
    google::protobuf::Empty persist_response;

    // Initialize the client writer required to perform calls to the gRPC persistence service
    auto persist_writer = m_persistence_stub->persistTranscript(&persist_context, &persist_response);

    // This continuation makes sure that the writer is properly dealt with, no matter what happens via RAII
    auto persist_finish = utils::Continuation{ [&persist_writer] {
        if (not persist_writer) {
            return;
        }

        persist_writer->WritesDone();
        persist_writer->Finish();
    } };

    // Even if the persistence writer is not present, we still want to perform the transcription
    if (not persist_writer) {
        spdlog::error("Cannot persist transcription, unable to establish connection!");
    }

    // Prepare a chunk
    transcriber::Chunk chunk;
    std::ostringstream data_stream;

    // While there are incoming chunks of the media file, append them to our data stream
    while (stream->Read(&chunk)) {
        const auto &data = chunk.data();
        data_stream.write(data.data(), static_cast<std::streamsize>(data.size()));
    }

    spdlog::info("Finished reading transcribe request");

    // Convert our data stream to a full string
    auto const input_video = data_stream.str();

    // This performs the actual conversion of the input media file to raw PCM32 samples
    auto const decoded = decode_pcm32({ input_video.begin(), input_video.end() });

    // If the decoding failed, return an error to the caller
    if (not decoded) {
        spdlog::error("Failed to decode pcm32 from input: {}", decoded.error());
        return grpc::Status{ grpc::StatusCode::UNAVAILABLE,
                             std::format("Failed to decode PCM32: {}", decoded.error()) };
    }

    // If there are no samples, return an error to the caller
    if (decoded->empty()) {
        spdlog::warn("PCM32 samples are empty");
        return grpc::Status{ grpc::StatusCode::UNAVAILABLE, "Failed to retrieve PCM32 samples" };
    }

    spdlog::info("Decoded {} PCM samples", decoded->size());

    // Initialize the TranscribeContext to pass it to whisper
    // The transcription ID is necessary to correlate it later on to a summary -> together they form a smart session
    auto const transcription_id = utils::UUID::generate_v4();
    TranscribeContext transcribe_context{ .transcription_id = transcription_id,
                                          .user_id = chunk.userid(),
                                          .stream = stream,
                                          .persistence_writer = persist_writer.get() };

    // Initialize whisper with the default parameters and the callback function to handle new segments
    // Beam search performs better than greedy search
    auto params = whisper_full_default_params(WHISPER_SAMPLING_BEAM_SEARCH);
    params.new_segment_callback_user_data = &transcribe_context;
    params.new_segment_callback = handle_segment;

    // Setting the language to nullptr leads to auto-detect. We don't want to translate to english.
    params.language = nullptr;
    params.translate = false;

    // Split the decoded PCM samples into chunks to avoid overloading whisper
    for (size_t i = 0; i < decoded->size(); i += CHUNK_SIZE) {
        auto const len = std::min(static_cast<size_t>(CHUNK_SIZE), decoded->size() - i);

        // Lock the context as now we want to perform the actual transcription
        auto context_lock = m_context->lock();

        // This performs the actual transcription
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
