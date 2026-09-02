package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private var searchText: String = ""
    private val iTunesService: ITunesApi = ITunesNetworkClient.service
    private val tracks = ArrayList<Track>()
    private val historyTracks = ArrayList<Track>()
    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var searchHistory: SearchHistory

    private lateinit var etSearch: EditText
    private lateinit var resultsContainer: View
    private lateinit var rvTracks: RecyclerView
    private lateinit var placeholderContainer: View
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderMessage: TextView
    private lateinit var btnRetry: Button
    private lateinit var historyContainer: View
    private lateinit var rvHistory: RecyclerView
    private lateinit var btnClearHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_search)

        val isNightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isNightMode
        }

        val rootView = findViewById<View>(R.id.root_layout)
        val toolbarLayout = findViewById<View>(R.id.toolbar_layout)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            val spacing = (16 * density).toInt()
            toolbarLayout.setPadding(spacing, systemBars.top + spacing, spacing, spacing)
            insets
        }

        searchHistory = SearchHistory(getSharedPreferences(PREFS_NAME, MODE_PRIVATE))

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        etSearch = findViewById(R.id.et_search)
        val btnClear = findViewById<ImageView>(R.id.btn_clear_search)
        resultsContainer = findViewById(R.id.results_container)
        placeholderContainer = findViewById(R.id.placeholder_container)
        placeholderImage = findViewById(R.id.placeholder_image)
        placeholderMessage = findViewById(R.id.placeholder_message)
        btnRetry = findViewById(R.id.btn_retry)
        historyContainer = findViewById(R.id.history_container)
        rvHistory = findViewById(R.id.rv_history)
        btnClearHistory = findViewById(R.id.btn_clear_history)

        rvTracks = findViewById(R.id.rv_tracks)
        rvTracks.layoutManager = LinearLayoutManager(this)
        adapter = TrackAdapter(tracks) { track -> onTrackClicked(track) }
        rvTracks.adapter = adapter

        rvHistory.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(historyTracks) { track -> onTrackClicked(track) }
        rvHistory.adapter = historyAdapter

        btnClear.setOnClickListener {
            etSearch.text.clear()
            hideKeyboard(etSearch)
            hidePlaceholder()
            updateHistoryVisibility()
        }

        btnRetry.setOnClickListener {
            if (etSearch.text.isNotEmpty()) {
                search(etSearch.text.toString())
            }
        }

        btnClearHistory.setOnClickListener {
            searchHistory.clearHistory()
            historyTracks.clear()
            historyAdapter.notifyDataSetChanged()
            updateHistoryVisibility()
        }

        etSearch.doOnTextChanged { text, _, _, _ ->
            btnClear.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
            searchText = text?.toString() ?: ""
            if (text.isNullOrEmpty()) {
                hidePlaceholder()
            }
            updateHistoryVisibility()
        }

        etSearch.setOnFocusChangeListener { _, _ ->
            updateHistoryVisibility()
        }

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (etSearch.text.isNotEmpty()) {
                    search(etSearch.text.toString())
                }
                true
            }
            false
        }
        updateHistoryVisibility()
    }

    private fun onTrackClicked(track: Track) {
        searchHistory.addTrack(track)
        // TODO: soon...
    }

    private fun updateHistoryVisibility() {
        val query = etSearch.text?.toString().orEmpty()
        val shouldShowHistory = etSearch.hasFocus() && query.isEmpty()

        if (shouldShowHistory) {
            historyTracks.clear()
            historyTracks.addAll(searchHistory.getHistory())
            historyAdapter.notifyDataSetChanged()
        }

        if (shouldShowHistory && historyTracks.isNotEmpty()) {
            historyContainer.visibility = View.VISIBLE
            resultsContainer.visibility = View.GONE
        } else {
            historyContainer.visibility = View.GONE
            resultsContainer.visibility = View.VISIBLE
        }
    }

    private fun search(query: String) {
        iTunesService.search(query).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
                if (response.code() == 200) {
                    val results = response.body()?.results.orEmpty()
                    if (results.isNotEmpty()) {
                        tracks.clear()
                        tracks.addAll(results)
                        adapter.notifyDataSetChanged()
                        showTracks()
                    } else {
                        showPlaceholder(
                            text = getString(R.string.nothing_found),
                            image = R.drawable.ic_placeholder_no_results,
                            showRetry = false
                        )
                    }
                } else {
                    showPlaceholder(
                        text = getString(R.string.something_went_wrong),
                        image = R.drawable.ic_placeholder_no_internet,
                        showRetry = true
                    )
                }
            }

            override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                showPlaceholder(
                    text = getString(R.string.something_went_wrong),
                    image = R.drawable.ic_placeholder_no_internet,
                    showRetry = true
                )
            }
        })
    }

    private fun showTracks() {
        placeholderContainer.visibility = View.GONE
        rvTracks.visibility = View.VISIBLE
    }

    private fun showPlaceholder(text: String, image: Int, showRetry: Boolean) {
        tracks.clear()
        adapter.notifyDataSetChanged()
        rvTracks.visibility = View.GONE
        placeholderImage.setImageResource(image)
        placeholderMessage.text = text
        btnRetry.visibility = if (showRetry) View.VISIBLE else View.GONE
        placeholderContainer.visibility = View.VISIBLE
    }

    private fun hidePlaceholder() {
        tracks.clear()
        adapter.notifyDataSetChanged()
        placeholderContainer.visibility = View.GONE
        rvTracks.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        etSearch.setText(searchText)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT"
        private const val PREFS_NAME = "playlist_maker_prefs"
    }
}