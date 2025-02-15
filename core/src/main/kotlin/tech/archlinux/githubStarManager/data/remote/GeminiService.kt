package tech.archlinux.githubStarManager.data.remote

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import tech.archlinux.githubStarManager.data.model.Content
import tech.archlinux.githubStarManager.data.model.GeminiContent
import tech.archlinux.githubStarManager.data.model.Part

class GeminiService(
    model: String = "gemini-2.0-flash",
    apiKey: String,
    client: HttpClient,
) : AbstractAIService(model, apiKey, client) {
    override suspend fun generateContent(prompt: String, systemInstruction: String): String =
        client.post("$BASE_URL/models/$model:generateContent") {
            url {
                parameters.append("key", apiKey)
            }

            setBody(GeminiContent(
                listOf(Content(listOf(Part(prompt)), "user")),
                systemInstruction = Content(listOf(Part(systemInstruction)), "system")
            ))
        }.bodyAsText()

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
    }
}