package tech.archlinux.githubStarManager.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        tools: List<FunctionCall>
    ): OpenAICompletionResponse {
        val completion: OpenAICompletionResponse = client.post("$baseUrl/chat/completions") {
            val body = OpenAICompletion(
                model = model,
                messages = messages,
                tools = tools.map {
                    FunctionCall(
                        function = LLMFunction(
                            name = it.function.name,
                            description = it.function.description,
                            parameters = it.function.parameters
                        )
                    )
                }
            )
            logger.debug("Request: {}", Json.encodeToString(body))
            setBody(body)
        }.body()
        logger.debug("Completion: {}", completion)
//        return completion
        if (completion.choices[0].finishReason == "stop") {
            return completion
        }
        if (completion.choices[0].finishReason == "tool_calls") {

            val processedResults = mutableListOf<BasicContent>()

            processedResults.addAll(messages)
            processedResults.add(completion.choices[0].message)

            val functionCall = completion.choices[0].message.toolCalls!!
            functionCall.forEach { f ->
                val functionName = f.function.name
                val functionArgs = f.function.arguments

                logger.debug("FunctionCall: {}", functionName)
                logger.debug("FunctionArgs: {}", functionArgs)

                tools.find { f.function.name == it.function.name }?.let { function ->
                    val argMap = Json.decodeFromString<Map<String, JsonElement>>(functionArgs)
                        .mapValues { (_, value) ->
                            when (value) {
                                is JsonPrimitive -> value.content
                                else -> value.toString()
                            }
                        }
                    val result = function.functionBody(argMap)
                    logger.debug("FunctionResult: {}", result)

                    processedResults.add(
                        BasicContent(
                            content = result.toString(),
                            role = "tool",
                            toolCallId = f.id,
                        )
                    )
                }
            }

            return generateContent(processedResults, tools)
        } else {
            logger.error("Error: ${completion.choices[0].finishReason}")
            throw IllegalStateException("Error: ${completion.choices[0].finishReason}")
        }
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

    companion object {
        val logger: Logger = LoggerFactory.getLogger(OpenAIService::class.java)
    }
}