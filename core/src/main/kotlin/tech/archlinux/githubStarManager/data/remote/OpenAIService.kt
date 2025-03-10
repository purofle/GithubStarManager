package tech.archlinux.githubStarManager.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import tech.archlinux.githubStarManager.data.model.BasicContent
import tech.archlinux.githubStarManager.data.model.OpenAICompletion
import tech.archlinux.githubStarManager.data.model.OpenAICompletionResponse

class OpenAIService(
    private val model: String,
    var client: HttpClient,
    val baseUrl : String = "https://api.openai.com/v1",
    apiKey: String,
): BaseAIService {

    init {
        // set header
        client = client.config {
            install(Auth) {
                bearer {
                    loadTokens {
                        BearerTokens(apiKey, null)
                    }
                }
            }
        }
    }

    override suspend fun generateContent(messages: List<BasicContent>): OpenAICompletionResponse {
        return client.post("$baseUrl/chat/completions") {
            val body = OpenAICompletion(
                model = model,
                messages = messages
            )
            setBody(body)
        }.body()
    }
}