package com.example.playlistmaker.presentation.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.interactors.HistoryInteractor
import com.example.playlistmaker.domain.interactors.SearchConsumer
import com.example.playlistmaker.domain.interactors.SearchInteractor
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.Creator
import com.example.playlistmaker.presentation.adapters.TrackAdapter
import com.example.playlistmaker.presentation.model.TrackParcelable

class SearchActivity : AppCompatActivity() {

    private lateinit var searchInteractor: SearchInteractor
    private lateinit var historyInteractor: HistoryInteractor

    private lateinit var etSearch: EditText
    private lateinit var rvTracks: RecyclerView
    private lateinit var placeholderContainer: View
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderMessage: TextView
    private lateinit var btnRetry: Button
    private lateinit var tvHistoryTitle: TextView
    private lateinit var btnClearHistory: Button
    private lateinit var progressBar: ProgressBar

    private val displayedTracks = ArrayList<Track>()
    private lateinit var adapter: TrackAdapter

    private var searchText: String = ""

    private val searchDebounceHandler = Handler(Looper.getMainLooper())

    private val searchRunnable = Runnable {
        val query = etSearch.text?.toString().orEmpty()

        if (query.isNotEmpty()) {
            search(query)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_search)

        val isNightMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isNightMode
        }

        val rootView = findViewById<View>(R.id.root_layout)
        val toolbarLayout = findViewById<View>(R.id.toolbar_layout)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            val density = resources.displayMetrics.density
            val spacing = (16 * density).toInt()

            toolbarLayout.setPadding(
                spacing,
                systemBars.top + spacing,
                spacing,
                spacing
            )

