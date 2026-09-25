package com.example.playlistmaker.presentation.model

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactors.HistoryInteractor
import com.example.playlistmaker.domain.interactors.SearchConsumer
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.model.Track

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    private val _state = MutableLiveData(
        SearchScreenState()
    )

    val state: LiveData<SearchScreenState> = _state

    private val handler = Handler(
        Looper.getMainLooper()
    )

    private var searchQuery = ""
    private var hasSearchFocus = false

    private var searchRequestId = 0L

    private val searchRunnable = Runnable {
        if (searchQuery.isNotEmpty()) {
            performSearch(searchQuery)
        }
    }

    fun onQueryChanged(query: String) {

        searchQuery = query
        searchRequestId++

        handler.removeCallbacks(searchRunnable)

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
            handler.postDelayed(
                searchRunnable,
                SEARCH_DEBOUNCE_DELAY_MS
            )
        }
    }

    fun onFocusChanged(hasFocus: Boolean) {

        hasSearchFocus = hasFocus

        updateHistory()
    }

    fun searchNow() {

        handler.removeCallbacks(searchRunnable)

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

        historyInteractor.addTrack(track)

        updateState {
            it.copy(
                selectedTrack = track
            )
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

        val requestId = ++searchRequestId

        updateState {
            it.copy(
                tracks = emptyList(),
                content = SearchContent.EMPTY,
                isLoading = true
            )
        }

        searchInteractor.search(
            query = query,
            consumer = object : SearchConsumer {

                override fun consume(
                    tracks: List<Track>
                ) {

                    if (requestId != searchRequestId) {
                        return
                    }

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

                override fun consumeError() {

                    if (requestId != searchRequestId) {
                        return
                    }

                    updateState {
                        it.copy(
                            tracks = emptyList(),
                            content = SearchContent.ERROR,
                            isLoading = false
                        )
                    }
                }
            }
        )
    }

    private fun updateState(
        reducer: (SearchScreenState) -> SearchScreenState
    ) {
        _state.value = reducer(
            _state.value ?: SearchScreenState()
        )
    }

    override fun onCleared() {

        handler.removeCallbacks(
            searchRunnable
        )

        super.onCleared()
    }

    companion object {

        private const val SEARCH_DEBOUNCE_DELAY_MS = 2000L
    }
}