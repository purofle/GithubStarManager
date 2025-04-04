package tech.archlinux.githubStarManager.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import tech.archlinux.githubStarManager.data.model.*

class OpenAIService(
    private val model: String,
    private val embeddingModel: String,
    var client: HttpClient,
    val baseUrl : String = "https://api.openai.com/v1",
    val embeddingBaseUrl: String = baseUrl,
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

    override suspend fun generateContent(
        messages: List<BasicContent>,
        tools: List<LLMFunction>
    ): OpenAICompletionResponse {
        val completion: OpenAICompletionResponse = client.post("$baseUrl/chat/completions") {
            val body = OpenAICompletion(
                model = model,
                messages = messages,
                tools = tools.map {
                    FunctionCall(
                        type = "function",
                        function = it
                    )
                }
            )
            setBody(body)
        }.body()

//        if (completion.choices[0].finishReason == "stop") {
            return completion
//        }
//        if (completion.choices[0].finishReason == "function_call") {
//            val functionCall = completion.choices[0].message.functionCall
//            val functionName = functionCall.name
//            val arguments = functionCall.arguments
//
//            val function = tools.find { it.name == functionName }
//            if (function != null) {
//                val result = function.call(arguments)
//                return generateContent(
//                    messages = listOf(
//                        BasicContent(
//                            role = "user",
//                            content = result
//                        )
//                    )
//                )
//            }
//        }
    }

    override suspend fun createEmbeddings(text: String): List<Float> {
        val response: EmbeddingResponse = client.post("$embeddingBaseUrl/embeddings") {
            setBody(
                CreateEmbeddings(
                    input = text,
                    model = embeddingModel
                )
            )
        }.body()

        return response.data[0].embedding
    }
}