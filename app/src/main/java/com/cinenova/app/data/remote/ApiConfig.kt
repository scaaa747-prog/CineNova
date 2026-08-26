package com.cinenova.app.data.remote

/**
 * Upstream base URLs.
 *
 * Primary host is used first; [FALLBACK_HOSTS] are attempted in order by
 * [HostFailoverInterceptor] when the primary is unreachable or returns 5xx.
 */
object ApiConfig {
    const val PRIMARY_HOST = "api6.aoneroom.com"
    val FALLBACK_HOSTS = listOf("api5.aoneroom.com", "api4.aoneroom.com")
    val ALL_HOSTS = listOf(PRIMARY_HOST) + FALLBACK_HOSTS
    const val H5_HOST = "h5-api.aoneroom.com"

    /** Retrofit requires a base URL; the failover interceptor rewrites hosts. */
    const val BASE_URL = "https://$PRIMARY_HOST/"

    // Endpoint paths (v3 mobile BFF)
    const val PATH_TAB_OPERATING = "wefeed-mobile-bff/tab-operating"
    const val PATH_SEARCH_SUGGEST = "wefeed-mobile-bff/subject-api/search-suggest"
    const val PATH_SUBJECT_GET = "wefeed-mobile-bff/subject-api/get"
    const val PATH_SUBJECT_RESOURCE = "wefeed-mobile-bff/subject-api/resource"

    object Query {
        const val PAGE = "page"
        const val TAB_ID = "tabId"
        const val VERSION = "version"
        const val KEYWORD = "keyword"
        const val PER_PAGE = "perPage"
        const val SUBJECT_ID = "subjectId"
        const val SEASON = "se"
        const val EPISODE = "ep"
    }

    /** Movies use se=0 & ep=0 per upstream convention. */
    const val MOVIE_SE = 0
    const val MOVIE_EP = 0
}
