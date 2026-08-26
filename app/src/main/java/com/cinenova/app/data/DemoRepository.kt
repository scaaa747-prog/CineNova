package com.cinenova.app.data

/**
 * Demo catalog. All artwork is generic placeholder imagery (picsum.photos) —
 * no copyrighted posters.
 */
object DemoRepository {

    private fun seasonsFor(id: String, title: String, episodeRuntime: Int): List<Season> =
        (1..3).map { s ->
            Season(
                number = s,
                episodes = (1..6).map { e ->
                    Episode(
                        id = "$id-s${s}e$e",
                        seasonNumber = s,
                        episodeNumber = e,
                        title = "Episode $e",
                        runtimeMinutes = episodeRuntime + (e % 4),
                        description = "Chapter ${s * 10 + e} of \"$title\". The story deepens as new alliances form and old rivalries resurface.",
                    )
                },
            )
        }

    private fun movie(
        id: String, title: String, year: Int, rating: Double, age: String,
        runtime: Int, genres: List<String>, description: String,
    ): MediaItem = MediaItem(id, title, year, rating, age, runtime, genres, description, MediaType.MOVIE)

    private fun tv(
        id: String, title: String, year: Int, rating: Double, age: String,
        episodeRuntime: Int, genres: List<String>, description: String,
    ): Pair<MediaItem, List<Season>> =
        MediaItem(id, title, year, rating, age, episodeRuntime, genres, description, MediaType.TV) to
            seasonsFor(id, title, episodeRuntime)

    val catalog: List<MediaItem>
    private val seasonMap: Map<String, List<Season>>

