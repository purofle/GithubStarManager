import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import tech.archlinux.githubStarManager.data.remote.GithubApiService
import tech.archlinux.githubStarManager.data.remote.OpenAIService

fun main() {

    val logger = LoggerFactory.getLogger("main")

    val baseClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(Logging)
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
    runBlocking {
        val ai = OpenAIService(
            model = "gemini-2.0-flash",
            client = baseClient,
            apiKey = System.getenv("GEMINI_KEY"),
            baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai"
        )
        val starredRepos = GithubApiService(githubAPIClient).listUserStarredRepos().toList()
        logger.info("Get ${starredRepos.size} starred repos")
    }
}