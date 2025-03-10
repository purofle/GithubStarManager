package tech.archlinux.githubStarManager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class OpenAICompletion(
    val model: String,
    val messages: List<BasicContent>,
)

@Serializable
data class OpenAICompletionResponse(
    val choices: List<CompletionChoice>
)

@Serializable
data class CompletionChoice(
    val index: Int,
    val message: BasicContent,
)
