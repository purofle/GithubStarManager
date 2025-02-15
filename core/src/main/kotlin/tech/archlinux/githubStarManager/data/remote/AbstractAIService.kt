package tech.archlinux.githubStarManager.data.remote

import io.ktor.client.*

/**
 * Abstract class for AI services
 * @param model the model to use
 * @param client the http client to use
 */
abstract class AbstractAIService(
    val model: String,
    val apiKey: String,
    val client: HttpClient,
) {
    /**
     * Generate content from a prompt
     * @param prompt the prompt to use
     * @return the generated content
     */
    abstract suspend fun generateContent(prompt: String, systemInstruction: String = ""): String
}