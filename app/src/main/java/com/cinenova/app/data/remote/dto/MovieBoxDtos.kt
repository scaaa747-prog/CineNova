package com.cinenova.app.data.remote.dto

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

/*
 * Wire DTOs for the MovieBox v3 mobile BFF.
 */

// ---------- Envelope ----------
data class ApiEnvelope<T>(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("msg") val message: String? = null,
    @SerializedName("data") val data: T? = null,
)

// ---------- Image Wrapper ----------
@JsonAdapter(ImageDtoDeserializer::class)
data class ImageDto(
    @SerializedName("url") val url: String? = null,
)

class ImageDtoDeserializer : JsonDeserializer<ImageDto> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): ImageDto {
        if (json == null || json.isJsonNull) return ImageDto(null)
        if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
            return ImageDto(json.asString)
        }
        if (json.isJsonObject) {
            val obj = json.asJsonObject
            val url = obj.get("url")?.takeIf { it.isJsonPrimitive }?.asString
                ?: obj.get("coverUrl")?.takeIf { it.isJsonPrimitive }?.asString
                ?: obj.get("backdropUrl")?.takeIf { it.isJsonPrimitive }?.asString
            return ImageDto(url)
        }
        return ImageDto(null)
    }
}

// ---------- Bootstrap / tab-operating ----------
data class TabOperatingDto(
    @SerializedName("tabId") val tabId: Long? = null,
    @SerializedName("items") val items: List<TabSectionDto>? = null,
    @SerializedName("tabs") val tabs: List<TabDto>? = null,
    @SerializedName("token") val token: String? = null,
)

data class TabSectionDto(
    @SerializedName("type") val type: String? = null,
    @SerializedName("position") val position: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("subjects") val subjects: List<SubjectSummaryDto>? = null,
    @SerializedName("banner") val banner: BannerContainerDto? = null,
)

data class BannerContainerDto(
    @SerializedName("banners") val banners: List<BannerItemDto>? = null,
)

data class BannerItemDto(
    @SerializedName("subjectId") val subjectId: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("image") val image: ImageDto? = null,
    @SerializedName("subject") val subject: SubjectSummaryDto? = null,
)

data class TabDto(
    @SerializedName("tabId") val tabId: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("subjects") val subjects: List<SubjectSummaryDto>? = null,
)

// ---------- Search suggest / Subject summary ----------
data class SearchSuggestResponseDto(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SearchSuggestDataDto? = null,
    @SerializedName("items") val items: List<SearchSuggestItemDto>? = null,
) {
    fun subjectsOrEmpty(): List<SubjectSummaryDto> {
        val fromData = data?.items?.mapNotNull { it.subject ?: it.toSubjectSummary() }
        val fromItems = items?.mapNotNull { it.subject ?: it.toSubjectSummary() }
        return fromData ?: fromItems.orEmpty()
    }
}

data class SearchSuggestDataDto(
    @SerializedName("items") val items: List<SearchSuggestItemDto>? = null,
)

data class SearchSuggestItemDto(
    @SerializedName("word") val word: String? = null,
    @SerializedName("subject") val subject: SubjectSummaryDto? = null,
    @SerializedName("subjectId") val subjectId: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("cover") val cover: ImageDto? = null,
) {
    fun toSubjectSummary(): SubjectSummaryDto? {
        val t = title ?: word ?: return null
        return SubjectSummaryDto(
            subjectId = subjectId,
            title = t,
            cover = cover,
        )
    }
}

