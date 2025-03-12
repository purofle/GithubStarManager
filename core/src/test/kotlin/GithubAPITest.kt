import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import tech.archlinux.githubStarManager.data.remote.GithubApiService
import kotlin.test.Test
import kotlin.time.measureTime

class GithubAPITest {
    @Test
    fun `Measure the time to get the starred repo`() {
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

        var starredReposSize: Int
        val time = measureTime {
            runBlocking {
                starredReposSize = GithubApiService(githubAPIClient).listUserStarredRepos().toList().size
            }
        }

        println("Fetched $starredReposSize repos in $time")
    }
}