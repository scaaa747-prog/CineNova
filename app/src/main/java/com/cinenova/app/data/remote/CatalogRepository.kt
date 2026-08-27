package com.cinenova.app.data.remote

import android.content.Context
import com.cinenova.app.data.CastMember
import com.cinenova.app.data.MediaItem
import com.cinenova.app.data.Season
import com.cinenova.app.data.remote.dto.TabOperatingDto
import com.cinenova.app.data.remote.mapper.toCastMember
import com.cinenova.app.data.remote.mapper.toMediaItem
import com.cinenova.app.data.remote.mapper.toPlaybackResources
import com.cinenova.app.data.remote.mapper.toSeason
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Resilient Catalog repository backed by MovieBox v3 API with instant 0ms disk cache.
 */
interface CatalogRepository {
    fun initCache(context: Context)
    fun getCachedBootstrap(): TabOperatingDto?
    suspend fun bootstrap(forceRefresh: Boolean = false): ApiResult<TabOperatingDto>
    suspend fun search(keyword: String, perPage: Int = 20): ApiResult<List<MediaItem>>
    suspend fun subjectDetail(subjectId: Long): ApiResult<MediaItem>
    suspend fun seasonsOf(subjectId: Long): ApiResult<List<Season>>
    suspend fun castOf(subjectId: Long): ApiResult<List<CastMember>>
    suspend fun playbackResources(subjectId: Long, season: Int, episode: Int): ApiResult<PlaybackResources>
}

class MovieBoxCatalogRepository(
    private val api: MovieBoxApi,
) : CatalogRepository {

    @Volatile
    private var cachedBootstrap: TabOperatingDto? = null
    private var cacheFile: File? = null
    private val gson = Gson()

    override fun initCache(context: Context) {
        try {
            val file = File(context.cacheDir, "home_feed_cache.json")
            cacheFile = file
            if (file.exists() && cachedBootstrap == null) {
                val json = file.readText()
                if (json.isNotBlank()) {
                    cachedBootstrap = gson.fromJson(json, TabOperatingDto::class.java)
                }
            }
        } catch (_: Exception) {}
    }

    override fun getCachedBootstrap(): TabOperatingDto? = cachedBootstrap

    override suspend fun bootstrap(forceRefresh: Boolean): ApiResult<TabOperatingDto> {
        if (!forceRefresh && cachedBootstrap != null) {
            // Return cached feed immediately
            return ApiResult.Success(cachedBootstrap!!)
        }

        val result = apiCall { api.tabOperating() }
        return when (result) {
            is ApiResult.Success -> {
                val data = result.value.data ?: TabOperatingDto()
                cachedBootstrap = data
                persistCache(data)
                ApiResult.Success(data)
            }
            is ApiResult.HttpError -> {
                cachedBootstrap?.let { ApiResult.Success(it) } ?: result
            }
            is ApiResult.NetworkError -> {
                cachedBootstrap?.let { ApiResult.Success(it) } ?: result
            }
            ApiResult.Empty -> {
                cachedBootstrap?.let { ApiResult.Success(it) } ?: ApiResult.Empty
            }
        }
    }

    private fun persistCache(data: TabOperatingDto) {
        try {
            cacheFile?.let { file ->
                val json = gson.toJson(data)
                file.writeText(json)
            }
        } catch (_: Exception) {}
    }

    override suspend fun search(keyword: String, perPage: Int): ApiResult<List<MediaItem>> =
        apiCall { api.searchSuggest(keyword = keyword, perPage = perPage) }.map { response ->
            response?.subjectsOrEmpty().orEmpty().map { it.toMediaItem() }
        }

    override suspend fun subjectDetail(subjectId: Long): ApiResult<MediaItem> =
        apiCall { api.getSubject(subjectId) }.map { response ->
            response.toMediaItem(subjectId)
        }

    override suspend fun seasonsOf(subjectId: Long): ApiResult<List<Season>> =
        apiCall { api.getSubject(subjectId) }.map { response ->
            val detail = response?.resolvedDetail()
            val seasons = detail?.resolvedSeasonsList() ?: (response?.seasons).orEmpty().map { it.toSeason() }
            if (seasons.isNotEmpty()) seasons
            else {
                listOf(
                    Season(
                        number = 1,
                        episodes = (1..10).map { epNum ->
                            com.cinenova.app.data.Episode(
                                id = "1-$epNum",
                                seasonNumber = 1,
                                episodeNumber = epNum,
                                title = "Episode $epNum",
                                runtimeMinutes = 45,
                                description = "Season 1 Episode $epNum",
                                thumbnailUrl = "",
                            )
                        }
                    )
                )
            }
        }

    override suspend fun castOf(subjectId: Long): ApiResult<List<CastMember>> =
        apiCall { api.getSubject(subjectId) }.map { response ->
            (response?.resolvedDetail()?.resolvedCast() ?: emptyList()).map { it.toCastMember() }
        }

    override suspend fun playbackResources(
        subjectId: Long,
        season: Int,
        episode: Int,
    ): ApiResult<PlaybackResources> =
        apiCall { api.getResource(subjectId, season, episode) }.map { response ->
            response?.toPlaybackResources(subjectId, season, episode)
                ?: PlaybackResources(subjectId, season, episode, emptyList())
        }

    private inline fun <T> apiCall(block: () -> retrofit2.Response<T>): ApiResult<T> = try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body == null) ApiResult.Empty else ApiResult.Success(body)
        } else {
            ApiResult.HttpError(response.code(), response.errorBody()?.string())
        }
    } catch (e: IOException) {
        ApiResult.NetworkError(e)
    } catch (e: Exception) {
        ApiResult.NetworkError(e)
    }
}
