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

#ifndef SUMMARIZER_H
#define SUMMARIZER_H

#include "openai.h"

#include <summarizer.grpc.pb.h>
#include <whisper.h>

struct SummarizerService final : Summarizer::Service {
    /**
     * Instantiates a new summarizer gRPC service
     * @param endpoint The OpenAI endpoint
     * @param token The JWT token for authentication at the endpoint
     */
    SummarizerService(std::string endpoint, std::string token);

    /**
     * Summarizes a given text
     * @param context The server context
     * @param request The summarize request
     * @param response The response writer
     * @return A grpc status
     */
    grpc::Status summarize(grpc::ServerContext *context, Prompt const *request, Summary *response) override;

    /**
     * Retrieves a list of the available OpenAI models
     * @param context The server context
     * @param request The request, which is empty
     * @param response The response, which contains a list of models
     */
    grpc::Status models(grpc::ServerContext *context,
                        google::protobuf::Empty const *request,
                        Models *response) override;

    /**
     * Endpoint for checking whether the summarizer service is running
     * @param context The server context
     * @param request The heartbeat request, which is empty
     * @param response The response, which is empty
     * @return A grpc status
     */
    grpc::Status heartbeat(grpc::ServerContext *context,
                           google::protobuf::Empty const *request,
                           google::protobuf::Empty *response) override;

private:
    OpenAI m_client;
};

#endif// SUMMARIZER_H
