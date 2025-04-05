import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.archlinux.githubStarManager.data.model.BasicContent
import tech.archlinux.githubStarManager.data.model.functionCall
import tech.archlinux.githubStarManager.data.remote.OpenAIService

val logger: Logger = LoggerFactory.getLogger("main")

val baseClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
    install(Logging) {
        level = LogLevel.ALL
    }
    defaultRequest {
        header("Content-Type", "application/json")
    }
}

val ai = OpenAIService(
    model = "deepseek-chat",
    embeddingModel = "text-embedding-3-large",
    client = baseClient,
    apiKey = System.getenv("OAPI_KEY"),
    baseUrl = "https://api.deepseek.com",
    embeddingBaseUrl = "https://oapi.baka.plus/v1"
)

fun main() {
    runBlocking {
        processRepo("purofle/sb", "No description")
    }
}

suspend fun processRepo(fullName: String, repoDescription: String): String {
    val aiText = "repo name: ${fullName}, description: $repoDescription"

    val getReadmeFunction = functionCall {
        name = "get_readme"
        description = "Get the README of a GitHub repository"

        function<String> {
            // put your implementation here
            val repo: String = param { name = "repo"; description = "the repo description" }
            logger.info("get_readme function called with repo: $repo")

            return@function "there is nothing"
        }
    }

    val completion = ai.generateContent(
        listOf(
            BasicContent(
                content = "你将要扮演一个GitHub专家，为你点过Star的项目进行分类，你可以选择将项目归类到一个或多个类别中，也可以选择不归类。" +
                        "我将会提供给你项目的全名和描述。当你没有足够的信息进行分类时候，你可以调用 get_readme 以获取项目的README。",
                role = "system",
            ),
            BasicContent(
                content = aiText,
                role = "user",
            )
        ),
        tools = listOf(getReadmeFunction)
    )

    val generatedContent = completion.choices.first().message.content ?: "No content generated"
    logger.info("generated content: $generatedContent")

    return generatedContent
}
