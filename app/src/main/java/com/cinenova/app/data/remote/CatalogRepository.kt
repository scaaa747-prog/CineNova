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
 * Catalog repository backed by the MovieBox v3 API. All calls are suspend and
 * return [ApiResult]; no exceptions escape this layer.
 */
interface CatalogRepository {
    /** Bootstrap: home tab config + (if upstream returns one) bearer token. */
    suspend fun bootstrap(): ApiResult<TabOperatingDto>

    suspend fun search(keyword: String, perPage: Int = 20): ApiResult<List<MediaItem>>

    suspend fun subjectDetail(subjectId: Long): ApiResult<MediaItem>

    suspend fun seasonsOf(subjectId: Long): ApiResult<List<Season>>

    suspend fun castOf(subjectId: Long): ApiResult<List<CastMember>>

    /**
     * Playback sources for a title. Movies use se=0/ep=0.
     * Exposes only metadata + links returned by the upstream resource call.
     */
    suspend fun playbackResources(subjectId: Long, season: Int, episode: Int): ApiResult<PlaybackResources>
}

class MovieBoxCatalogRepository(
    private val api: MovieBoxApi,
) : CatalogRepository {

    override suspend fun bootstrap(): ApiResult<TabOperatingDto> =
        apiCall { api.tabOperating() }.map { envelope ->
            envelope ?: TabOperatingDto()
        }

    override suspend fun search(keyword: String, perPage: Int): ApiResult<List<MediaItem>> =
        apiCall { api.searchSuggest(keyword = keyword, perPage = perPage) }.map { response ->
            response?.subjectsOrEmpty().orEmpty().map { it.toMediaItem() }
        }

    override suspend fun subjectDetail(subjectId: Long): ApiResult<MediaItem> =
        apiCall { api.getSubject(subjectId) }.map { response ->
            // Mapper falls back to flattened top-level fields when `subject` is absent.
            response.toMediaItem(subjectId)
        }

    override suspend fun seasonsOf(subjectId: Long): ApiResult<List<Season>> =
        apiCall { api.getSubject(subjectId) }.map { response ->
            response?.seasons.orEmpty().map { it.toSeason() }
        }

    override suspend fun castOf(subjectId: Long): ApiResult<List<CastMember>> =
        apiCall { api.getSubject(subjectId) }.map { response ->
            response?.resolvedCast().orEmpty().map { it.toCastMember() }
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

    /** Uniform error mapping for all endpoint calls. */
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
