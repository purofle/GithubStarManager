import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import tech.archlinux.githubStarManager.data.remote.GithubApiService

fun main() {

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }

        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(System.getenv("GITHUB_TOKEN"), null)
                }
            }
        }

        defaultRequest {
            header("Accept", "application/vnd.github.star+json")
        }
    }

    val api = GithubApiService(client)
    runBlocking {
        val stars = api.listUserStarredRepos()
        println(stars)
    }
}