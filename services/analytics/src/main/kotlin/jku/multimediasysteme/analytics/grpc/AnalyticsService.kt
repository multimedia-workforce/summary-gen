package jku.multimediasysteme.analytics.grpc

import jku.multimediasysteme.analytics.service.prompt.PromptService
import jku.multimediasysteme.grpc.analytics.AnalyticsServiceGrpcKt
import jku.multimediasysteme.grpc.analytics.SmartSessionPromptRequest
import jku.multimediasysteme.grpc.analytics.SmartSessionPromptResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import net.devh.boot.grpc.server.service.GrpcService
import java.util.*

@GrpcService
class AnalyticsService(private val promptService: PromptService) :
    AnalyticsServiceGrpcKt.AnalyticsServiceCoroutineImplBase() {

    override fun handleSmartSessionPrompt(request: SmartSessionPromptRequest): Flow<SmartSessionPromptResponse> =
        callbackFlow {
            val disposable = promptService.processPromptStreamed(
                prompt = request.prompt,
                smartSessionIds = request.smartSessionIdsList.map(UUID::fromString)
            ) { chunk ->
                trySend(SmartSessionPromptResponse.newBuilder().setChunk(chunk).build())
            }

            if (disposable == null) {
                close()
                return@callbackFlow
            }

            awaitClose {
                println("Client disconnected or stream closed.")
                disposable.dispose()
            }
        }
}
