package jku.multimediasysteme.analytics.resources.prompt

import jku.multimediasysteme.analytics.data.prompt.PromptRequest
import jku.multimediasysteme.analytics.data.prompt.PromptResponse
import jku.multimediasysteme.analytics.service.prompt.PromptService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/prompts")
class PromptResource(private val promptService: PromptService) {

    @PostMapping
    suspend fun handlePrompt(@RequestBody request: PromptRequest): PromptResponse {
        return promptService.processPrompt(request)
    }
}