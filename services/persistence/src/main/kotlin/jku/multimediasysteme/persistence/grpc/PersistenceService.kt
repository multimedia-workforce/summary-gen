package jku.multimediasysteme.persistence.grpc

import com.google.protobuf.Empty
import io.grpc.Status
import jku.multimediasysteme.grpc.persistence.Chunk
import jku.multimediasysteme.grpc.persistence.PersistenceGrpcKt
import jku.multimediasysteme.shared.jpa.transcription.model.Transcription
import jku.multimediasysteme.shared.jpa.transcription.repository.TranscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import net.devh.boot.grpc.server.service.GrpcService
import java.util.*


@GrpcService
class PersistenceService(
    private val repository: TranscriptionRepository
) : PersistenceGrpcKt.PersistenceCoroutineImplBase() {

    override suspend fun persist(requests: Flow<Chunk>): Empty {
        val chunks = requests.toList()
        if (chunks.isEmpty()) {
            throw Status.FAILED_PRECONDITION
                .withDescription("No chunks received")
                .asRuntimeException()
        }

        val fullSummaryText = chunks.joinToString(separator = " ") { it.text }
        val last = chunks.last()
        val transcription = Transcription(
            id = UUID.fromString(last.id),
            userId = UUID.fromString(last.userId),
            summaryText = fullSummaryText
        )

        withContext(Dispatchers.IO) {
            repository.save(transcription)
        }

        return Empty.getDefaultInstance()
    }
}