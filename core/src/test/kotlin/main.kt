import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.r2dbc.spi.Parameter.In
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.archlinux.githubStarManager.data.model.BasicContent
import tech.archlinux.githubStarManager.data.model.functionCall
import tech.archlinux.githubStarManager.data.remote.OpenAIService
import tech.archlinux.githubStarManager.sql.ConnManager

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
    model = "gpt-4o-mini",
    embeddingModel = "text-embedding-3-large",
    client = baseClient,
    apiKey = System.getenv("OAPI_KEY"),
    baseUrl = "https://api.deepseek.com",
    embeddingBaseUrl = "https://oapi.baka.plus/v1"
)

fun main() {
//    val githubAPIClient = baseClient.config {
//        install(Auth) {
//            bearer {
//                loadTokens {
//                    BearerTokens(System.getenv("GITHUB_TOKEN"), null)
//                }
//            }
//        }
//        defaultRequest {
//            header("Accept", "application/vnd.github.star+json")
//        }
//    }

    runBlocking {

        processRepo("purofle/sb", "No description")

//        val repoFlow = GithubApiService(githubAPIClient).listUserStarredRepos()
//        val repoList = ConnManager.getAllRepoName()
//        println(repoFlow.toList().take(500).map { "github.com/${it.repo.fullName}" }.joinToString("\n"))

        ConnManager.close()
    }
}

suspend fun <T> retryWithDelay(
    times: Int = 3,
    initialDelay: Long = 1000L,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            logger.warn("Attempt ${attempt + 1} failed, retrying in $currentDelay ms...", e)
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong() // 指数回退
    }
    return block() // 最后一次尝试
}

suspend fun processRepo(fullName: String, repoDescription: String): String = retryWithDelay {
    val aiText = "repo name: ${fullName}, description: $repoDescription"

    val getReadmeFunction = functionCall {
        name = "get_readme"
        description = "Get the README of a GitHub repository"

        function<String, Int>("repo", "The full name of the repository") { a, b ->

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

    return@retryWithDelay generatedContent
}
