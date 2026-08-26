package com.cinenova.app.data.remote.mapper

import com.cinenova.app.data.CastMember
import com.cinenova.app.data.Episode
import com.cinenova.app.data.MediaItem
import com.cinenova.app.data.MediaType
import com.cinenova.app.data.Season
import com.cinenova.app.data.remote.PlaybackResources
import com.cinenova.app.data.remote.StreamResource
import com.cinenova.app.data.remote.dto.EpisodeDto
import com.cinenova.app.data.remote.dto.ResourceResponseDto
import com.cinenova.app.data.remote.dto.SeasonDto
import com.cinenova.app.data.remote.dto.SubjectDetailDto
import com.cinenova.app.data.remote.dto.SubjectDetailResponseDto
import com.cinenova.app.data.remote.dto.SubjectSummaryDto

/**
 * Mappers from wire DTOs to the app's existing domain models.
 * All fields are nullable upstream — defaults keep the UI stable.
 */

private const val CATEGORY_MOVIE = 1 // adjust if needed after inspecting payloads

fun SubjectSummaryDto.toMediaItem(): MediaItem = MediaItem(
    id = resolvedId().toString().ifEmpty { resolvedTitle() },
    title = resolvedTitle(),
    year = resolvedYear(),
    rating = resolvedRating(),
    ageRating = "NR",
    runtimeMinutes = durationMinutes ?: 0,
    genres = genres.orEmpty(),
    description = description.orEmpty(),
    type = category.toMediaTypeOrDefault(),
)

fun SubjectDetailDto.toMediaItem(): MediaItem = MediaItem(
    id = resolvedId().toString(),
    title = resolvedTitle(),
    year = resolvedYear(),
    rating = resolvedRating(),
    ageRating = ageRating ?: mpa ?: "NR",
    runtimeMinutes = resolvedRuntime(),
    genres = resolvedGenres(),
    description = resolvedDescription(),
    type = category.toMediaTypeOrDefault(),
)

/** Flattened detail response → domain model. */
fun SubjectDetailResponseDto.toMediaItem(subjectId: Long): MediaItem {
    val detail = subject
    return MediaItem(
        id = (detail?.resolvedId() ?: subjectId).toString(),
        title = resolvedTitle(),
        year = detail?.resolvedYear() ?: year ?: 0,
        rating = detail?.resolvedRating() ?: score ?: 0.0,
        ageRating = detail?.ageRating ?: detail?.mpa ?: "NR",
        runtimeMinutes = detail?.resolvedRuntime() ?: 0,
        genres = detail?.resolvedGenres() ?: genres.orEmpty(),
        description = detail?.resolvedDescription() ?: description.orEmpty(),
        type = (detail?.category ?: category).toMediaTypeOrDefault(),
    )
}

fun CastMemberDto.toCastMember(): CastMember = CastMember(
    name = name.orEmpty(),
    role = resolvedRole(),
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
)

/**
 * Resource response → [com.cinenova.app.data.remote.PlaybackResources].
 * Only metadata + links already returned by upstream are surfaced; nothing
 * here modifies or circumvents access controls.
 */
fun ResourceResponseDto.toPlaybackResources(
    subjectId: Long,
    season: Int,
    episode: Int,
): com.cinenova.app.data.remote.PlaybackResources {
    val direct = itemsOrEmpty()
    val fromEpisodes = episodes
        .orEmpty()
        .filter { matchesEpisode(it.season, it.episode, season, episode) }
        .flatMap { it.resources.orEmpty() }

    val sources = (direct + fromEpisodes)
        .mapNotNull { item ->
            item.resolvedUrl()?.let { url ->
                StreamResource(
                    url = url,
                    qualityLabel = item.resolvedQualityLabel(),
                    sizeBytes = item.resolvedSizeBytes(),
                    format = item.format ?: item.codec,
                    season = item.season ?: season,
                    episode = item.episode ?: episode,
                )
            }
        }
        .distinctBy { it.url }

    return com.cinenova.app.data.remote.PlaybackResources(
        subjectId = subjectId,
        season = season,
        episode = episode,
        sources = sources,
    )
}

private fun matchesEpisode(actualSe: Int?, actualEp: Int?, wantedSe: Int, wantedEp: Int): Boolean =
    (actualSe == null || actualSe == wantedSe) && (actualEp == null || actualEp == wantedEp)

private fun Int?.toMediaTypeOrDefault(): MediaType = when (this) {
    CATEGORY_MOVIE -> MediaType.MOVIE
    null -> MediaType.MOVIE
    else -> MediaType.TV
}
