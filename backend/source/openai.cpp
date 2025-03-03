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

#include "openai.h"
#include "utils/collect.h"

#include <httplib.h>
#include <nlohmann/json.hpp>
#include <tl/expected.hpp>

#include <format>

namespace {

struct ModelsResponse {
    struct Entry {
        std::string id;
        std::string object;
        u64 created = 0;
    };

    std::vector<Entry> data;
};

NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(ModelsResponse::Entry, id, object, created);
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(ModelsResponse, data);

struct CompletionResponse {
    struct Choice {
        Message message;
    };

    std::vector<Choice> choices;
};

NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(CompletionResponse::Choice, message);
NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(CompletionResponse, choices);

}// namespace

OpenAI::OpenAI(std::string endpoint, std::string token)
    : endpoint{ std::move(endpoint) },
      token{ std::move(token) },
      m_client{ this->endpoint } { }

Result<std::string> OpenAI::completion(CompletionRequest const &request) {
    spdlog::debug("Performing completion request: {}", nlohmann::json(request).dump());

    auto const response = authorized_post<CompletionRequest, CompletionResponse>("chat/completions", request);
    if (!response) {
        return tl::unexpected(response.error());
    }
    if (response->choices.empty()) {
        spdlog::error("Completion responded with zero choices.");
        return tl::unexpected("Completion responded with zero choices.");
    }
    return std::ranges::begin(response->choices)->message.content;
}

Result<std::vector<std::string>> OpenAI::models() {
    auto const response = authorized_get<ModelsResponse>("models");
    if (!response) {
        return tl::unexpected(response.error());
    }
    return std::views::transform(response->data, [](auto &&e) { return e.id; }) |
           utils::collect<std::vector<std::string>>();
}
