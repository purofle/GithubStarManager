import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import tech.archlinux.githubStarManager.data.model.BasicContent
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
        install(Logging) {
            level = LogLevel.ALL

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
    runBlocking {
        val ai = OpenAIService(
            model = "gemini-2.0-flash",
            client = baseClient,
            apiKey = System.getenv("GEMINI_KEY"),
            baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai"
        )
        GithubApiService(githubAPIClient).listUserStarredRepos().take(20).collect {
            val aiText = "repo name: ${it.repo.fullName}, description: ${it.repo.description}"
            val completion = ai.generateContent(
                listOf(
                    BasicContent(
                        content = "You will play as a GitHub expert. Your role is to summarize the GitHub repository information I give you and summarize it into one sentence. When the information I give you is insufficient, you will use the get_readme function to get the readme.\n" +
                                "In your output, you need to include the full name of the project, followed by your summary. The summary needs to be comprehensive and detailed.",
                        role = "system",
                    ),
                    BasicContent(
                        content = aiText,
                        role = "user",
                    )
                )
            )
            logger.info("Generated content: ${completion.choices.first().message.content}")
        }
    }
}