package jku.multimediasysteme.analytics.service.prompt

import jku.multimediasysteme.analytics.data.prompt.PromptRequest
import jku.multimediasysteme.analytics.data.prompt.PromptResponse
import jku.multimediasysteme.shared.jpa.transcription.repository.SmartSessionRepository
import org.springframework.stereotype.Service

@Service
class PromptService(
    private val smartSessionRepository: SmartSessionRepository,
    private val deepSeekClient: DeepSeekClient
) {
    suspend fun processPrompt(request: PromptRequest): PromptResponse {
        val sessions = smartSessionRepository.findAllById(request.smartSessionIds)

        if (sessions.isEmpty()) {
            throw IllegalArgumentException("No SmartSessions found")
        }

        val mergedText = sessions.joinToString("\n---\n") { session ->
            val transcription = session.transcription?.text ?: "[Leere Transkription]"
            val summary = session.summary?.text ?: "[Keine Zusammenfassung]"
            """
            Transkription:
            $transcription
            Zusammenfassung:
            $summary
            """.trimIndent()
        }
        val fullPrompt = """
            Du bist ein Assistent. Hier ist eine Sammlung von Transkriptionen:
            ---
            $mergedText
            ---
            Aufgabe: ${request.prompt}
        """.trimIndent()

        val result = deepSeekClient.query(fullPrompt)
        return PromptResponse(result = result)
    }
}