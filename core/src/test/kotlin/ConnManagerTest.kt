import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import tech.archlinux.githubStarManager.data.remote.OpenAIService
import tech.archlinux.githubStarManager.sql.ConnManager
import tech.archlinux.githubStarManager.sql.ConnManager.searchRepo
import kotlin.test.Test

class ConnManagerTest {
    @Test
    fun testConnManager() {
        ConnManager.createTable(3072)
    }

    @Test
    fun testDropTable() {
        ConnManager.dropTable()
    }

    @Test
    fun `RAG Search test`() {

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

        val ai = OpenAIService(
            model = "gpt-4o-mini",
            embeddingModel = "text-embedding-3-large",
            client = baseClient,
            apiKey = System.getenv("OAPI_KEY"),
            baseUrl = "https://oapi.baka.plus/v1"
        )

        runBlocking {
            val embedding = ai.createEmbeddings("列出所有包括无线电的内容")
            searchRepo(embedding)
        }
    }
}