    init {
        val movies = listOf(
            movie(
                "midnight-horizon", "Midnight Horizon", 2025, 8.7, "PG-13", 128,
                listOf("Sci-Fi", "Thriller", "Adventure"),
                "A deep-space salvage crew answers a distress call from a derelict station orbiting a dying star — and discovers the signal was never meant for them.",
            ),
            movie(
                "the-quiet-tide", "The Quiet Tide", 2024, 8.2, "PG", 112,
                listOf("Drama", "Romance"),
                "Two lighthouse keepers on a remote island share one silent winter that will echo through the rest of their lives.",
            ),
            movie(
                "steel-requiem", "Steel Requiem", 2025, 7.9, "R", 141,
                listOf("Action", "Crime", "Thriller"),
                "A retired getaway driver is pulled back for one final job — but the cargo is not what anyone expected.",
            ),
            movie(
                "paper-lanterns", "Paper Lanterns", 2023, 8.9, "PG", 98,
                listOf("Animation", "Family", "Fantasy"),
                "A young lantern-maker's creations come alive one festival night, leading her on a journey across a city painted in light.",
            ),
            movie(
                "arctic-static", "Arctic Static", 2024, 7.4, "R", 106,
                listOf("Horror", "Sci-Fi"),
                "A radio operator at a polar research station intercepts a broadcast from her own voice — dated six days in the future.",
            ),
            movie(
                "solstice-run", "Solstice Run", 2025, 7.6, "PG-13", 117,
                listOf("Adventure", "Comedy"),
                "Three friends attempt an impossible coast-to-coast bicycle race during the longest heatwave in history.",
            ),
            movie(
                "gravity-well", "Gravity Well", 2022, 8.4, "PG-13", 134,
                listOf("Sci-Fi", "Drama"),
                "An astrophysicist racing to prove wormhole theory must choose between the discovery of a lifetime and the family she left behind.",
            ),
            movie(
                "velvet-thunder", "Velvet Thunder", 2024, 6.9, "R", 122,
                listOf("Action", "Crime"),
                "A heist crew steals a legendary car, only to find its trunk holds evidence capable of toppling a criminal empire.",
            ),
            movie(
                "the-cartographer", "The Cartographer", 2023, 8.6, "PG-13", 125,
                listOf("Adventure", "Drama", "History"),
                "Obsessed with mapping an uncharted river, a surveyor ventures into wilderness that rewrites his understanding of home.",
            ),
            movie(
                "cherry-winter", "Cherry Winter", 2022, 8.1, "PG", 104,
                listOf("Romance", "Drama"),
                "A pastry chef and a violinist meet every February in the same small café — until one of them stops coming.",
            ),
            movie(
                "iron-monsoon", "Iron Monsoon", 2025, 7.2, "PG-13", 119,
                listOf("Action", "Adventure", "Sci-Fi"),
                "In a flooded future, storm-chasing salvagers battle corporate armadas for the last untainted rainwater.",
            ),
            movie(
                "last-train-to-lisbon", "Last Train to Lisbon", 2021, 8.3, "PG", 110,
                listOf("Drama", "Romance"),
                "Strangers share a sleeper car across Europe and discover their lives crossed decades earlier.",
            ),
            movie(
                "neon-verdict", "Neon Verdict", 2023, 7.7, "R", 127,
                listOf("Crime", "Thriller", "Sci-Fi"),
                "In a megacity where memories are admissible evidence, a detective's own recollection becomes the trial of the century.",
            ),
            movie(
                "featherstone", "Featherstone", 2024, 7.5, "PG", 96,
                listOf("Comedy", "Family"),
                "A shy ornithologist accidentally becomes a viral sensation after rescuing a talkative parrot with a secret.",
            ),
        )

        val shows = listOf(
            tv(
                "emberfall", "Emberfall", 2024, 9.1, "TV-MA", 52,
                listOf("Fantasy", "Drama"),
                "In a kingdom where fire is currency, an exiled heir and a disgraced alchemist conspire to reignite a dying dynasty.",
            ),
            tv(
                "hollow-creek", "Hollow Creek", 2025, 8.5, "TV-14", 45,
                listOf("Mystery", "Crime", "Drama"),
                "When a small town's beloved mayor vanishes, a rookie sheriff uncovers secrets buried deeper than the creek itself.",
            ),
            tv(
                "the-gilded-hour", "The Gilded Hour", 2023, 8.8, "TV-MA", 58,
                listOf("Drama", "Crime"),
                "1920s New York: a jazz singer, a corrupt prosecutor, and a stolen fortune collide over a single gilded evening.",
            ),
            tv(
                "northbound", "Northbound", 2024, 8.0, "TV-14", 44,
                listOf("Documentary", "Adventure"),
                "An expedition team documents the last unmapped valley of the far north — and the people who have guarded it for centuries.",
            ),
            tv(
                "signal-lost", "Signal Lost", 2025, 7.8, "TV-MA", 48,
                listOf("Horror", "Mystery", "Thriller"),
                "A podcast host investigating a ghost town starts receiving transmissions from listeners who died years ago.",
            ),
            tv(
                "the-understudy", "The Understudy", 2024, 8.7, "TV-MA", 50,
                listOf("Drama", "Thriller"),
                "A theatre understudy gets her break when the lead actress disappears — and the reviews are murder.",
            ),
        )

        catalog = movies + shows.map { it.first }
        seasonMap = shows.associate { it.first.id to it.second } +
            movies.associate { it.id to emptyList<Season>() }
    }

    private val byId by lazy { catalog.associateBy { it.id } }

    fun item(id: String): MediaItem? = byId[id]

    fun items(ids: List<String>): List<MediaItem> = ids.mapNotNull { byId[it] }

    fun episodesOf(item: MediaItem): List<Season> = seasonMap[item.id].orEmpty()

    val trending get() = catalog.sortedByDescending { it.rating }.take(10)
    val popularMovies get() = catalog.filter { it.type == MediaType.MOVIE }.sortedByDescending { it.rating }
    val popularTv get() = catalog.filter { it.type == MediaType.TV }.sortedByDescending { it.rating }
    val newReleases get() = catalog.filter { it.year >= 2025 }
    val recentlyAdded get() = catalog.reversed()
    val topRated get() = catalog.sortedByDescending { it.rating }
    val recommended get() = catalog.filter { it.genres.any { g -> g in setOf("Sci-Fi", "Thriller") } }
    val becauseYouWatched get() = catalog.filter { it.id != "midnight-horizon" && it.id != "gravity-well" }

