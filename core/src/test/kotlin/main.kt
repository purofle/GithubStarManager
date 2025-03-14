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
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import tech.archlinux.githubStarManager.data.model.BasicContent
import tech.archlinux.githubStarManager.data.remote.GithubApiService
import tech.archlinux.githubStarManager.data.remote.OpenAIService
import tech.archlinux.githubStarManager.sql.ConnManager

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

    runBlocking {
        val ai = OpenAIService(
            model = "gpt-4o-mini",
            embeddingModel = "text-embedding-3-large",
            client = baseClient,
            apiKey = System.getenv("OAPI_KEY"),
            baseUrl = "https://oapi.baka.plus/v1"
        )


        GithubApiService(githubAPIClient).listUserStarredRepos().take(50).collect {
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

            val generatedContent = completion.choices.first().message.content ?: "No content generated"

            logger.info("generated content: $generatedContent")

            val embeddings = ai.createEmbeddings(generatedContent)
            try {
                ConnManager.insertRepo(it.repo.fullName, embeddings)
            } catch (e: PSQLException) {
                logger.error("Error inserting repo", e)
            }
        }
    }

    ConnManager.close()
}