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

#include <grpcpp/grpcpp.h>
#include <spdlog/spdlog.h>

#include "summarizer.h"
#include "transcriber.h"

/**
 * Retrieves the specified environment variable. If it is not present, the alternative is returned
 * @param env The name of the environment variable
 * @param alternative The value of the alternative
 * @return Either the value of the environment variable or the alternative
 */
static char const *env_or_default(char const *env, char const *alternative) {
    if (auto const *value = std::getenv(env)) {
        return value;
    }
    return alternative;
}

/**
 * Checks whether the specified environment variable is present
 * @param env The name of the environment variable
 * @return Whether the environment variable is present
 */
static bool env_present(char const *env) {
    return std::getenv(env) != nullptr;
}

int main(int, char **) {
    spdlog::info("Starting transcriber server...");

    // Here all environment variables that are necessary for the configuration of the worker are retrieved
    auto const model_path = env_or_default("WHISPER_MODEL_PATH", "models/ggml-tiny.bin");
    auto const listen_addr = env_or_default("GRPC_LISTEN_ADDRESS", "0.0.0.0:50051");
    auto const persistence_addr = env_or_default("GRPC_PERSISTENCE_ADDRESS", "0.0.0.0:50052");
    auto const openai_endpoint = env_or_default("OPENAI_ENDPOINT", "https://engelbert.ip-ddns.com");
    auto const jwt = env_or_default("OPENAI_TOKEN", "REDACTED");

    // Check if debug logging should be enabled
    if (env_present("SPDLOG_DEBUG")) {
        spdlog::set_level(spdlog::level::debug);
    }

    spdlog::info("Model path: {}", model_path);
    spdlog::info("Listen address: {}", listen_addr);
    spdlog::info("Persistence address: {}", persistence_addr);

    // This creates a gRPC channel in order for the worker to call the persistence gRPC service.
    // Insecure credentials are used to avoid certificate setup
    auto const persistence_channel = CreateChannel(persistence_addr, grpc::InsecureChannelCredentials());
    std::shared_ptr const persistence_stub = persistence::Persistence::NewStub(persistence_channel);

    // The ServerBuilder enables us to configure the gRPC server part of the worker
    grpc::ServerBuilder builder;
    builder.AddListeningPort(listen_addr, grpc::InsecureServerCredentials());

    // The TranscriberService is configured with the whisper model path and persistence stub
    // The model path is necessary for whisper to load its transcription context
    // The persistence stub is necessary to communicate with the persistence gRPC service
    TranscriberService transcriber_service{ model_path, persistence_stub };
    builder.RegisterService(&transcriber_service);

    // The SummarizerService is configured with the OpenAI endpoint (which is in fact DeepSeek), the JWT token
    // which is required for the endpoint and the persistence stub which is necessary to communicate with the
    // persistence gRPC service.
    SummarizerService summarizer_service{ openai_endpoint, jwt, persistence_stub };
    builder.RegisterService(&summarizer_service);

    // The gRPC server is built and started. This call does not return until the server is stopped.
    builder.BuildAndStart()->Wait();
    return 0;
}
