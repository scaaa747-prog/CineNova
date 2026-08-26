package com.cinenova.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinenova.app.data.MediaItem
import com.cinenova.app.data.Season
import com.cinenova.app.data.remote.ApiResult
import com.cinenova.app.data.remote.CatalogRepository
import com.cinenova.app.data.remote.PlaybackResources
import com.cinenova.app.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel bridging the MovieBox API layer to any screen that opts in.
 * UI files are untouched — screens can observe these states when wiring up.
 */
class CatalogViewModel(
    private val repository: CatalogRepository = ServiceLocator.catalogRepository,
) : ViewModel() {

    // ---- Bootstrap (token/tab config) ----
    data class BootstrapState(val loading: Boolean = false, val error: String? = null)

    private val _bootstrap = MutableStateFlow(BootstrapState())
    val bootstrap: StateFlow<BootstrapState> = _bootstrap.asStateFlow()

    fun bootstrapToken() {
        if (_bootstrap.value.loading) return
        _bootstrap.value = BootstrapState(loading = true)
        viewModelScope.launch {
            when (val result = repository.bootstrap()) {
                is ApiResult.Success -> _bootstrap.value = BootstrapState(loading = false)
                is ApiResult.HttpError -> _bootstrap.value =
                    BootstrapState(error = "HTTP ${result.code}")
                is ApiResult.NetworkError -> _bootstrap.value =
                    BootstrapState(error = result.cause.message ?: "Network error")
                ApiResult.Empty -> _bootstrap.value = BootstrapState()
            }
        }
    }

    // ---- Search ----
    sealed interface SearchUiState {
        data object Idle : SearchUiState
        data object Loading : SearchUiState
        data class Results(val items: List<MediaItem>) : SearchUiState
        data class Error(val message: String) : SearchUiState
    }

    private val _search = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val search: StateFlow<SearchUiState> = _search.asStateFlow()

    fun search(keyword: String, perPage: Int = 20) {
        if (keyword.isBlank()) {
            _search.value = SearchUiState.Idle
            return
        }
        _search.value = SearchUiState.Loading
        viewModelScope.launch {
            _search.value = when (val result = repository.search(keyword, perPage)) {
                is ApiResult.Success -> SearchUiState.Results(result.value)
                is ApiResult.HttpError -> SearchUiState.Error("HTTP ${result.code}")
                is ApiResult.NetworkError -> SearchUiState.Error(result.cause.message ?: "Network error")
                ApiResult.Empty -> SearchUiState.Results(emptyList())
            }
        }
    }

    // ---- Subject detail + seasons + cast ----
    sealed interface DetailUiState {
        data object Idle : DetailUiState
        data object Loading : DetailUiState
        data class Ready(
            val item: MediaItem,
            val seasons: List<Season> = emptyList(),
            val cast: List<com.cinenova.app.data.CastMember> = emptyList(),
        ) : DetailUiState
        data class Error(val message: String) : DetailUiState
    }

    private val _detail = MutableStateFlow<DetailUiState>(DetailUiState.Idle)
    val detail: StateFlow<DetailUiState> = _detail.asStateFlow()

    fun loadSubject(subjectId: Long) {
        _detail.value = DetailUiState.Loading
        viewModelScope.launch {
            val itemResult = repository.subjectDetail(subjectId)
            when (itemResult) {
                is ApiResult.Success -> {
                    val seasonsResult = repository.seasonsOf(subjectId)
                    val castResult = repository.castOf(subjectId)
                    _detail.value = DetailUiState.Ready(
                        item = itemResult.value,
                        seasons = seasonsResult.getOrNull().orEmpty(),
                        cast = castResult.getOrNull().orEmpty(),
                    )
                }
                is ApiResult.HttpError -> _detail.value = DetailUiState.Error("HTTP ${itemResult.code}")
                is ApiResult.NetworkError -> _detail.value =
                    DetailUiState.Error(itemResult.cause.message ?: "Network error")
                ApiResult.Empty -> _detail.value = DetailUiState.Error("Title not found")
            }
        }
    }

    // ---- Playback resources ----
    sealed interface PlaybackUiState {
        data object Idle : PlaybackUiState
        data object Loading : PlaybackUiState
        data class Ready(val resources: PlaybackResources) : PlaybackUiState
        data class Error(val message: String) : PlaybackUiState
    }

    private val _playback = MutableStateFlow<PlaybackUiState>(PlaybackUiState.Idle)
    val playback: StateFlow<PlaybackUiState> = _playback.asStateFlow()

    /**
     * Loads stream metadata for a title. Movies use se=0/ep=0.
     * The chosen source URL can be handed to the existing player layer.
     */
    fun loadPlayback(subjectId: Long, season: Int = 0, episode: Int = 0) {
        _playback.value = PlaybackUiState.Loading
        viewModelScope.launch {
            _playback.value = when (val result = repository.playbackResources(subjectId, season, episode)) {
                is ApiResult.Success -> {
                    if (result.value.sources.isEmpty()) PlaybackUiState.Error("No sources returned")
                    else PlaybackUiState.Ready(result.value)
                }
                is ApiResult.HttpError -> PlaybackUiState.Error("HTTP ${result.code}")
                is ApiResult.NetworkError -> PlaybackUiState.Error(result.cause.message ?: "Network error")
                ApiResult.Empty -> PlaybackUiState.Error("No sources returned")
            }
        }
    }
}
