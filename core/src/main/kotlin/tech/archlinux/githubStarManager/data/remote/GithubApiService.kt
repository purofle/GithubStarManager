package tech.archlinux.githubStarManager.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import tech.archlinux.githubStarManager.data.model.StarredRepo

/**
 * Service to interact with the GitHub API
 * @param client The HTTP client to use. note: authentication is required to access the API
 */
class GithubApiService(
    private val client: HttpClient
) {

    suspend fun listUserStarredRepos(): List<StarredRepo> {
        return client.get("$BASE_URL/user/starred").body()
    }

    companion object {
        const val BASE_URL = "https://api.github.com"
    }
}