package jku.multimediasysteme.analytics.data.prompt

import java.util.*

data class PromptRequest(
    val smartSessionIds: List<UUID>,
    val prompt: String
)