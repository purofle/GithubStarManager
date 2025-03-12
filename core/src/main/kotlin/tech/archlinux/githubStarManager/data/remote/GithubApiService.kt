package tech.archlinux.githubStarManager.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.archlinux.githubStarManager.data.model.StarredRepo

/**
 * Service to interact with the GitHub API
 * @param client The HTTP client to use. note: authentication is required to access the API
 */
class GithubApiService(
    private val client: HttpClient
) {

    fun listUserStarredRepos(dispatcher: CoroutineDispatcher = Dispatchers.IO): Flow<StarredRepo> = channelFlow {
        // 获取第一页并解析总页数
        val firstResponse = client.get("$BASE_URL/user/starred") {
            parameter("per_page", 30)
            parameter("page", 1)
        }
        val firstPageRepos = firstResponse.body<List<StarredRepo>>()
        val linkHeader = firstResponse.headers["Link"]
        val totalPages = parseTotalPagesFromLinkHeader(linkHeader)

        // 发送第一页数据
        firstPageRepos.forEach { send(it) }

        // 并发请求剩余页
        if (totalPages > 1) {
            logger.info("Total pages: $totalPages")
            (2..totalPages).map { page ->
                launch {
                    val repos = client.get("$BASE_URL/user/starred") {
                        parameter("per_page", 30)
                        parameter("page", page)
                    }.body<List<StarredRepo>>()
                    repos.forEach { send(it) }
                }
            }.joinAll()
        }
    }.flowOn(dispatcher)

    private fun parseTotalPagesFromLinkHeader(linkHeader: String?): Int {
        // 示例解析逻辑，实际需根据 GitHub 的 Link 头格式处理
        val regex = """page=(\d+)>; rel="last"""".toRegex()
        return linkHeader?.let { header ->
            regex.find(header)?.groupValues?.get(1)?.toInt() ?: 1
        } ?: 1
    }

    companion object {
        const val BASE_URL = "https://api.github.com"
        val logger: Logger = LoggerFactory.getLogger(GithubApiService::class.java)
    }
}