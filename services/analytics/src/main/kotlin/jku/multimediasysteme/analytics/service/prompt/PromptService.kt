package jku.multimediasysteme.analytics.service.prompt

import jku.multimediasysteme.shared.jpa.transcription.repository.SmartSessionRepository
import org.springframework.stereotype.Service
import reactor.core.Disposable
import java.util.*

/**
 * Service that handles prompt-based analysis of SmartSessions using DeepSeekClient.
 * Allows processing long content in streamed blocks and combining results into a final synthesis.
 */
@Service
class PromptService(
    private val smartSessionRepository: SmartSessionRepository,
    private val deepSeekClient: DeepSeekClient
) {

    /**
     * Streams a prompt-based summarization over selected SmartSessions using the external LLM (e.g. DeepSeek/OpenAI).
     * Supports chunked processing for long content and sends streamed results via callbacks.
     *
     * @param prompt        The user query to be processed.
     * @param smartSessionIds IDs of SmartSessions to include in the analysis.
     * @param model         Model name to use (e.g., "gpt-3.5-turbo").
     * @param temperature   Sampling temperature for the model.
     * @param onChunk       Callback for each streamed content chunk.
     * @param onDone        Callback when all processing is complete.
     * @param onError       Callback for any error encountered during processing.
     * @param chunkSize     Maximum character size for each block (default: 2000).
     */
    fun processPromptStreamed(
        prompt: String,
        smartSessionIds: List<UUID>,
        model: String,
        temperature: Float,
        onChunk: (String) -> Unit,
        onDone: () -> Unit,
        onError: (Throwable) -> Unit,
        chunkSize: Int = 2000
    ): Disposable {
        // Fetch all selected sessions from database
        val sessions = smartSessionRepository.findAllById(smartSessionIds)

        // Merge transcriptions and summaries into a combined prompt text
        val mergedText = sessions.joinToString("\n---\n") {
            val transcription = it.transcription?.text ?: "[Empty transcriptions]"
            val summary = it.summary?.text ?: "[Empty summaries]"
            "Transcriptions:\n$transcription\nSummaries:\n$summary"
        }

        // Break large prompt into smaller manageable blocks
        val blocks = mergedText.chunked(chunkSize)
        // Will collect partial summaries
        val blockSummaries = mutableListOf<String>()
        // Used to dispose stream if needed
        var currentDisposable: Disposable? = null

        /**
         * Recursively processes each block and accumulates summaries.
         */
        fun processBlock(index: Int) {
            if (index >= blocks.size) {
                // When all blocks are processed, build final prompt to synthesize result
                val finalPrompt = """
                    You are an assistant for text analysis. Here are summaries and transcriptions:

                    ${blockSummaries.withIndex().joinToString("\n---\n") { (i, s) -> "Block ${i + 1}:\n$s" }}

                    Please summarize them as a whole into a full synthesis.
                """.trimIndent()

                // Final call to external LLM for overall summary
                currentDisposable = deepSeekClient.queryStreamed(
                    prompt = finalPrompt,
                    model = model,
                    temperature = temperature,
                    onChunk = onChunk,
                    onDone = onDone,
                    onError = onError,
                )

                return
            }

            // Prompt for current block
            val blockPrompt = """
                Please summarize the following block precisely:

                ${blocks[index]}
            """.trimIndent()

            val builder = StringBuilder()

            // Stream response for current block
            currentDisposable = deepSeekClient.queryStreamed(
                prompt = blockPrompt,
                onChunk = {
                    builder.append(it)  // Accumulate partial output
                    onChunk(it)         // Forward chunk to client
                },
                onDone = {
                    blockSummaries += builder.toString() // Save full block summary
                    processBlock(index + 1)        // Proceed to next block
                },
                onError = onError,
                model = model,
                temperature = temperature
            )
        }

        // Start processing from first block
        processBlock(0)

        // Return Disposable to control lifecycle externally if needed
        return object : Disposable {
            override fun dispose() {
                currentDisposable?.dispose()
            }

            override fun isDisposed(): Boolean {
                return currentDisposable?.isDisposed ?: true
            }
        }
    }
}