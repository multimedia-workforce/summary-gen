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

#include "http.h"

#include <format>
#include <utility>

namespace utils::http {

namespace {

struct CallbackContext {
    Client::ServerSentEvent callback;
    std::string buffer;
};

size_t http_get_write(char const *ptr, size_t const size, size_t const nmemb, void *userdata) {
    auto *out = static_cast<std::string *>(userdata);
    auto const total = size * nmemb;
    out->append(ptr, total);
    return total;
}

size_t http_post_write_stream(char const *ptr, size_t const size, size_t const nmemb, void *userdata) {
    auto *ctx = static_cast<CallbackContext *>(userdata);
    auto const total = size * nmemb;
    ctx->buffer.append(ptr, total);

    std::istringstream stream(ctx->buffer);
    std::string line;
    std::string leftover;

    while (std::getline(stream, line)) {
        if (line.starts_with("data: ")) {
            ctx->callback(line.substr(6));
        }
        if (stream.eof()) {
            leftover = line;
        }
    }
    ctx->buffer = std::move(leftover);
    return total;
}
}// namespace

Headers::~Headers() {
    curl_slist_free_all(m_headers);
}

void Headers::add(std::string_view key, std::string_view value) {
    auto const header = std::format("{}: {}", key, value);
    m_headers = curl_slist_append(m_headers, header.c_str());
}

curl_slist *Headers::native_handle() const {
    return m_headers;
}

Client::Client(std::string endpoint, std::string token) : m_endpoint(std::move(endpoint)), m_token(std::move(token)) { }

Result<std::string> Client::authorized_get(std::string_view path) const {
    auto const handle = Handle{ curl_easy_init(), curl_easy_cleanup };
    if (!handle)
        return tl::unexpected("Failed to init CURL");

    auto const url = std::format("{}/v1/{}", m_endpoint, path);
    std::string response;
    std::string error_buffer(CURL_ERROR_SIZE, '\0');

    Headers headers{};
    headers.add("Content-Type", "application/json");
    headers.add("Authorization", std::format("Bearer {}", m_token));

    curl_easy_setopt(handle.get(), CURLOPT_URL, url.c_str());
    curl_easy_setopt(handle.get(), CURLOPT_HTTPHEADER, headers.native_handle());
    curl_easy_setopt(handle.get(), CURLOPT_WRITEFUNCTION, http_get_write);

    curl_easy_setopt(handle.get(), CURLOPT_WRITEDATA, &response);
    curl_easy_setopt(handle.get(), CURLOPT_ERRORBUFFER, error_buffer.data());

    if (auto const res = curl_easy_perform(handle.get()); res != CURLE_OK) {
        return tl::unexpected(std::format("CURL error: {}", error_buffer));
    }

    return response;
}

Result<void> Client::authorized_post_stream(std::string_view path, std::string_view body, ServerSentEvent callback) {
    auto const handle = Handle{ curl_easy_init(), curl_easy_cleanup };
    if (!handle)
        return tl::unexpected("Failed to init CURL");

    auto const url = std::format("{}/v1/{}", m_endpoint, path);
    std::string error_buffer(CURL_ERROR_SIZE, '\0');

    Headers headers{};
    headers.add("Content-Type", "application/json");
    headers.add("Authorization", std::format("Bearer {}", m_token));
    headers.add("Accept", "text/event-stream");

    struct CallbackContext {
        ServerSentEvent callback;
        std::string buffer;
    } ctx{ std::move(callback), "" };

    curl_easy_setopt(handle.get(), CURLOPT_URL, url.c_str());
    curl_easy_setopt(handle.get(), CURLOPT_HTTPHEADER, headers.native_handle());
    curl_easy_setopt(handle.get(), CURLOPT_POSTFIELDS, body.data());
    curl_easy_setopt(handle.get(), CURLOPT_POSTFIELDSIZE, body.size());
    curl_easy_setopt(handle.get(), CURLOPT_WRITEFUNCTION, http_post_write_stream);
    curl_easy_setopt(handle.get(), CURLOPT_WRITEDATA, &ctx);
    curl_easy_setopt(handle.get(), CURLOPT_ERRORBUFFER, error_buffer.data());

    if (auto const res = curl_easy_perform(handle.get()); res != CURLE_OK) {
        return tl::unexpected(std::format("CURL error: {}", error_buffer));
    }
    return {};
}

}// namespace utils::http