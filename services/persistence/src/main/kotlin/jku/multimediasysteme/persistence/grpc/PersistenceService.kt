package jku.multimediasysteme.persistence.grpc

import com.google.protobuf.Empty
import io.grpc.Status
import jku.multimediasysteme.grpc.persistence.Chunk
import jku.multimediasysteme.grpc.persistence.PersistenceGrpcKt
import jku.multimediasysteme.shared.jpa.transcription.model.Summary
import jku.multimediasysteme.shared.jpa.transcription.model.Transcription
import jku.multimediasysteme.shared.jpa.transcription.repository.SummaryRepository
import jku.multimediasysteme.shared.jpa.transcription.repository.TranscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import net.devh.boot.grpc.server.service.GrpcService
import java.util.*


@GrpcService
class PersistenceService(
    private val transcriptionRepository: TranscriptionRepository,
    private val summaryRepository: SummaryRepository
) : PersistenceGrpcKt.PersistenceCoroutineImplBase() {

    override suspend fun persistTranscript(requests: Flow<Chunk>): Empty {
        val chunks = requests.toList()
        if (chunks.isEmpty()) {
            throw Status.FAILED_PRECONDITION
                .withDescription("No chunks received")
                .asException()
        }

        val transcriptionText = chunks.joinToString(separator = " ") { it.text }
        val last = chunks.last()
        val transcription = Transcription(
            id = UUID.fromString(last.id),
            userId = UUID.fromString(last.userId),
            text = transcriptionText,
            time = last.time
        )

        withContext(Dispatchers.IO) {
            transcriptionRepository.save(transcription)
        }

        return Empty.getDefaultInstance()
    }

    override suspend fun persistSummary(requests: Flow<Chunk>): Empty {
        val chunks = requests.toList()
        if (chunks.isEmpty()) {
            throw Status.FAILED_PRECONDITION
                .withDescription("No chunks received")
                .asException()
        }

        val summaryText = chunks.joinToString(separator = " ") { it.text }
        val last = chunks.last()
        val transcription = Summary(
            id = UUID.fromString(last.id),
            userId = UUID.fromString(last.userId),
            text = summaryText,
            time = last.time
        )

        withContext(Dispatchers.IO) {
            summaryRepository.save(transcription)
        }

        return Empty.getDefaultInstance()
    }
}