data class SubjectSummaryDto(
    @SerializedName("subjectId") val subjectId: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("score") val score: JsonElement? = null,
    @SerializedName("rating") val rating: JsonElement? = null,
    @SerializedName("imdbRatingValue") val imdbRatingValue: String? = null,
    @SerializedName("imdbRate") val imdbRate: String? = null,
    @SerializedName("cover") val cover: ImageDto? = null,
    @SerializedName("poster") val poster: ImageDto? = null,
    @SerializedName("subjectType") val subjectType: Int? = null,
    @SerializedName("category") val category: JsonElement? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("seconds") val seconds: Int? = null,
    @SerializedName("durationSeconds") val durationSeconds: Int? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("countryName") val countryName: String? = null,
) {
    fun resolvedId(): String = subjectId ?: id.orEmpty()
    fun resolvedTitle(): String = title ?: name.orEmpty()
    fun resolvedYear(): Int = releaseDate?.take(4)?.toIntOrNull() ?: year ?: 0
    fun resolvedRating(): Double {
        val rateStr = imdbRatingValue ?: imdbRate
        if (!rateStr.isNullOrBlank()) {
            rateStr.toDoubleOrNull()?.let { return it }
        }
        score?.let {
            if (it.isJsonPrimitive) it.asString.toDoubleOrNull()?.let { s -> return s }
        }
        rating?.let {
            if (it.isJsonPrimitive) it.asString.toDoubleOrNull()?.let { r -> return r }
        }
        return 0.0
    }
    fun resolvedPoster(): String? = cover?.url ?: poster?.url
    fun resolvedBackdrop(): String? = cover?.url ?: poster?.url
    fun resolvedGenres(): List<String> =
        genre?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?: genres.orEmpty()
    fun resolvedCategory(): Int? = when {
        category == null -> null
        category.isJsonPrimitive && category.asJsonPrimitive.isNumber -> category.asInt
        category.isJsonPrimitive && category.asJsonPrimitive.isString -> category.asString.toIntOrNull()
        else -> null
    }
}

// ---------- Subject detail ----------
data class SubjectDetailResponseDto(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SubjectDetailDto? = null,
    @SerializedName("subject") val subject: SubjectDetailDto? = null,
    @SerializedName("seasons") val seasons: List<SeasonDto>? = null,
) {
    fun resolvedDetail(): SubjectDetailDto? = data ?: subject
}

data class DubDto(
    @SerializedName("subjectId") val subjectId: String? = null,
    @SerializedName("lanName") val lanName: String? = null,
    @SerializedName("lan") val lan: String? = null,
)

data class SubjectDetailDto(
    @SerializedName("subjectId") val subjectId: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("score") val score: JsonElement? = null,
    @SerializedName("rating") val rating: JsonElement? = null,
    @SerializedName("imdbRatingValue") val imdbRatingValue: String? = null,
    @SerializedName("imdbRate") val imdbRate: String? = null,
    @SerializedName("contentRating") val contentRating: String? = null,
    @SerializedName("ageRating") val ageRating: String? = null,
    @SerializedName("mpa") val mpa: String? = null,
    @SerializedName("seconds") val seconds: Int? = null,
    @SerializedName("durationSeconds") val durationSeconds: Int? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("introduction") val introduction: String? = null,
    @SerializedName("cover") val cover: ImageDto? = null,
    @SerializedName("poster") val poster: ImageDto? = null,
    @SerializedName("subjectType") val subjectType: Int? = null,
    @SerializedName("category") val category: JsonElement? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("genres") val genres: List<String>? = null,
    @SerializedName("countryName") val countryName: String? = null,
    @SerializedName("staffList") val staffList: List<CastMemberDto>? = null,
    @SerializedName("cast") val cast: List<CastMemberDto>? = null,
    @SerializedName("actors") val actors: List<CastMemberDto>? = null,
    @SerializedName("seasons") val seasons: List<SeasonDto>? = null,
    @SerializedName("dubs") val dubs: List<DubDto>? = null,
) {
    fun resolvedId(): String = subjectId ?: id.orEmpty()
    fun resolvedTitle(): String = title ?: name.orEmpty()
    fun resolvedYear(): Int = releaseDate?.take(4)?.toIntOrNull() ?: year ?: 0
    fun resolvedRating(): Double {
        val rateStr = imdbRatingValue ?: imdbRate
        if (!rateStr.isNullOrBlank()) {
            rateStr.toDoubleOrNull()?.let { return it }
        }
        score?.let {
            if (it.isJsonPrimitive) it.asString.toDoubleOrNull()?.let { s -> return s }
        }
        rating?.let {
            if (it.isJsonPrimitive) it.asString.toDoubleOrNull()?.let { r -> return r }
        }
        return 0.0
    }
    fun resolvedRuntime(): Int {
        val sec = durationSeconds ?: seconds
        if (sec != null && sec > 0) return sec / 60
        duration?.let { d ->
            var mins = 0
            val hMatch = Regex("(\\d+)\\s*h").find(d)
            val mMatch = Regex("(\\d+)\\s*m").find(d)
            if (hMatch != null) mins += (hMatch.groupValues[1].toIntOrNull() ?: 0) * 60
            if (mMatch != null) mins += (mMatch.groupValues[1].toIntOrNull() ?: 0)
            if (mins > 0) return mins
        }
        return 0
    }
    fun resolvedDescription(): String = description ?: introduction.orEmpty()
    fun resolvedPoster(): String? = cover?.url ?: poster?.url
    fun resolvedBackdrop(): String? = cover?.url ?: poster?.url
    fun resolvedGenres(): List<String> =
        genre?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?: genres.orEmpty()
    fun resolvedCast(): List<CastMemberDto> = staffList ?: cast ?: actors.orEmpty()
    fun resolvedAgeRating(): String = contentRating ?: ageRating ?: mpa ?: "NR"
    fun resolvedCategory(): Int? = when {
        category == null -> null
        category.isJsonPrimitive && category.asJsonPrimitive.isNumber -> category.asInt
        category.isJsonPrimitive && category.asJsonPrimitive.isString -> category.asString.toIntOrNull()
        else -> null
    }
}

