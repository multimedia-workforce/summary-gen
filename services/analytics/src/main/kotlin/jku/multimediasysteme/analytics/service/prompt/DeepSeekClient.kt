package jku.multimediasysteme.analytics.service.prompt

import jku.multimediasysteme.analytics.data.prompt.deepseek.DeepSeekResponse
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class DeepSeekClient(
    @Value("\${OPENAI_ENDPOINT}") private val endpoint: String,
    @Value("\${OPENAI_TOKEN}") private val apiToken: String,
    @Value("\${OPENAI_MODEL}") private val model: String
) {

    private val webClient = WebClient.builder()
        .baseUrl(endpoint)
        .defaultHeader("Authorization", "Bearer $apiToken")
        .defaultHeader("Content-Type", "application/json")
        .build()

    suspend fun query(fullPrompt: String): String {
        val request = mapOf(
            "model" to model,
            "messages" to listOf(
                mapOf("role" to "system", "content" to "Du bist ein hilfsbereiter Assistent."),
                mapOf("role" to "user", "content" to fullPrompt)
            ),
            "stream" to false
        )

        val response = webClient.post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(DeepSeekResponse::class.java)
            .awaitSingle()

        return response.choices.firstOrNull()?.message?.content?.trim() ?: "Keine Antwort erhalten."
    }
}