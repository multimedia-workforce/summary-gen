package jku.multimediasysteme.analytics.grpc

import jku.multimediasysteme.analytics.service.prompt.PromptService
import jku.multimediasysteme.grpc.analytics.AnalyticsGrpcKt
import jku.multimediasysteme.grpc.analytics.SmartSessionPromptRequest
import jku.multimediasysteme.grpc.analytics.SmartSessionPromptResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import net.devh.boot.grpc.server.service.GrpcService
import java.util.*

/**
 * gRPC service implementation that handles prompt-based analytics requests for selected SmartSessions.
 *
 * Provides streamed responses for interactive analysis via the external DeepSeek API.
 *
 * @param promptService Service responsible for processing and forwarding prompt requests.
 */
@GrpcService
class PromptAnalyticsService(private val promptService: PromptService) :
    AnalyticsGrpcKt.AnalyticsCoroutineImplBase() {

    /**
     * Handles a prompt request for selected SmartSession IDs.
     *
     * Receives a [SmartSessionPromptRequest] which includes a user prompt, model config, temperature, and
     * a list of session IDs. For each session, transcriptions and summaries are collected and passed to
     * an external model (DeepSeek) for streamed text generation. The result is streamed back to the client
     * in chunks via [SmartSessionPromptResponse].
     *
     * @param request Incoming prompt request with model and session metadata.
     * @return Stream of responses containing prompt results.
     */
    override fun handleSmartSessionPrompt(
        request: SmartSessionPromptRequest
    ): Flow<SmartSessionPromptResponse> = callbackFlow {
        promptService.processPromptStreamed(
            prompt = request.prompt,
            smartSessionIds = request.smartSessionIdsList.map(UUID::fromString),
            model = request.model,
            temperature = request.temperature,
            onChunk = { chunk ->
                trySend(SmartSessionPromptResponse.newBuilder().setChunk(chunk).build())
            },
            onDone = { close() },
            onError = { close(it) }
        )
        awaitClose()
    }
}