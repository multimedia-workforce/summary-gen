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
    suspend fun processPromptStreamed(
        prompt: String,
        smartSessionIds: List<UUID>,
        onChunk: (String) -> Unit
    ): Disposable? {
        val sessions = smartSessionRepository.findAllById(smartSessionIds)
        if (sessions.isEmpty()) return null

        val mergedText = sessions.joinToString("\n---\n") {
            val transcription = it.transcription?.text ?: "[Leere Transkription]"
            val summary = it.summary?.text ?: "[Keine Zusammenfassung]"
            "Transkription:\n$transcription\nZusammenfassung:\n$summary"
        }

        val fullPrompt = """
        Du bist ein Assistent. Hier ist eine Sammlung von Transkriptionen:
        ---
        $mergedText
        ---
        Aufgabe: $prompt
        """.trimIndent()

        return deepSeekClient.queryStreamed(fullPrompt, onChunk)
    }
}