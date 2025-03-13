package tech.archlinux.githubStarManager.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class StarredRepo(
    @SerialName("starred_at") val starredAt: String,
    val repo: Repo
)

@Serializable
data class Repo(
    val id: Int,
    val name: String,
    @SerialName("full_name") val fullName: String,
    val owner: Owner,
    val description: String? = "",
)