    val continueWatching: List<WatchProgress> = listOf(
        WatchProgress("midnight-horizon", 74, 128),
        WatchProgress("emberfall", 31, 52, "S1 · E4 \"Episode 4\""),
        WatchProgress("the-gilded-hour", 12, 58, "S2 · E2 \"Episode 2\""),
        WatchProgress("steel-requiem", 103, 141),
        WatchProgress("hollow-creek", 20, 45, "S1 · E6 \"Episode 6\""),
    )

    val castFor: Map<String, List<CastMember>> = mapOf(
        "midnight-horizon" to listOf(
            CastMember("Ada Reyes", "Commander Vale"),
            CastMember("Jonas Feld", "Chief Engineer Milo"),
            CastMember("Priya Anand", "Dr. Sefu"),
            CastMember("Marco Bell", "Navigator Kesh"),
            CastMember("Lena Ostrov", "Ship AI 'Vesper'"),
        ),
        "emberfall" to listOf(
            CastMember("Cora Blackwood", "Heir Maren"),
            CastMember("Idris Kane", "Alchemist Doran"),
            CastMember("Yuki Tanaka", "Spymaster Orla"),
        ),
    ).withDefault {
        listOf(
            CastMember("Alex Meridian", "Lead"),
            CastMember("Sam Okafor", "Supporting"),
            CastMember("Jamie Calder", "Supporting"),
            CastMember("Riley Voss", "Director"),
        )
    }

    val reviewsFor: Map<String, List<Review>> = mapOf(
        "midnight-horizon" to listOf(
            Review("cinephile_99", 9.0, "Aug 12, 2026", "Gorgeous practical effects and a third act that actually earns its silence."),
            Review("nova_reviews", 8.0, "Jul 30, 2026", "Slow-burn sci-fi done right. A touch long in the middle, but the ending lands."),
        ),
    ).withDefault {
        listOf(
            Review("reel_maven", 8.0, "Jun 2026", "Confident, stylish, and surprisingly heartfelt. A great weekend watch."),
            Review("screen_dose", 7.0, "May 2026", "Not perfect, but the craft on display is undeniable."),
        )
    }

    val trailersFor: Map<String, List<Trailer>> = mapOf(
        "midnight-horizon" to listOf(Trailer("Official Teaser", 2), Trailer("Final Trailer", 3)),
    ).withDefault { listOf(Trailer("Official Trailer", 2)) }

    val notifications: List<AppNotification> = listOf(
        AppNotification("n1", NotificationKind.NEW_RELEASE, "New release", "\"Iron Monsoon\" is now streaming.", "2h ago", true),
        AppNotification("n2", NotificationKind.NEW_EPISODE, "New episode", "Emberfall S3 · E6 is available.", "5h ago", true),
        AppNotification("n3", NotificationKind.DOWNLOAD_COMPLETE, "Download complete", "Midnight Horizon finished downloading.", "1d ago", false),
        AppNotification("n4", NotificationKind.RECOMMENDATION, "Recommended for you", "Because you watched Midnight Horizon: Gravity Well.", "2d ago", false),
        AppNotification("n5", NotificationKind.APP, "Tip", "Tap and hold any poster to add it to your watchlist faster.", "1w ago", false),
    )

    val trendingSearches = listOf("Midnight Horizon", "Emberfall", "New this week", "Award winners", "Space")
    val suggestedSearches = listOf("Action", "Comedy", "Documentaries", "Highly rated")

    val allGenres = listOf(
        "Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary",
        "Drama", "Fantasy", "Horror", "Romance", "Sci-Fi", "Thriller",
    )

    fun search(query: String, typeFilter: MediaType? = null): List<MediaItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return catalog.filter { item ->
            (typeFilter == null || item.type == typeFilter) &&
                (
                    item.title.lowercase().contains(q) ||
                        item.description.lowercase().contains(q) ||
                        item.genres.any { it.lowercase().contains(q) } ||
                        castFor.getValue(item.id).any { it.name.lowercase().contains(q) }
                    )
        }
    }

    fun byGenre(genre: String?): List<MediaItem> =
        if (genre.isNullOrBlank()) catalog else catalog.filter { genre in it.genres }
}
