package com.cinenova.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/*
 * Wire DTOs for the MovieBox v3 mobile BFF.
 *
 * NOTE: Upstream payloads are loosely structured and may nest data under
 * envelope keys (e.g. `data`, `list`, `items`). Mappers in
 * [com.cinenova.app.data.remote.mapper] extract defensively; adjust
 * @SerializedName values after inspecting real responses — no other layer
 * needs to change.
 */

// ---------- Envelope ----------
data class ApiEnvelope<T>(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val message: String? = null,
    @SerializedName("data") val data: T? = null,
)

// ---------- Bootstrap / tab-operating ----------
data class TabOperatingDto(
    @SerializedName("tabs") val tabs: List<TabDto>? = null,
    @SerializedName("token") val token: String? = null,
    @SerializedName("banners") val banners: List<BannerDto>? = null,
)

data class TabDto(
    @SerializedName("tabId") val tabId: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("subjects") val subjects: List<SubjectSummaryDto>? = null,
)

data class BannerDto(
    @SerializedName("subjectId") val subjectId: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("coverUrl") val coverUrl: String? = null,
    @SerializedName("backdropUrl") val backdropUrl: String? = null,
    @SerializedName("jumpUrl") val jumpUrl: String? = null,
)

// ---------- Search suggest ----------
data class SearchSuggestResponseDto(
    @SerializedName("list") val list: List<SubjectSummaryDto>? = null,
    @SerializedName("items") val items: List<SubjectSummaryDto>? = null,
    @SerializedName("data") val data: List<SubjectSummaryDto>? = null,
) {
    /** First non-null collection wins. */
    fun subjectsOrEmpty(): List<SubjectSummaryDto> =
        (list ?: items ?: data).orEmpty()
}

data class SubjectSummaryDto(
    @SerializedName("subjectId") val subjectId: Long? = null,
    @SerializedName("id") val id: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("releaseYear") val releaseYear: Int? = null,
    @SerializedName("score") val score: Double? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("coverUrl") val coverUrl: String? = null,
    @SerializedName("category") val category: Int? = null,
    @SerializedName("typeName") val typeName: String? = null,
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("duration") val durationMinutes: Int? = null,
) {
    fun resolvedId(): Long = subjectId ?: id ?: 0L
    fun resolvedTitle(): String = title ?: name.orEmpty()
    fun resolvedYear(): Int = year ?: releaseYear ?: 0
    fun resolvedRating(): Double = score ?: rating ?: 0.0
    fun resolvedPoster(): String? = poster ?: cover ?: coverUrl
}

// ---------- Subject detail ----------
data class SubjectDetailResponseDto(
    @SerializedName("subject") val subject: SubjectDetailDto? = null,
    @SerializedName("seasons") val seasons: List<SeasonDto>? = null,
    // Some payloads flatten fields onto the top level:
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("score") val score: Double? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("category") val category: Int? = null,
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("cast") val cast: List<CastMemberDto>? = null,
    @SerializedName("actors") val actors: List<CastMemberDto>? = null,
) {
    fun resolvedCast(): List<CastMemberDto> = cast ?: actors.orEmpty()
    fun resolvedTitle(): String = subject?.title ?: title ?: name.orEmpty()
}

data class SubjectDetailDto(
    @SerializedName("subjectId") val subjectId: Long? = null,
    @SerializedName("id") val id: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("releaseYear") val releaseYear: Int? = null,
    @SerializedName("score") val score: Double? = null,
    @SerializedName("rating") val rating: Double? = null,
    @SerializedName("ageRating") val ageRating: String? = null,
    @SerializedName("mpa") val mpa: String? = null,
    @SerializedName("runtime") val runtimeMinutes: Int? = null,
    @SerializedName("duration") val durationMinutes: Int? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("introduction") val introduction: String? = null,
    @SerializedName("poster") val poster: String? = null,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("backdrop") val backdrop: String? = null,
    @SerializedName("backdropUrl") val backdropUrl: String? = null,
    @SerializedName("category") val category: Int? = null,
    @SerializedName("typeName") val typeName: String? = null,
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("dubbing") val dubbing: List<String>? = null,
) {
    fun resolvedId(): Long = subjectId ?: id ?: 0L
    fun resolvedTitle(): String = title ?: name.orEmpty()
    fun resolvedYear(): Int = year ?: releaseYear ?: 0
    fun resolvedRating(): Double = score ?: rating ?: 0.0
    fun resolvedRuntime(): Int = runtimeMinutes ?: durationMinutes ?: 0
    fun resolvedDescription(): String = description ?: introduction.orEmpty()
    fun resolvedGenres(): List<String> =
        (genres ?: tags)?.takeIf { it.isNotEmpty() }
            ?: typeName?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
}

data class CastMemberDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("character") val character: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("photo") val photo: String? = null,
) {
    fun resolvedRole(): String = character ?: role.orEmpty()
    fun resolvedAvatar(): String? = avatar ?: photo
}

data class SeasonDto(
    @SerializedName("season") val seasonNumber: Int? = null,
    @SerializedName("se") val se: Int? = null,
    @SerializedName("episodes") val episodes: List<EpisodeDto>? = null,
) {
    fun resolvedNumber(): Int = seasonNumber ?: se ?: 1
}

data class EpisodeDto(
    @SerializedName("episode") val episodeNumber: Int? = null,
    @SerializedName("ep") val ep: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("runtime") val runtimeMinutes: Int? = null,
    @SerializedName("duration") val durationMinutes: Int? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("thumb") val thumb: String? = null,
    @SerializedName("thumbnail") val thumbnail: String? = null,
) {
    fun resolvedNumber(): Int = episodeNumber ?: ep ?: 0
    fun resolvedRuntime(): Int = runtimeMinutes ?: durationMinutes ?: 0
    fun resolvedThumb(): String? = thumb ?: thumbnail
}

// ---------- Resources / playback ----------
data class ResourceResponseDto(
    @SerializedName("resources") val resources: List<ResourceItemDto>? = null,
    @SerializedName("list") val list: List<ResourceItemDto>? = null,
    @SerializedName("qualities") val qualities: List<ResourceItemDto>? = null,
    @SerializedName("episodes") val episodes: List<ResourceEpisodeDto>? = null,
) {
    fun itemsOrEmpty(): List<ResourceItemDto> =
        (resources ?: list ?: qualities).orEmpty()
}

data class ResourceItemDto(
    @SerializedName("resolution") val resolution: String? = null,
    @SerializedName("quality") val quality: String? = null,
    @SerializedName("qualityName") val qualityName: String? = null,
    @SerializedName("resourceLink") val resourceLink: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("link") val link: String? = null,
    @SerializedName("size") val sizeBytes: Long? = null,
    @SerializedName("fileSize") val fileSizeBytes: Long? = null,
    @SerializedName("format") val format: String? = null,
    @SerializedName("codec") val codec: String? = null,
    @SerializedName("se") val season: Int? = null,
    @SerializedName("ep") val episode: Int? = null,
) {
    fun resolvedQualityLabel(): String = qualityName ?: quality ?: resolution.orEmpty()
    fun resolvedUrl(): String? = resourceLink ?: url ?: link
    fun resolvedSizeBytes(): Long? = sizeBytes ?: fileSizeBytes
}

data class ResourceEpisodeDto(
    @SerializedName("se") val season: Int? = null,
    @SerializedName("ep") val episode: Int? = null,
    @SerializedName("resources") val resources: List<ResourceItemDto>? = null,
)
