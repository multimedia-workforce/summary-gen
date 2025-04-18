package jku.multimediasysteme.analytics.service.prompt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.Disposable
import java.nio.charset.StandardCharsets

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Component
class DeepSeekClient(
    @Value("\${OPENAI_ENDPOINT}") private val endpoint: String,
    @Value("\${OPENAI_TOKEN}") private val apiToken: String
) {
    private val webClient = WebClient.builder()
        .baseUrl(endpoint)
        .defaultHeader("Authorization", "Bearer $apiToken")
        .defaultHeader("Content-Type", "application/json")
        .build()

    fun queryStreamed(
        prompt: String,
        model: String,
        temperature: Float,
        onChunk: (String) -> Unit,
        onDone: () -> Unit,
        onError: (Throwable) -> Unit,
    ): Disposable {
        println("📡 Endpoint: $endpoint")
        println("🧠 Prompt-Length: ${prompt.length}")
        println("🧠 Prompt-Preview: ${prompt.take(300)}")

        val request = mapOf(
            "model" to model,
            "messages" to listOf(
                mapOf("role" to "system", "content" to "You are an assistant"),
                mapOf("role" to "user", "content" to prompt)
            ),
            "stream" to true,
            "temperature" to temperature
        )

        val mapper = jacksonObjectMapper()

        return webClient.post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .onStatus({ it.isError }) { response ->
                response.bodyToMono(String::class.java).map { body ->
                    println("❌ Error with code: ${response.statusCode()} - $body")
                    RuntimeException("Error: ${response.statusCode()}")
                }
            }
            .bodyToFlux(String::class.java)
            .subscribe({ chunk ->
                try {
                    if (chunk.trim() == "[DONE]") return@subscribe
                    val json = mapper.readTree(chunk.removePrefix("data:").trim())
                    val content = json["choices"]?.firstOrNull()?.get("delta")?.get("content")?.asText()
                    if (!content.isNullOrEmpty()) {
                        onChunk(content)
                    }
                } catch (e: Exception) {
                    println("❌ Error during parsing: ${e.message}")
                }
            }, { error ->
                onError(error)
            }, {
                onDone()
            })
    }
}
