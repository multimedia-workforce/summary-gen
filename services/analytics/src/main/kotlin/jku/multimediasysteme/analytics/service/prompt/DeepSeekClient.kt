package jku.multimediasysteme.analytics.service.prompt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.Disposable

@Component
class DeepSeekClient(
    @Value("\${OPENAI_ENDPOINT}") private val endpoint: String,
    @Value("\${OPENAI_TOKEN}") private val apiToken: String,
    @Value("\${OPENAI_MODEL}") private val model: String
) {

    private val webClient = WebClient.builder().baseUrl(endpoint).defaultHeader("Authorization", "Bearer $apiToken")
        .defaultHeader("Content-Type", "application/json").build()

    fun queryStreamed(prompt: String, onChunk: (String) -> Unit): Disposable {
        val request = mapOf(
            "model" to model, "messages" to listOf(
                mapOf("role" to "system", "content" to "Du bist ein hilfsbereiter Assistent."),
                mapOf("role" to "user", "content" to prompt)
            ), "stream" to true
        )

        val mapper = jacksonObjectMapper()

        return webClient.post().uri("/chat/completions").bodyValue(request).retrieve().bodyToFlux(String::class.java)
            .subscribe { chunk ->
                try {
                    if (chunk.trim() == "[DONE]") return@subscribe
                    val json = mapper.readTree(chunk.removePrefix("data:").trim())
                    val content = json["choices"]?.firstOrNull()?.get("delta")?.get("content")?.asText()
                    if (!content.isNullOrEmpty()) {
                        onChunk(content)
                    }
                } catch (e: Exception) {
                    println("Fehler beim Parsen: ${e.message}")
                }
            }
    }
}