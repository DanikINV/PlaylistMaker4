package com.example.playlistmaker.presentation.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.HistoryInteractor
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    private val _state = MutableLiveData(
        SearchScreenState()
    )

    val state: LiveData<SearchScreenState> = _state

    private var searchQuery = ""
    private var hasSearchFocus = false

    private var searchDebounceJob: Job? = null
    private var searchRequestJob: Job? = null
    private var trackClickJob: Job? = null

    fun onQueryChanged(query: String) {

        searchQuery = query

        searchDebounceJob?.cancel()
        searchRequestJob?.cancel()

        updateState {
            it.copy(
                tracks = emptyList(),
                content = SearchContent.EMPTY,
                isLoading = false,
                showClearButton = query.isNotEmpty()
            )
        }

        if (query.isEmpty()) {
            updateHistory()
        } else {
            searchDebounceJob = viewModelScope.launch {

                delay(SEARCH_DEBOUNCE_DELAY_MS)

                if (searchQuery.isNotEmpty()) {
                    performSearch(searchQuery)
                }
            }
        }
    }

    fun onFocusChanged(hasFocus: Boolean) {

        hasSearchFocus = hasFocus

        updateHistory()
    }

    fun searchNow() {

        searchDebounceJob?.cancel()

        if (searchQuery.isNotEmpty()) {
            performSearch(searchQuery)
        }
    }

    fun retry() {

        if (searchQuery.isNotEmpty()) {
            performSearch(searchQuery)
        }
    }

    fun clearHistory() {

        historyInteractor.clearHistory()

        if (
            hasSearchFocus &&
            searchQuery.isEmpty()
        ) {
            updateState {
                it.copy(
                    tracks = emptyList(),
                    content = SearchContent.EMPTY
                )
            }
        }
    }

    fun onTrackSelected(track: Track) {

        trackClickJob?.cancel()

        trackClickJob = viewModelScope.launch {

            delay(TRACK_CLICK_DEBOUNCE_DELAY_MS)

            historyInteractor.addTrack(track)

            updateState {
                it.copy(
                    selectedTrack = track
                )
            }
        }
    }

    fun onTrackNavigationHandled() {

        updateState {
            it.copy(
                selectedTrack = null
            )
        }
    }

    private fun updateHistory() {

        if (
            !hasSearchFocus ||
            searchQuery.isNotEmpty()
        ) {
            updateState {
                it.copy(
                    tracks = emptyList(),
                    content = SearchContent.EMPTY
                )
            }

            return
        }

        val history = historyInteractor.getHistory()

        updateState {
            it.copy(
                tracks = history,
                content =
                    if (history.isNotEmpty()) {
                        SearchContent.HISTORY
                    } else {
                        SearchContent.EMPTY
                    },
                isLoading = false
            )
        }
    }

    private fun performSearch(query: String) {

        searchRequestJob?.cancel()

        updateState {
            it.copy(
                tracks = emptyList(),
                content = SearchContent.EMPTY,
                isLoading = true
            )
        }

        searchRequestJob = viewModelScope.launch {

            searchInteractor
                .search(query)
                .collect { result ->

                    result
                        .onSuccess { tracks ->

                            updateState {
                                it.copy(
                                    tracks = tracks,
                                    content =
                                        if (tracks.isNotEmpty()) {
                                            SearchContent.TRACKS
                                        } else {
                                            SearchContent.NOTHING_FOUND
                                        },
                                    isLoading = false
                                )
                            }
                        }
                        .onFailure {

                            updateState {
                                it.copy(
                                    tracks = emptyList(),
                                    content = SearchContent.ERROR,
                                    isLoading = false
                                )
                            }
                        }
                }
        }
    }

    private fun updateState(
        reducer: (SearchScreenState) -> SearchScreenState
    ) {
        _state.value = reducer(
            _state.value ?: SearchScreenState()
        )
    }

    override fun onCleared() {

        searchDebounceJob?.cancel()
        searchRequestJob?.cancel()
        trackClickJob?.cancel()

        super.onCleared()
    }

    companion object {

        private const val SEARCH_DEBOUNCE_DELAY_MS = 2000L
        private const val TRACK_CLICK_DEBOUNCE_DELAY_MS = 1000L
    }
}