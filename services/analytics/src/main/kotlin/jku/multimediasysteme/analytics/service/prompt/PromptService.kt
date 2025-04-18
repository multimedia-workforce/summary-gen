package jku.multimediasysteme.analytics.service.prompt

import jku.multimediasysteme.shared.jpa.transcription.repository.SmartSessionRepository
import org.springframework.stereotype.Service
import reactor.core.Disposable
import java.util.*

@Service
class PromptService(
    private val smartSessionRepository: SmartSessionRepository,
    private val deepSeekClient: DeepSeekClient
) {
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
        val sessions = smartSessionRepository.findAllById(smartSessionIds)
        val mergedText = sessions.joinToString("\n---\n") {
            val transcription = it.transcription?.text ?: "[Empty transcriptions]"
            val summary = it.summary?.text ?: "[Empty summaries]"
            "Transcriptions:\n$transcription\nSummaries:\n$summary"
        }

        val blocks = mergedText.chunked(chunkSize)
        val blockSummaries = mutableListOf<String>()

        var currentDisposable: Disposable? = null

        fun processBlock(index: Int) {
            if (index >= blocks.size) {
                val finalPrompt = """
                    You are an assistant for text analysis. Here are summaries and transcriptions:

                    ${blockSummaries.withIndex().joinToString("\n---\n") { (i, s) -> "Block ${i + 1}:\n$s" }}

                    Please summarize them as a whole into a full synthesis.
                """.trimIndent()

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

            val blockPrompt = """
                Please summarize the following block precisely:

                ${blocks[index]}
            """.trimIndent()

            val builder = StringBuilder()
            currentDisposable = deepSeekClient.queryStreamed(
                prompt = blockPrompt,
                onChunk = {
                    builder.append(it)
                    onChunk(it)
                },
                onDone = {
                    blockSummaries += builder.toString()
                    processBlock(index + 1)
                },
                onError = onError,
                model = model,
                temperature = temperature
            )
        }

        processBlock(0)

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