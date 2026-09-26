package com.example.playlistmaker.presentation.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
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
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.adapters.TrackAdapter
import com.example.playlistmaker.presentation.model.TrackParcelable
import com.example.playlistmaker.presentation.model.SearchContent
import com.example.playlistmaker.presentation.model.SearchScreenState
import com.example.playlistmaker.presentation.model.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {

    private val viewModel: SearchViewModel by viewModel()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContentView(R.layout.activity_search)

        val isNightMode =
            resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = !isNightMode
        }

        val rootView = findViewById<View>(
            R.id.root_layout
        )

        val toolbarLayout = findViewById<View>(
            R.id.toolbar_layout
        )

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView
        ) { _, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            val density =
                resources.displayMetrics.density

            val spacing =
                (16 * density).toInt()

            toolbarLayout.setPadding(
                spacing,
                systemBars.top + spacing,
                spacing,
                spacing
            )

            insets
        }

        initViews()
        initRecyclerView()
        initListeners()
        observeViewModel()
    }

    private fun initViews() {

        findViewById<ImageView>(
            R.id.btn_back
        ).setOnClickListener {
            finish()
        }

        etSearch = findViewById(
            R.id.et_search
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

        btnRetry = findViewById(
            R.id.btn_retry
        )

        tvHistoryTitle = findViewById(
            R.id.tv_history_title
        )

        btnClearHistory = findViewById(
            R.id.btn_clear_history
        )

        progressBar = findViewById(
            R.id.progress_bar
        )

        rvTracks = findViewById(
            R.id.rv_tracks
        )
    }

    private fun initRecyclerView() {

        rvTracks.layoutManager =
            LinearLayoutManager(this)

        adapter = TrackAdapter(
            displayedTracks
        ) { track ->
            viewModel.onTrackSelected(track)
        }

        rvTracks.adapter = adapter
    }

    private fun initListeners() {

        val btnClear = findViewById<ImageView>(
            R.id.btn_clear_search
        )

        btnClear.setOnClickListener {

            etSearch.text.clear()

            hideKeyboard(etSearch)
        }

        btnRetry.setOnClickListener {
            viewModel.retry()
        }

        btnClearHistory.setOnClickListener {
            viewModel.clearHistory()
        }

        etSearch.doOnTextChanged { text, _, _, _ ->

            viewModel.onQueryChanged(
                text?.toString().orEmpty()
            )
        }

        etSearch.setOnFocusChangeListener { _, hasFocus ->

            viewModel.onFocusChanged(
                hasFocus
            )
        }

        etSearch.setOnEditorActionListener {
                _, actionId, _ ->

            if (
                actionId ==
                EditorInfo.IME_ACTION_DONE
            ) {
                viewModel.searchNow()
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {

        viewModel.state.observe(
            this
        ) { state ->

            renderState(state)

            state.selectedTrack?.let { track ->

                openPlayer(track)

                viewModel.onTrackNavigationHandled()
            }
        }
    }

    private fun renderState(
        state: SearchScreenState
    ) {

        val clearButton =
            findViewById<ImageView>(
                R.id.btn_clear_search
            )

        clearButton.visibility =
            if (state.showClearButton) {
                View.VISIBLE
            } else {
                View.GONE
            }

        if (state.isLoading) {
            showLoading()
            return
        }

        when (state.content) {

            SearchContent.HISTORY -> {
                showHistory(state.tracks)
            }

            SearchContent.TRACKS -> {
                showTracks(state.tracks)
            }

            SearchContent.NOTHING_FOUND -> {
                showNothingFound()
            }

            SearchContent.ERROR -> {
                showError()
            }

            SearchContent.EMPTY -> {
                showEmpty()
            }
        }
    }

    private fun showHistory(
        tracks: List<Track>
    ) {

        setTracks(tracks)

        tvHistoryTitle.visibility =
            View.VISIBLE

        btnClearHistory.visibility =
            View.VISIBLE

        placeholderContainer.visibility =
            View.GONE

        progressBar.visibility =
            View.GONE

        rvTracks.visibility =
            View.VISIBLE

        setHistoryLayoutMode(true)
    }

    private fun showTracks(
        tracks: List<Track>
    ) {

        setTracks(tracks)

        tvHistoryTitle.visibility =
            View.GONE

        btnClearHistory.visibility =
            View.GONE

        placeholderContainer.visibility =
            View.GONE

        progressBar.visibility =
            View.GONE

        rvTracks.visibility =
            View.VISIBLE

        setHistoryLayoutMode(false)
    }

    private fun showLoading() {

        tvHistoryTitle.visibility =
            View.GONE

        btnClearHistory.visibility =
            View.GONE

        placeholderContainer.visibility =
            View.GONE

        rvTracks.visibility =
            View.GONE

        progressBar.visibility =
            View.VISIBLE
    }

    private fun showNothingFound() {

        setTracks(emptyList())

        tvHistoryTitle.visibility =
            View.GONE

        btnClearHistory.visibility =
            View.GONE

        rvTracks.visibility =
            View.GONE

        progressBar.visibility =
            View.GONE

        placeholderImage.setImageResource(
            R.drawable.ic_placeholder_no_results
        )

        placeholderMessage.text =
            getString(
                R.string.nothing_found
            )

        btnRetry.visibility =
            View.GONE

        placeholderContainer.visibility =
            View.VISIBLE
    }

    private fun showError() {

        setTracks(emptyList())

        tvHistoryTitle.visibility =
            View.GONE

        btnClearHistory.visibility =
            View.GONE

        rvTracks.visibility =
            View.GONE

        progressBar.visibility =
            View.GONE

        placeholderImage.setImageResource(
            R.drawable.ic_placeholder_no_internet
        )

        placeholderMessage.text =
            getString(
                R.string.something_went_wrong
            )

        btnRetry.visibility =
            View.VISIBLE

        placeholderContainer.visibility =
            View.VISIBLE
    }

    private fun showEmpty() {

        setTracks(emptyList())

        tvHistoryTitle.visibility =
            View.GONE

        btnClearHistory.visibility =
            View.GONE

        placeholderContainer.visibility =
            View.GONE

        progressBar.visibility =
            View.GONE

        rvTracks.visibility =
            View.VISIBLE

        setHistoryLayoutMode(false)
    }

    private fun setTracks(
        newTracks: List<Track>
    ) {

        displayedTracks.clear()

        displayedTracks.addAll(
            newTracks
        )

        adapter.notifyDataSetChanged()
    }

    private fun openPlayer(
        track: Track
    ) {

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

    private fun hideKeyboard(
        view: View
    ) {

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

        rvTracks.layoutParams =
            recyclerParams

        btnClearHistory.layoutParams =
            buttonParams
    }
}