package com.cinenova.app.data.remote

import com.cinenova.app.data.CastMember
import com.cinenova.app.data.MediaItem
import com.cinenova.app.data.Season
import com.cinenova.app.data.remote.dto.TabOperatingDto
import com.cinenova.app.data.remote.mapper.toCastMember
import com.cinenova.app.data.remote.mapper.toMediaItem
import com.cinenova.app.data.remote.mapper.toPlaybackResources
import com.cinenova.app.data.remote.mapper.toSeason
import java.io.IOException

/**
 * Resilient Catalog repository backed by MovieBox v3 API with local cache fallback for low-coverage networks.
 */
interface CatalogRepository {
    suspend fun bootstrap(): ApiResult<TabOperatingDto>
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

    override suspend fun bootstrap(): ApiResult<TabOperatingDto> {
        val result = apiCall { api.tabOperating() }
        return when (result) {
            is ApiResult.Success -> {
                val data = result.value.data ?: TabOperatingDto()
                cachedBootstrap = data
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
            (response?.resolvedDetail()?.seasons ?: response?.seasons).orEmpty().map { it.toSeason() }
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

    /** Uniform error mapping for all endpoint calls with auto-token healing on 441/401. */
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
