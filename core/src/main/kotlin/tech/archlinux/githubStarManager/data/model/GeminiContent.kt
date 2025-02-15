package tech.archlinux.githubStarManager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GeminiContent(
    val contents: List<Content>,
    val systemInstruction: Content,
)

@Serializable
data class Content(
    val parts: List<Part>,
    val role: String,
)

@Serializable
data class Part(
    val text: String,
)
