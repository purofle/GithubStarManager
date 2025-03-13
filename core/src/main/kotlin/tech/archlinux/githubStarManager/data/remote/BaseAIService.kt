package tech.archlinux.githubStarManager.data.remote

import tech.archlinux.githubStarManager.data.model.BasicContent
import tech.archlinux.githubStarManager.data.model.OpenAICompletionResponse

/**
 * Base class for AI services
 */
interface BaseAIService {
    /**
     * Generate content from a prompt
     * @param messages the prompt to use
     * @return the generated content
     */
    suspend fun generateContent(messages: List<BasicContent>): OpenAICompletionResponse
    suspend fun createEmbeddings(text: String): List<Float>
}