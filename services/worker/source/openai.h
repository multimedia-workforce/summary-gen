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

#ifndef OPEN_AI_H
#define OPEN_AI_H

#include "types.h"
#include "utils/fmt.h"
#include "utils/json.h"

#include <format>

#include <spdlog/spdlog.h>
#include <nlohmann/json.hpp>

#include "utils/http.h"

struct Message {
    std::string content;
    std::string role;

    static Message developer(std::string content) {
        return { std::move(content), "developer" };
    }

    static Message user(std::string content) {
        return { std::move(content), "user" };
    }
};

NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(Message, content, role);

struct CompletionRequest {
    std::string model;
    std::vector<Message> messages;
    f64 temperature = 0.2f;
    bool stream = true;
};

using CompletionCallback = std::function<void(std::string)>;

NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(CompletionRequest, model, messages, temperature, stream);

struct OpenAI {
    /**
     * Instantiates a new OpenAI client
     * @param endpoint The OpenAI endpoint
     * @param token The JWT token for authentication at the endpoint
     */
    OpenAI(std::string endpoint, std::string token);

    /**
     * Performs a completion request
     * @param request The request parameters
     * @param callback The callback used for completions
     * @return The result
     */
    Result<void> completion(CompletionRequest const &request, CompletionCallback const &callback);

    /**
     * Request the available models
     * @return A list of available models
     */
    Result<std::vector<std::string>> models() const;

    std::string endpoint;
    std::string token;
    utils::http::Client m_client;
};


#endif// OPEN_AI_H
