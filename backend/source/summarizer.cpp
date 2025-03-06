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

#include <httplib.h>
#include <spdlog/spdlog.h>
#include <nlohmann/json.hpp>

#include "summarizer.h"

constexpr auto COMPLETION_DEV_MESSAGE = R"(
Summarize this meeting transcription with **maximum accuracy and clarity**. The summary **must** include:

- **Agenda**: Clearly list the topics planned for discussion.
- **Major Discussion Points**: Identify the key subjects covered, summarize arguments, and capture important exchanges.
- **Decisions & Outcomes**: State what was decided, what actions were assigned, and any unresolved issues.

This summary **must be concise but complete**—no fluff, no unnecessary details. **Extract only what matters** while preserving meaning. If there’s repetition or off-topic chatter, **cut it out**. Format the output with bullet points for readability.

Do not miss anything important. **Precision is critical.**
)";

SummarizerService::SummarizerService(std::string endpoint, std::string token)
    : m_client{ std::move(endpoint), std::move(token) } { }

grpc::Status SummarizerService::summarize(grpc::ServerContext *context, Prompt const *request, Summary *response) {
    spdlog::info("Incoming summarize request");

    CompletionRequest completion_request;
    completion_request.model = request->model();
    completion_request.temperature = request->temperature();
    completion_request.messages = { Message::developer(COMPLETION_DEV_MESSAGE),
                                    Message::user(std::format("{}: {}", request->prompt(), request->transcript())) };

    auto const result = m_client.completion(completion_request);
    if (not result) {
        spdlog::error("Failed to summarize: {}", result.error());
        return grpc::Status{ grpc::StatusCode::UNAVAILABLE, result.error() };
    }
    response->set_text(*result);

    spdlog::info("Summarize OK.");
    return grpc::Status::OK;
}

grpc::Status SummarizerService::models(grpc::ServerContext *context,
                                       google::protobuf::Empty const *request,
                                       Models *response) {
    spdlog::info("Incoming models request");
    auto const result = m_client.models();
    if (not result) {
        spdlog::error("Failed to retrieve models: {}", result.error());
        return grpc::Status{ grpc::StatusCode::UNAVAILABLE, result.error() };
    }

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
