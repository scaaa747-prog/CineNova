package com.cinenova.app.data.remote.mapper

import com.cinenova.app.data.CastMember
import com.cinenova.app.data.DubOption
import com.cinenova.app.data.Episode
import com.cinenova.app.data.MediaItem
import com.cinenova.app.data.MediaType
import com.cinenova.app.data.Season
import com.cinenova.app.data.remote.PlaybackResources
import com.cinenova.app.data.remote.StreamResource
import com.cinenova.app.data.remote.SubtitleTrack
import com.cinenova.app.data.remote.dto.CastMemberDto
import com.cinenova.app.data.remote.dto.EpisodeDto
import com.cinenova.app.data.remote.dto.ResourceResponseDto
import com.cinenova.app.data.remote.dto.SeasonDto
import com.cinenova.app.data.remote.dto.SubjectDetailDto
import com.cinenova.app.data.remote.dto.SubjectDetailResponseDto
import com.cinenova.app.data.remote.dto.SubjectSummaryDto

private const val CATEGORY_MOVIE = 1
private const val CATEGORY_TV = 2

fun SubjectSummaryDto.toMediaItem(): MediaItem = MediaItem(
    id = resolvedId().ifEmpty { resolvedTitle() },
    title = resolvedTitle(),
    year = resolvedYear(),
    rating = resolvedRating(),
    ageRating = "NR",
    runtimeMinutes = (seconds ?: durationSeconds ?: 0) / 60,
    genres = resolvedGenres(),
    description = description.orEmpty(),
    posterUrl = resolvedPoster() ?: "",
    backdropUrl = resolvedBackdrop() ?: resolvedPoster() ?: "",
    type = (subjectType ?: resolvedCategory()).toMediaTypeOrDefault(),
)

fun SubjectDetailDto.toMediaItem(): MediaItem = MediaItem(
    id = resolvedId(),
    title = resolvedTitle(),
    year = resolvedYear(),
    rating = resolvedRating(),
    ageRating = resolvedAgeRating(),
    runtimeMinutes = resolvedRuntime(),
    genres = resolvedGenres(),
    description = resolvedDescription(),
    posterUrl = resolvedPoster() ?: "",
    backdropUrl = resolvedBackdrop() ?: resolvedPoster() ?: "",
    type = (subjectType ?: resolvedCategory()).toMediaTypeOrDefault(),
    dubs = dubs.orEmpty().mapNotNull { d ->
        val sId = d.subjectId ?: return@mapNotNull null
        DubOption(subjectId = sId, languageName = d.lanName ?: d.lan ?: "Dub")
    },
)

fun SubjectDetailResponseDto.toMediaItem(subjectId: Long): MediaItem {
    val detail = resolvedDetail()
    return detail?.toMediaItem() ?: MediaItem(
        id = subjectId.toString(),
        title = "",
        year = 0,
        rating = 0.0,
        ageRating = "NR",
        runtimeMinutes = 0,
        genres = emptyList(),
        description = "",
        posterUrl = "",
        backdropUrl = "",
        type = MediaType.MOVIE,
    )
}

fun CastMemberDto.toCastMember(): CastMember = CastMember(
    name = name.orEmpty(),
    role = resolvedRole(),
    avatarUrl = resolvedAvatar() ?: "",
)

fun SeasonDto.toSeason(): Season = Season(
    number = resolvedNumber(),
    episodes = episodes.orEmpty().map { it.toEpisode(resolvedNumber()) },
)

fun EpisodeDto.toEpisode(seasonNumber: Int): Episode = Episode(
    id = "$seasonNumber-${resolvedNumber()}",
    seasonNumber = seasonNumber,
    episodeNumber = resolvedNumber(),
    title = title ?: "Episode ${resolvedNumber()}",
    runtimeMinutes = resolvedRuntime(),
    description = description.orEmpty(),
    thumbnailUrl = resolvedThumb() ?: "",
)

fun ResourceResponseDto.toPlaybackResources(
    subjectId: Long,
    season: Int,
    episode: Int,
): PlaybackResources {
    val items = itemsOrEmpty()
    val sources = items.mapNotNull { item ->
        item.resolvedUrl()?.let { url ->
            val subs = item.extCaptions.orEmpty().mapNotNull { c ->
                val cUrl = c.url ?: return@mapNotNull null
                SubtitleTrack(
                    language = c.lan.orEmpty(),
                    languageName = c.lanName ?: c.lan ?: "Subtitle",
                    url = cUrl,
                )
            }
            StreamResource(
                url = url,
                qualityLabel = item.resolvedQualityLabel(),
                sizeBytes = item.resolvedSizeBytes(),
                format = item.format ?: item.codecName,
                season = item.season ?: season,
                episode = item.episode ?: episode,
                subtitles = subs,
            )
        }
    }.distinctBy { it.url }

    return PlaybackResources(
        subjectId = subjectId,
        season = season,
        episode = episode,
        sources = sources,
    )
}

private fun Int?.toMediaTypeOrDefault(): MediaType = when (this) {
    CATEGORY_TV -> MediaType.TV
    else -> MediaType.MOVIE
}
