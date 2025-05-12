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

/**
 * gRPC service for persisting transcriptions and summaries.
 * Receives streaming Chunk data and stores processed results in the database.
 */
@GrpcService
class PersistenceService(
    private val transcriptionRepository: TranscriptionRepository,
    private val summaryRepository: SummaryRepository,
    private val smartSessionRepository: SmartSessionRepository
) : PersistenceGrpcKt.PersistenceCoroutineImplBase() {

    /**
     * Persists a transcription from a stream of text chunks.
     * Builds the final transcription text and saves it to the database.
     *
     * @param requests a flow of Chunk objects representing the streamed transcription
     * @return an empty response if successful
     * @throws io.grpc.StatusException if the input is empty
     */
    override suspend fun persistTranscript(requests: Flow<Chunk>): Empty {
        val chunks = requests.toList()  // Collect all streamed chunks
        if (chunks.isEmpty()) {
            // Return error if stream is empty
            throw Status.FAILED_PRECONDITION
                .withDescription("No chunks received")
                .asException()
        }

        val first = chunks.first()  // First chunk (used to calculate duration)
        val last = chunks.last()    // Last chunk (used to calculate duration + IDs)

        val duration = last.time - first.time                               // Calculate transcription duration
        val text = chunks.joinToString(" ") { it.text }            // Merge all chunk texts
        val id = UUID.fromString(last.transcriptId)                         // Extract transcript ID
        val userId = UUID.fromString(last.userId)                           // Extract user ID
        val transcription = Transcription(                                  // Build entity
            id,
            userId,
            text,
            System.currentTimeMillis(),
            duration
        )

        // Persist data in the database
        withContext(Dispatchers.IO) {
            transcriptionRepository.save(transcription)
            upsertSmartSession(transcriptId = id, transcription = transcription)
        }

        return Empty.getDefaultInstance() // Return empty gRPC response
    }

    /**
     * Persists a summary from a stream of text chunks.
     * Builds the final summary text and saves it to the database.
     *
     * @param requests a flow of Chunk objects representing the streamed summary
     * @return an empty response if successful
     * @throws io.grpc.StatusException if the input is empty
     */
    override suspend fun persistSummary(requests: Flow<Chunk>): Empty {
        val chunks = requests.toList() // Collect all streamed chunks
        if (chunks.isEmpty()) {
            // Return error if stream is empty
            throw Status.FAILED_PRECONDITION
                .withDescription("No chunks received")
                .asException()
        }
        val first = chunks.first()  // First chunk (used to calculate duration)
        val last = chunks.last()    // Last chunk (used to calculate duration + IDs)

        val duration = last.time - first.time                                         // Calculate  duration
        val text = chunks.joinToString(" ") { it.text }                      // Merge all chunk texts
        val id = UUID.fromString(last.summaryId)                                      // Extract Summary ID
        val transcriptId = UUID.fromString(last.transcriptId)                         // Extract Transcription ID
        val userId = UUID.fromString(last.userId)                                     // Extract user ID
        val summary = Summary(id, userId, text, System.currentTimeMillis(), duration) // Build entity


        // Persist data in the database
        withContext(Dispatchers.IO) {
            summaryRepository.save(summary)
            upsertSmartSession(transcriptId = transcriptId, summary = summary)
        }

        return Empty.getDefaultInstance() // Return empty gRPC response
    }

    /**
     * Inserts or updates a SmartSession entity by linking it with the given transcription and/or summary.
     *
     * @param transcriptId the ID of the transcription (used as key for lookup)
     * @param transcription the transcription to associate (nullable)
     * @param summary the summary to associate (nullable)
     */
    fun upsertSmartSession(transcriptId: UUID, transcription: Transcription? = null, summary: Summary? = null) {
        val userId = transcription?.userId ?: summary!!.userId  // Determine user ID from input

        // Try to find existing SmartSession
        val existing = smartSessionRepository.findFirstByUserIdAndTranscriptionId(userId, transcriptId)
        val session = existing.orElse(null) ?: SmartSession(userId = userId) // Reuse or create session

        // Update session fields if provided
        transcription?.let { session.transcription = it }
        summary?.let { session.summary = it }

        smartSessionRepository.save(session) // Persist updated or new session
    }
}
