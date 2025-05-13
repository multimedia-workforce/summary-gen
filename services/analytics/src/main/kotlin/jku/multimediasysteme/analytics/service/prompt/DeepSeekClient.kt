package jku.multimediasysteme.analytics.service.prompt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.Disposable

/**
 * Client for interacting with a remote LLM (e.g., OpenAI-compatible) via streaming requests.
 * Uses WebClient to send prompts and receive streamed completion results.
 */
@Component
class DeepSeekClient(
    @Value("\${OPENAI_ENDPOINT}") private val endpoint: String,
    @Value("\${OPENAI_TOKEN}") private val apiToken: String
) {
    // Reactive WebClient configured with base URL and headers
    private val webClient = WebClient.builder()
        .baseUrl(endpoint)
        .defaultHeader("Authorization", "Bearer $apiToken")
        .defaultHeader("Content-Type", "application/json")
        .build()

    /**
     * Sends a prompt to the model and streams the response using Server-Sent Events.
     *
     * @param prompt      The input text or query to be processed.
     * @param model       The model name to be used (e.g., "gpt-3.5-turbo").
     * @param temperature Sampling temperature (controls randomness).
     * @param onChunk     Callback to handle each text chunk streamed by the server.
     * @param onDone      Callback when the full response is completed.
     * @param onError     Callback for handling any error during streaming.
     * @return            A Disposable to control the subscription if needed.
     */
    fun queryStreamed(
        prompt: String,
        model: String,
        temperature: Float,
        onChunk: (String) -> Unit,
        onDone: () -> Unit,
        onError: (Throwable) -> Unit,
    ): Disposable {
        // Build the request payload
        val request = mapOf(
            "model" to model,
            "messages" to listOf(
                mapOf("role" to "system", "content" to "You are an assistant"),
                mapOf("role" to "user", "content" to prompt)
            ),
            "stream" to true, // Enable streaming response
            "temperature" to temperature
        )

        val mapper = jacksonObjectMapper()

        // Send POST request and handle streaming response
        return webClient.post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .onStatus({ it.isError }) { response ->
                // If HTTP error, convert body to exception
                response.bodyToMono(String::class.java).map { body ->
                    RuntimeException("Error: ${response.statusCode()}")
                }
            }
            .bodyToFlux(String::class.java)  // Flux emits streamed chunks of the response
            .subscribe({ chunk ->
                try {
                    if (chunk.trim() == "[DONE]") return@subscribe // End-of-stream marker
                    // Remove "data:" prefix and parse JSON
                    val json = mapper.readTree(chunk.removePrefix("data:").trim())
                    val content = json["choices"]?.firstOrNull()?.get("delta")?.get("content")?.asText()
                    if (!content.isNullOrEmpty()) {
                        onChunk(content) // Deliver partial content to client
                    }
                } catch (_: Exception) {
                }
            }, { error -> onError(error) }, { onDone() })
    }
}
