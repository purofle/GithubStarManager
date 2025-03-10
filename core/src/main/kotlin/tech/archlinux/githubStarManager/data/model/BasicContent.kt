package tech.archlinux.githubStarManager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BasicContent(
    val content: String? = null,
    val role: String,
    val name: String? = null,
)