            insets
        }

        searchInteractor = Creator.provideSearchInteractor()
        historyInteractor = Creator.provideHistoryInteractor(applicationContext)

        val btnBack = findViewById<ImageView>(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }

        etSearch = findViewById(R.id.et_search)

        val btnClear = findViewById<ImageView>(
            R.id.btn_clear_search
        )

        placeholderContainer = findViewById(
            R.id.placeholder_container
        )

        placeholderImage = findViewById(
            R.id.placeholder_image
        )

        placeholderMessage = findViewById(
            R.id.placeholder_message
        )

        btnRetry = findViewById(R.id.btn_retry)

        tvHistoryTitle = findViewById(
            R.id.tv_history_title
        )

        btnClearHistory = findViewById(
            R.id.btn_clear_history
        )

        progressBar = findViewById(
            R.id.progress_bar
        )

        rvTracks = findViewById(R.id.rv_tracks)

        rvTracks.layoutManager = LinearLayoutManager(this)

        adapter = TrackAdapter(displayedTracks) { track ->
            onTrackClicked(track)
        }

        rvTracks.adapter = adapter

        btnClear.setOnClickListener {

            searchDebounceHandler.removeCallbacks(
                searchRunnable
            )

            etSearch.text.clear()

            hideKeyboard(etSearch)

            hidePlaceholder()

            updateHistoryVisibility()
        }

        btnRetry.setOnClickListener {

            val query = etSearch.text?.toString().orEmpty()

            if (query.isNotEmpty()) {
                search(query)
            }
        }

        btnClearHistory.setOnClickListener {

            historyInteractor.clearHistory()

            setTracks(emptyList())

            tvHistoryTitle.visibility = View.GONE
            btnClearHistory.visibility = View.GONE
            placeholderContainer.visibility = View.GONE
            rvTracks.visibility = View.VISIBLE
        }

        etSearch.doOnTextChanged { text, _, _, _ ->

            btnClear.visibility =
                if (text.isNullOrEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            searchText = text?.toString().orEmpty()

            searchDebounceHandler.removeCallbacks(
                searchRunnable
            )

            if (text.isNullOrEmpty()) {
                hidePlaceholder()
            } else {
                searchDebounceHandler.postDelayed(
                    searchRunnable,
                    SEARCH_DEBOUNCE_DELAY_MS
                )
            }

            updateHistoryVisibility()
        }

        etSearch.setOnFocusChangeListener { _, _ ->
            updateHistoryVisibility()
        }

        etSearch.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                val query = etSearch.text?.toString().orEmpty()

                if (query.isNotEmpty()) {

                    searchDebounceHandler.removeCallbacks(
                        searchRunnable
                    )

                    search(query)
                }

                true
            } else {
                false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateHistoryVisibility()
    }

    private fun onTrackClicked(track: Track) {

        historyInteractor.addTrack(track)

        val intent = Intent(
            this,
            PlayerActivity::class.java
        ).apply {

            putExtra(
                PlayerActivity.EXTRA_TRACK,
                TrackParcelable.fromDomain(track)
            )
        }

        startActivity(intent)
    }

    private fun updateHistoryVisibility() {

        val query = etSearch.text?.toString().orEmpty()

        val hasFocusAndEmpty =
            etSearch.hasFocus() && query.isEmpty()

        if (!hasFocusAndEmpty) {

            tvHistoryTitle.visibility = View.GONE
            btnClearHistory.visibility = View.GONE

            return
        }

        val history = historyInteractor.getHistory()

        setTracks(history)

        val showHistoryUi = history.isNotEmpty()

        tvHistoryTitle.visibility =
            if (showHistoryUi) {
                View.VISIBLE
            } else {
                View.GONE
            }

        btnClearHistory.visibility =
            if (showHistoryUi) {
                View.VISIBLE
            } else {
                View.GONE
            }

        setHistoryLayoutMode(showHistoryUi)

        placeholderContainer.visibility = View.GONE
        rvTracks.visibility = View.VISIBLE
    }

    private fun setTracks(newTracks: List<Track>) {

        displayedTracks.clear()
        displayedTracks.addAll(newTracks)

        adapter.notifyDataSetChanged()
    }

    private fun search(query: String) {

        showLoading()

        searchInteractor.search(
            query = query,
            consumer = object : SearchConsumer {

                override fun consume(
                    tracks: List<Track>
                ) {

                    progressBar.visibility = View.GONE

                    if (tracks.isNotEmpty()) {

                        setTracks(tracks)
                        showTracks()

                    } else {

                        showPlaceholder(
                            text = getString(
                                R.string.nothing_found
                            ),
                            image =
                                R.drawable.ic_placeholder_no_results,
                            showRetry = false
                        )
                    }
                }

                override fun consumeError() {

                    progressBar.visibility = View.GONE

                    showPlaceholder(
                        text = getString(
                            R.string.something_went_wrong
                        ),
                        image =
                            R.drawable.ic_placeholder_no_internet,
                        showRetry = true
                    )
                }
            }
        )
    }

    private fun showLoading() {

        tvHistoryTitle.visibility = View.GONE
        btnClearHistory.visibility = View.GONE
        placeholderContainer.visibility = View.GONE
        rvTracks.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun showTracks() {

        tvHistoryTitle.visibility = View.GONE
        btnClearHistory.visibility = View.GONE
        placeholderContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
        rvTracks.visibility = View.VISIBLE
    }

    private fun showPlaceholder(
        text: String,
        image: Int,
        showRetry: Boolean
    ) {

        setTracks(emptyList())

        tvHistoryTitle.visibility = View.GONE
        btnClearHistory.visibility = View.GONE
        rvTracks.visibility = View.GONE
        progressBar.visibility = View.GONE

        placeholderImage.setImageResource(image)
        placeholderMessage.text = text

        btnRetry.visibility =
            if (showRetry) {
                View.VISIBLE
            } else {
                View.GONE
            }

        placeholderContainer.visibility = View.VISIBLE
    }

    private fun hidePlaceholder() {

        setTracks(emptyList())

        placeholderContainer.visibility = View.GONE
        progressBar.visibility = View.GONE
        rvTracks.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {

        super.onSaveInstanceState(outState)

        outState.putString(
            SEARCH_TEXT_KEY,
            searchText
        )
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle
    ) {

        super.onRestoreInstanceState(savedInstanceState)

        searchText = savedInstanceState.getString(
            SEARCH_TEXT_KEY,
            ""
        )

        etSearch.setText(searchText)
    }

    override fun onDestroy() {

        super.onDestroy()

        searchDebounceHandler.removeCallbacks(
            searchRunnable
        )
    }

    private fun hideKeyboard(view: View) {

        val imm = getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager

        imm.hideSoftInputFromWindow(
            view.windowToken,
            0
        )

        view.clearFocus()
    }

    private fun setHistoryLayoutMode(
        compact: Boolean
    ) {

        val recyclerParams =
            rvTracks.layoutParams
                    as ConstraintLayout.LayoutParams

        val buttonParams =
            btnClearHistory.layoutParams
                    as ConstraintLayout.LayoutParams

        if (compact) {

            recyclerParams.height =
                ConstraintLayout.LayoutParams.WRAP_CONTENT

            recyclerParams.bottomToTop =
                ConstraintLayout.LayoutParams.UNSET

            buttonParams.topToBottom =
                R.id.rv_tracks

            buttonParams.bottomToBottom =
                ConstraintLayout.LayoutParams.UNSET

        } else {

            recyclerParams.height = 0

            recyclerParams.bottomToTop =
                R.id.btn_clear_history

            buttonParams.topToBottom =
                ConstraintLayout.LayoutParams.UNSET

            buttonParams.bottomToBottom =
                ConstraintLayout.LayoutParams.PARENT_ID
        }

        rvTracks.layoutParams = recyclerParams
        btnClearHistory.layoutParams = buttonParams
    }

    companion object {

        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT"

        private const val SEARCH_DEBOUNCE_DELAY_MS = 2000L
    }
}