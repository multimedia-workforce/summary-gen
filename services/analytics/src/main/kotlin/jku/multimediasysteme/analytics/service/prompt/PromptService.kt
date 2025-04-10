package jku.multimediasysteme.analytics.service.prompt

import jku.multimediasysteme.analytics.data.prompt.PromptRequest
import jku.multimediasysteme.analytics.data.prompt.PromptResponse
import jku.multimediasysteme.shared.jpa.transcription.repository.TranscriptionRepository
import org.springframework.stereotype.Service

@Service
class PromptService(
    private val transcriptionRepository: TranscriptionRepository, private val deepSeekClient: DeepSeekClient
) {
    suspend fun processPrompt(request: PromptRequest): PromptResponse {
        val transcription = transcriptionRepository.findById(request.transcriptionId)
            .orElseThrow { IllegalArgumentException("Transcription not found") }

        val fullPrompt = """
            Du bist ein Assistent. Hier ist eine Transkription:
            ---
            ${transcription.summaryText}
            ---
            Aufgabe: ${request.prompt}
        """.trimIndent()

        val result = deepSeekClient.query(fullPrompt)
        return PromptResponse(result = result)
    }
}