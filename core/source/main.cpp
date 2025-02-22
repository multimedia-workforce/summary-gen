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

#include "transcriber.h"

int main(int argc, char **argv) {
    spdlog::info("Starting transcriber server...");
    if (argc != 3) {
        spdlog::error("Usage: {} <model-path> <port>", argv[0]);
        return 1;
    }

    spdlog::info("Model path: {}", argv[1]);
    spdlog::info("Port: {}", argv[2]);

    auto listen_address = std::format("0.0.0.0:{}", argv[2]);
    spdlog::info("Listening on {}", listen_address);

    grpc::ServerBuilder builder;
    builder.AddListeningPort(listen_address, grpc::InsecureServerCredentials());

    TranscriberService transcriber_service{ argv[1] };
    builder.RegisterService(&transcriber_service);
    auto const server = builder.BuildAndStart();
    server->Wait();
    return 0;
}
