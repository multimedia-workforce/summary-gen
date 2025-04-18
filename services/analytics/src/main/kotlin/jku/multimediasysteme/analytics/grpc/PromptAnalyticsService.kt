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

@GrpcService
class PromptAnalyticsService(private val promptService: PromptService) :
    AnalyticsGrpcKt.AnalyticsCoroutineImplBase() {

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