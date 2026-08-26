package com.cinenova.app.data.remote

import com.cinenova.app.data.remote.dto.ApiEnvelope
import com.cinenova.app.data.remote.dto.ResourceResponseDto
import com.cinenova.app.data.remote.dto.SearchSuggestResponseDto
import com.cinenova.app.data.remote.dto.SubjectDetailResponseDto
import com.cinenova.app.data.remote.dto.TabOperatingDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the MovieBox v3 mobile BFF.
 *
 * All requests flow through:
 *  1. [HostFailoverInterceptor] — primary host → api5 → api4
 *  2. [RequestAuthInterceptor]  — injectable signing headers (placeholder)
 */
interface MovieBoxApi {

    /** Token bootstrap / home tab configuration. */
    @GET(ApiConfig.PATH_TAB_OPERATING)
    suspend fun tabOperating(
        @Query(ApiConfig.Query.PAGE) page: Int = 1,
        @Query(ApiConfig.Query.TAB_ID) tabId: Int = 0,
        @Query(ApiConfig.Query.VERSION) version: String = "",
    ): Response<ApiEnvelope<TabOperatingDto>>

    @GET(ApiConfig.PATH_SEARCH_SUGGEST)
    suspend fun searchSuggest(
        @Query(ApiConfig.Query.KEYWORD) keyword: String,
        @Query(ApiConfig.Query.PER_PAGE) perPage: Int = 20,
    ): Response<SearchSuggestResponseDto>

    @GET(ApiConfig.PATH_SUBJECT_GET)
    suspend fun getSubject(
        @Query(ApiConfig.Query.SUBJECT_ID) subjectId: Long,
    ): Response<SubjectDetailResponseDto>

    /**
     * Playback resources. Movies: se=0, ep=0. Series: selected season/episode.
     */
    @GET(ApiConfig.PATH_SUBJECT_RESOURCE)
    suspend fun getResource(
        @Query(ApiConfig.Query.SUBJECT_ID) subjectId: Long,
        @Query(ApiConfig.Query.SEASON) season: Int,
        @Query(ApiConfig.Query.EPISODE) episode: Int,
    ): Response<ResourceResponseDto>
}
