package jku.multimediasysteme.analytics.data.prompt

import java.util.*

data class PromptRequest(
    val transcriptionId: UUID,
    val prompt: String
)
