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

#ifndef UTILS_HTTP_H
#define UTILS_HTTP_H

#include <curl/curl.h>

#include <format>
#include <memory>
#include <string_view>

#include "../types.h"
#include "json.h"

namespace utils::http {

class Headers {
public:
    Headers() = default;
    ~Headers();

    Headers(Headers const &) = delete;
    Headers &operator=(Headers const &) = delete;
    Headers(Headers &&) = default;
    Headers &operator=(Headers &&) = default;

    /**
     * Adds a new header entry
     * @param key The key
     * @param value The value
     */
    void add(std::string_view key, std::string_view value);

    /**
     * The header native handle
     * @return The curl header native handle
     */
    [[nodiscard]] curl_slist *native_handle() const;

private:
    curl_slist *m_headers;
};

class Client {
public:
    explicit Client(std::string endpoint, std::string token);

    /**
     * Performs an authorized GET request to the specified path
     * @param path The path
     * @return The response
     */
    [[nodiscard]] Result<std::string> authorized_get(std::string_view path) const;

    /**
     * Performs an authorized GET request to the specified path and converts to the response
     * @tparam ResponseType The type of the response
     * @param path The path
     * @return The response
     */
    template<typename ResponseType>
        requires utils::DeserializableFromJson<ResponseType>
    [[nodiscard]] Result<ResponseType> authorized_get_response(std::string_view path) const {
        return authorized_get(path).and_then([](std::string response) -> Result<ResponseType> {
            try {
                ResponseType parsed = nlohmann::json::parse(response);
                return parsed;
            } catch (const std::exception &e) {
                return tl::unexpected(std::format("Failed to parse response: {}", e.what()));
            }
        });
    }

    using ServerSentEvent = std::function<void(std::string)>;

    /**
     * Performs an authorized POST request and streams back server sent events
     * @param path The path
     * @param body The body
     * @param callback The callback for server sent events
     * @return Result
     */
    [[nodiscard]] Result<void> authorized_post_stream(std::string_view path,
                                                      std::string_view body,
                                                      ServerSentEvent callback);

    template<typename RequestType>
        requires utils::SerializableToJson<RequestType>
    Result<void> authorized_post_stream(std::string_view path, const RequestType &request, ServerSentEvent callback) {
        nlohmann::json const body = request;
        return authorized_post_stream(path, std::string_view{ body.dump() }, std::move(callback));
    }

private:
    using Handle = std::unique_ptr<CURL, decltype(&curl_easy_cleanup)>;

    std::string m_endpoint;
    std::string m_token;
};

}// namespace utils::http

#endif// UTILS_HTTP_H