data class CastMemberDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("character") val character: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("avatar") val avatar: ImageDto? = null,
    @SerializedName("photo") val photo: ImageDto? = null,
    @SerializedName("staffType") val staffType: Int? = null,
) {
    fun resolvedRole(): String = character?.takeIf { it.isNotBlank() } ?: role.orEmpty()
    fun resolvedAvatar(): String? = avatar?.url ?: photo?.url
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
    @SerializedName("durationSeconds") val durationSeconds: Int? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("thumb") val thumb: ImageDto? = null,
    @SerializedName("cover") val cover: ImageDto? = null,
) {
    fun resolvedNumber(): Int = episodeNumber ?: ep ?: 0
    fun resolvedRuntime(): Int = (durationSeconds ?: 0) / 60
    fun resolvedThumb(): String? = thumb?.url ?: cover?.url
}

// ---------- Resources / playback ----------
data class CaptionDto(
    @SerializedName("lan") val lan: String? = null,
    @SerializedName("lanName") val lanName: String? = null,
    @SerializedName("url") val url: String? = null,
)

data class ResourceResponseDto(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ResourceDataDto? = null,
    @SerializedName("resources") val resources: List<ResourceItemDto>? = null,
    @SerializedName("list") val list: List<ResourceItemDto>? = null,
) {
    fun itemsOrEmpty(): List<ResourceItemDto> =
        (data?.list ?: data?.resources ?: resources ?: list).orEmpty()
}

data class ResourceDataDto(
    @SerializedName("list") val list: List<ResourceItemDto>? = null,
    @SerializedName("resources") val resources: List<ResourceItemDto>? = null,
    @SerializedName("subjectId") val subjectId: String? = null,
    @SerializedName("subjectTitle") val subjectTitle: String? = null,
)

data class ResourceItemDto(
    @SerializedName("resolution") val resolution: Any? = null,
    @SerializedName("quality") val quality: String? = null,
    @SerializedName("resourceLink") val resourceLink: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("size") val size: Any? = null,
    @SerializedName("format") val format: String? = null,
    @SerializedName("codecName") val codecName: String? = null,
    @SerializedName("se") val season: Int? = null,
    @SerializedName("ep") val episode: Int? = null,
    @SerializedName("extCaptions") val extCaptions: List<CaptionDto>? = null,
) {
    fun resolvedQualityLabel(): String {
        val q = quality ?: resolution?.toString() ?: ""
        return when {
            q.contains("1080") -> "1080P Full HD"
            q.contains("720") -> "720P HD"
            q.contains("480") -> "480P Data Saver"
            q.isNotBlank() -> q
            else -> "HD"
        }
    }

    fun resolvedUrl(): String? = resourceLink ?: url

    fun resolvedSizeBytes(): Long = when (val s = size) {
        is Number -> s.toLong()
        is String -> s.toLongOrNull() ?: 0L
        else -> 0L
    }
}
