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

#include "openai.h"
#include "utils/collect.h"

#include <nlohmann/json.hpp>
#include <tl/expected.hpp>

#include <format>

namespace {

/**
 * Defines the ModelsResponse DTO for the OpenAI API
 */
struct ModelsResponse {
    struct Entry {
        std::string id;
        std::string object;
        u64 created = 0;
    };

    std::vector<Entry> data;
};

// Defines the nlohmann::json conversion functions for the ModelsResponse::Entry DTO
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(ModelsResponse::Entry, id, object, created);

// Defines the nlohmann::json conversion functions for the ModelsResponse DTO
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(ModelsResponse, data);

/**
 * Defines the Choice DTO for the OpenAI API
 */
struct Choice {
    struct Delta {
        std::string role;
        std::string content;
    };

    Delta delta;
};

// Defines the nlohmann::json conversion functions for the Choice::Delta DTO
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(Choice::Delta, role, content);

// Defines the nlohmann::json conversion functions for the Choice DTO
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(Choice, delta);

/**
 * Defines the ChatCompletionChunk DTO for the OpenAI API
 */
struct ChatCompletionChunk {
    std::string model;
    std::vector<Choice> choices;
};

// Defines the nlohmann::json conversion functions for the ChatCompletionChunk DTO
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(ChatCompletionChunk, model, choices)

}// namespace

OpenAI::OpenAI(std::string endpoint, std::string token)
    : endpoint{ std::move(endpoint) },
      token{ std::move(token) },
      m_client{ this->endpoint, this->token } { }

Result<void> OpenAI::completion(CompletionRequest const &request, CompletionCallback const &callback) {
    spdlog::debug("Performing completion request: {}", nlohmann::json(request).dump());

    // Performs the completion API call via the HTTP client.
    // The completion call is an HTTP POST request with an SSE response.
    return m_client.authorized_post_stream<CompletionRequest>(
            "chat/completions", request, [callback](std::string message) {
                try {
                    // Indicates that the call is finished
                    if (message == "[DONE]") {
                        return;
                    }

                    // Parse the completion message as a chunk
                    ChatCompletionChunk chunk = nlohmann::json::parse(message);

                    // Use the first choice and pass its delta to the callback
                    callback(std::ranges::begin(chunk.choices)->delta.content);
                } catch (std::exception const &e) {
                    spdlog::error("Unsupported OpenAI stream message: {} (message: {})", e.what(), message);
                }
            });
}

Result<std::vector<std::string>> OpenAI::models() const {
    // Performs the model API call via the HTTP client
    auto const response = m_client.authorized_get_response<ModelsResponse>("models");
    if (not response) {
        return tl::unexpected(response.error());
    }
    return std::views::transform(response->data, [](auto &&e) { return e.id; }) |
           utils::collect<std::vector<std::string>>();
}
