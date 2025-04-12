package jku.multimediasysteme.persistence.grpc

import com.google.protobuf.Empty
import io.grpc.Status
import jku.multimediasysteme.grpc.persistence.Chunk
import jku.multimediasysteme.grpc.persistence.PersistenceGrpcKt
import jku.multimediasysteme.shared.jpa.transcription.model.SmartSession
import jku.multimediasysteme.shared.jpa.transcription.model.Summary
import jku.multimediasysteme.shared.jpa.transcription.model.Transcription
import jku.multimediasysteme.shared.jpa.transcription.repository.SmartSessionRepository
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
    private val summaryRepository: SummaryRepository,
    private val smartSessionRepository: SmartSessionRepository
) : PersistenceGrpcKt.PersistenceCoroutineImplBase() {

    override suspend fun persistTranscript(requests: Flow<Chunk>): Empty {
        val chunks = requests.toList()
        if (chunks.isEmpty()) {
            throw Status.FAILED_PRECONDITION
                .withDescription("No chunks received")
                .asException()
        }

        val first = chunks.first()
        val last = chunks.last()

        val duration = last.time - first.time
        val text = chunks.joinToString(" ") { it.text }
        val id = UUID.fromString(last.id)
        val userId = UUID.fromString(last.userId)
        val transcription = Transcription(id, userId, text, System.currentTimeMillis(), duration)

        withContext(Dispatchers.IO) {
            transcriptionRepository.save(transcription)
            upsertSmartSession(transcription = transcription)
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
        val first = chunks.first()
        val last = chunks.last()

        val duration = last.time - first.time
        val text = chunks.joinToString(" ") { it.text }
        val id = UUID.fromString(last.id)
        val userId = UUID.fromString(last.userId)
        val summary = Summary(id, userId, text, System.currentTimeMillis(), duration)

        withContext(Dispatchers.IO) {
            summaryRepository.save(summary)
            upsertSmartSession(summary = summary)
        }

        return Empty.getDefaultInstance()
    }

    fun upsertSmartSession(transcription: Transcription? = null, summary: Summary? = null) {
        val userId = transcription?.userId ?: summary!!.userId

        val existing = smartSessionRepository.findAllByUserId(userId)
            .firstOrNull { session ->
                (transcription != null && session.transcription?.id == transcription.id) ||
                        (summary != null && session.summary?.id == summary.id)
            }

        val session = existing ?: SmartSession(userId = userId)

        transcription?.let { session.transcription = it }
        summary?.let { session.summary = it }

        smartSessionRepository.save(session)
    }
}
