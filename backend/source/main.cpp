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

#include <grpcpp/grpcpp.h>
#include <spdlog/spdlog.h>

#include "summarizer.h"
#include "transcriber.h"

static char const *env_or_default(char const *env, char const *alternative) {
    if (auto const *value = std::getenv(env)) {
        return value;
    }
    return alternative;
}

int main(int argc, char **argv) {
    spdlog::info("Starting transcriber server...");
    auto const model_path = env_or_default("WHISPER_MODEL_PATH", "models/ggml-tiny.bin");
    auto const listen_addr = env_or_default("GRPC_LISTEN_ADDRESS", "0.0.0.0:50051");
    auto const openai_endpoint = env_or_default("OPENAI_ENDPOINT", "http://localhost:11434");
    auto const jwt = env_or_default("OPENAI_TOKEN", "");

    if (std::getenv("SPDLOG_DEBUG") != nullptr) {
        spdlog::set_level(spdlog::level::debug);
    }


    spdlog::info("Model path: {}", model_path);
    spdlog::info("Listen address: {}", listen_addr);

    grpc::ServerBuilder builder;
    builder.AddListeningPort(listen_addr, grpc::InsecureServerCredentials());

    TranscriberService transcriber_service{ model_path };
    builder.RegisterService(&transcriber_service);

    SummarizerService summarizer_service{ openai_endpoint, jwt };
    builder.RegisterService(&summarizer_service);

    builder.BuildAndStart()->Wait();
    return 0;
}
