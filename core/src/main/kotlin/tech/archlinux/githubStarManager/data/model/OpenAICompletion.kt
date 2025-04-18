package tech.archlinux.githubStarManager.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAICompletion(
    val model: String,
    val messages: List<BasicContent>,
    val tools: List<FunctionCall> = listOf(),
)

@Serializable
data class OpenAICompletionResponse(
    val choices: List<CompletionChoice>
)

@Serializable
data class CompletionChoice(
    val index: Int,
    val message: BasicContent,
    @SerialName("finish_reason") val finishReason: String,
)

@Serializable
data class CreateEmbeddings(
    val input: String,
    val model: String
)

@Serializable
data class EmbeddingResponse(
    val data: List<Embedding>
)

@Serializable
data class Embedding(
    val embedding: List<Float>
)