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
#include <nlohmann/json.hpp>

#include "summarizer.h"

#include "utils/continuation.h"
#include "utils/uuid.h"

// This message is passed to the OpenAI instance as a developer suggestion to the model
constexpr auto COMPLETION_DEV_MESSAGE = R"(
Summarize this meeting transcription with **maximum accuracy and clarity**. The summary **must** include:

- **Agenda**: Clearly list the topics planned for discussion.
- **Major Discussion Points**: Identify the key subjects covered, summarize arguments, and capture important exchanges.
- **Decisions & Outcomes**: State what was decided, what actions were assigned, and any unresolved issues.

This summary **must be concise but complete**—no fluff, no unnecessary details. **Extract only what matters** while preserving meaning. If there’s repetition or off-topic chatter, **cut it out**. Format the output with bullet points for readability.

Do not miss anything important. **Precision is critical.**
)";

SummarizerService::SummarizerService(std::string endpoint,
                                     std::string token,
                                     std::shared_ptr<persistence::Persistence::Stub> stub)
    : m_client{ std::move(endpoint), std::move(token) },
      m_persistence_stub{ std::move(stub) } { }

grpc::Status SummarizerService::summarize(grpc::ServerContext *context,
                                          summarizer::Prompt const *request,
                                          grpc::ServerWriter<summarizer::Summary> *writer) {
    spdlog::info("Incoming summarize request");

    // Initialize the client context required to perform calls to the gRPC persistence service
    grpc::ClientContext persist_context;
    google::protobuf::Empty persist_response;

    // Initialize the client writer required to perform calls to the gRPC persistence service
    auto persist_writer = m_persistence_stub->persistSummary(&persist_context, &persist_response);

    // This continuation makes sure that the writer is properly dealt with, no matter what happens via RAII
    auto persist_finish = utils::Continuation{ [&persist_writer] {
        if (not persist_writer) {
            return;
        }

        persist_writer->WritesDone();
        persist_writer->Finish();
    } };

    if (not persist_writer) {
        spdlog::error("Cannot persist transcription, unable to establish connection!");
    }

    // Generate a summary ID for correlation
    auto const summary_id = utils::UUID::generate_v4();

    // Prepare the completion request to pass to the OpenAI instance
    CompletionRequest completion_request;
    completion_request.model = request->model();
    completion_request.temperature = request->temperature();
    completion_request.messages = { Message::developer(COMPLETION_DEV_MESSAGE),
                                    Message::user(std::format("{}: {}", request->prompt(), request->transcript())) };

    // Perform the actual completion call with our custom callback
    auto const result = m_client.completion(completion_request,
                                            [request, writer, &persist_writer, summary_id](std::string message) {
                                                spdlog::debug("Received summary chunk of size {}", message.size());

                                                // Prepare the summary chunk and configure the message
                                                summarizer::Summary summary;
                                                summary.set_text(message);
                                                writer->Write(summary);

                                                // If the persistence writer is configured, write the chunk to
                                                // persistence
                                                if (persist_writer) {
                                                    persistence::Chunk persistence_chunk;
                                                    persistence_chunk.set_transcriptid(request->transcriptid());
                                                    persistence_chunk.set_summaryid(summary_id);
                                                    persistence_chunk.set_userid(request->userid());
                                                    persistence_chunk.set_text(message);
                                                    persistence_chunk.set_time(std::time(nullptr));
                                                    persist_writer->Write(persistence_chunk);
                                                }
                                            });

    // If the completion call failed, return an error to the caller
    if (not result) {
        spdlog::error("Failed to summarize: {}", result.error());
        return grpc::Status{ grpc::StatusCode::UNAVAILABLE, result.error() };
    }

    spdlog::info("Summarize OK.");
    return grpc::Status::OK;
}

grpc::Status SummarizerService::models(grpc::ServerContext *context,
                                       google::protobuf::Empty const *request,
                                       summarizer::Models *response) {
    spdlog::info("Incoming models request");

    // Asks the OpenAI instance for the available models
    auto const result = m_client.models();

    // If the result is not successful, return the error to the user
    if (not result) {
        spdlog::error("Failed to retrieve models: {}", result.error());
        return grpc::Status{ grpc::StatusCode::UNAVAILABLE, result.error() };
    }

    // Build the gRPC response
    std::ranges::for_each(*result, [response](auto &&model) { response->add_models(model); });

    spdlog::info("Models OK.");
    return grpc::Status::OK;
}

grpc::Status SummarizerService::heartbeat(grpc::ServerContext *context,
                                          const google::protobuf::Empty *request,
                                          google::protobuf::Empty *response) {
    spdlog::info("Incoming summarizer heartbeat, respond with OK");
    return grpc::Status::OK;
}
