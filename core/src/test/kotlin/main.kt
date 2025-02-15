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
import tech.archlinux.githubStarManager.data.remote.GeminiService
import tech.archlinux.githubStarManager.data.remote.GithubApiService

fun main() {

    val baseClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            header("Content-Type", "application/json")
        }
    }

    val githubAPIClient = baseClient.config {
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

    val api = GithubApiService(githubAPIClient)
    val ai = GeminiService(apiKey = System.getenv("GEMINI_KEY"), client = baseClient)
    runBlocking {
        val stars = api.listUserStarredRepos()
        println(ai.generateContent("你妈死了"))
    }
}