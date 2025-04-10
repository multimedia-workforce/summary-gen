package jku.multimediasysteme.persistanceservice.grpc

import jku.multimediasysteme.grpc.transcription.TranscriptChunk
import jku.multimediasysteme.grpc.transcription.TranscriptionPersistenceServiceGrpcKt
import jku.multimediasysteme.grpc.transcription.TranscriptionUploadResult
import jku.multimediasysteme.persistanceservice.data.transcription.model.Transcription
import jku.multimediasysteme.persistanceservice.data.transcription.repository.TranscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import net.devh.boot.grpc.server.service.GrpcService
import java.util.*


@GrpcService
class TranscriptionGrpcService(
    private val repository: TranscriptionRepository
) : TranscriptionPersistenceServiceGrpcKt.TranscriptionPersistenceServiceCoroutineImplBase() {

    override suspend fun streamTranscription(requests: Flow<TranscriptChunk>): TranscriptionUploadResult {
        val chunks = requests.toList()

        val last = chunks.lastOrNull() ?: return TranscriptionUploadResult.newBuilder()
            .setMessage("No chunks received")
            .setTranscriptionId("")
            .build()

        val fullDescription = chunks.joinToString(separator = " ") { it.description }
        val fullSummaryText = chunks.joinToString(separator = " ") { it.summaryText }

        val transcription = Transcription(
            userId = UUID.fromString(last.userId),
            description = fullDescription,
            summaryText = fullSummaryText
        )

        val saved = repository.save(transcription)

        return TranscriptionUploadResult.newBuilder()
            .setMessage("Saved successfully")
            .setTranscriptionId(saved.id.toString())
            .build()
    }
}