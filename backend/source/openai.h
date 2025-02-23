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

#include <httplib.h>
#include <spdlog/spdlog.h>
#include <nlohmann/json.hpp>

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
};

NLOHMANN_DEFINE_TYPE_NON_INTRUSIVE(CompletionRequest, model, messages, temperature);

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
     * @return The completion result
     */
    Result<std::string> completion(CompletionRequest const &request);

    /**
     * Request the available models
     * @return A list of available models
     */
    Result<std::vector<std::string>> models();

private:
    template<typename ResponseType>
        requires(utils::DeserializableFromJson<ResponseType>)
    Result<ResponseType> authorized_get(std::string_view path) {
        auto headers = httplib::Headers{};
        headers.emplace("Content-Type", "application/json");
        headers.emplace("Authorization", std::format("Bearer {}", token));

        auto const result = m_client.Get(std::format("{}/v1/{}", endpoint, path), headers);
        if (!result) {
            return utils::unexpected_format("Failed to perform GET request: {}", to_string(result.error()));
        }
        if (result->status != httplib::StatusCode::OK_200) {
            return utils::unexpected_format("GET request with status '{}': {}", result->status, result->body);
        }

        try {
            ResponseType const parsed = nlohmann::json::parse(result->body);
            return parsed;
        } catch (std::exception const &e) {
            return utils::unexpected_format("Failed to parse response: {}", e.what());
        }
    }

    template<typename RequestType, typename ResponseType>
        requires(utils::SerializableToJson<RequestType> and utils::DeserializableFromJson<ResponseType>)
    Result<ResponseType> authorized_post(std::string_view path, RequestType const &request) {
        auto headers = httplib::Headers{};
        headers.emplace("Content-Type", "application/json");
        headers.emplace("Authorization", std::format("Bearer {}", token));

        try {
            nlohmann::json const body = request;
            auto const result =
                    m_client.Post(std::format("{}/v1/{}", endpoint, path), headers, body.dump(), "application/json");

            if (!result) {
                return tl::unexpected(std::format("Failed to perform POST request: {}", to_string(result.error())));
            }
            if (result->status != httplib::StatusCode::OK_200) {
                return tl::unexpected(std::format("POST request with status '{}': {}", result->status, result->body));
            }

            ResponseType const parsed = nlohmann::json::parse(result->body);
            return parsed;
        } catch (std::exception const &e) {
            return tl::unexpected(std::format("POST request failed: {}", e.what()));
        }
    }

    std::string endpoint;
    std::string token;
    httplib::Client m_client;
};


#endif// OPEN_AI_H
