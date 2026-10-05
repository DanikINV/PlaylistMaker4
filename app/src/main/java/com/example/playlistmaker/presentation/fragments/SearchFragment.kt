package com.example.playlistmaker.presentation.fragments

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.activities.PlayerActivity
import com.example.playlistmaker.presentation.adapters.TrackAdapter
import com.example.playlistmaker.presentation.model.SearchContent
import com.example.playlistmaker.presentation.model.SearchScreenState
import com.example.playlistmaker.presentation.model.SearchViewModel
import com.example.playlistmaker.presentation.model.TrackParcelable
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment(R.layout.activity_search) {

    private var _binding: ActivitySearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModel()

    private lateinit var adapter: TrackAdapter

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ActivitySearchBinding.bind(view)

        setupWindowInsets()
        initRecyclerView()
        initListeners()
        observeViewModel()
    }

    private fun setupWindowInsets() {

        val isNightMode =
            resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES

        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        ).isAppearanceLightStatusBars = !isNightMode

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.rootLayout
        ) { _, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            val density =
                resources.displayMetrics.density

            val spacing =
                (16 * density).toInt()

            binding.toolbarLayout.setPadding(
                spacing,
                systemBars.top + spacing,
                spacing,
                spacing
            )

            insets
        }
    }

    private fun initRecyclerView() {

        binding.rvTracks.layoutManager =
            LinearLayoutManager(requireContext())

        adapter = TrackAdapter(
            onTrackClick = { track ->
                viewModel.onTrackSelected(track)
            }
        )

        binding.rvTracks.adapter = adapter
    }

    private fun initListeners() {

        binding.btnClearSearch.setOnClickListener {

            binding.etSearch.text.clear()
            hideKeyboard(binding.etSearch)
        }

        binding.btnRetry.setOnClickListener {
            viewModel.retry()
        }

        binding.btnClearHistory.setOnClickListener {
            viewModel.clearHistory()
        }

        binding.etSearch.doOnTextChanged { text, _, _, _ ->

            viewModel.onQueryChanged(
                text?.toString().orEmpty()
            )
        }

        binding.etSearch.setOnFocusChangeListener { _, hasFocus ->

            viewModel.onFocusChanged(hasFocus)
        }

        binding.etSearch.setOnEditorActionListener {
                _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                viewModel.searchNow()
                true

            } else {
                false
            }
        }
    }

    private fun observeViewModel() {

        viewModel.state.observe(
            viewLifecycleOwner
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

        binding.btnClearSearch.visibility =
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

        binding.tvHistoryTitle.visibility =
            View.VISIBLE

        binding.btnClearHistory.visibility =
            View.VISIBLE

        binding.placeholderContainer.visibility =
            View.GONE

        binding.progressBar.visibility =
            View.GONE

        binding.rvTracks.visibility =
            View.VISIBLE

        setHistoryLayoutMode(true)
    }

    private fun showTracks(
        tracks: List<Track>
    ) {

        setTracks(tracks)

        binding.tvHistoryTitle.visibility =
            View.GONE

        binding.btnClearHistory.visibility =
            View.GONE

        binding.placeholderContainer.visibility =
            View.GONE

        binding.progressBar.visibility =
            View.GONE

        binding.rvTracks.visibility =
            View.VISIBLE

        setHistoryLayoutMode(false)
    }

    private fun showLoading() {

        binding.tvHistoryTitle.visibility =
            View.GONE

        binding.btnClearHistory.visibility =
            View.GONE

        binding.placeholderContainer.visibility =
            View.GONE

        binding.rvTracks.visibility =
            View.GONE

        binding.progressBar.visibility =
            View.VISIBLE
    }

    private fun showNothingFound() {

        setTracks(emptyList())

        binding.tvHistoryTitle.visibility =
            View.GONE

        binding.btnClearHistory.visibility =
            View.GONE

        binding.rvTracks.visibility =
            View.GONE

        binding.progressBar.visibility =
            View.GONE

        binding.placeholderImage.setImageResource(
            R.drawable.ic_placeholder_no_results
        )

        binding.placeholderMessage.text =
            getString(R.string.nothing_found)

        binding.btnRetry.visibility =
            View.GONE

        binding.placeholderContainer.visibility =
            View.VISIBLE
    }

    private fun showError() {

        setTracks(emptyList())

        binding.tvHistoryTitle.visibility =
            View.GONE

        binding.btnClearHistory.visibility =
            View.GONE

        binding.rvTracks.visibility =
            View.GONE

        binding.progressBar.visibility =
            View.GONE

        binding.placeholderImage.setImageResource(
            R.drawable.ic_placeholder_no_internet
        )

        binding.placeholderMessage.text =
            getString(R.string.something_went_wrong)

        binding.btnRetry.visibility =
            View.VISIBLE

        binding.placeholderContainer.visibility =
            View.VISIBLE
    }

    private fun showEmpty() {

        setTracks(emptyList())

        binding.tvHistoryTitle.visibility =
            View.GONE

        binding.btnClearHistory.visibility =
            View.GONE

        binding.placeholderContainer.visibility =
            View.GONE

        binding.progressBar.visibility =
            View.GONE

        binding.rvTracks.visibility =
            View.VISIBLE

        setHistoryLayoutMode(false)
    }

    private fun setTracks(
        newTracks: List<Track>
    ) {

        adapter.setTracks(newTracks)
    }

    private fun openPlayer(
        track: Track
    ) {

        val intent =
            Intent(
                requireContext(),
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

        val imm =
            requireContext()
                .getSystemService(
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
            binding.rvTracks.layoutParams
                    as ConstraintLayout.LayoutParams

        val buttonParams =
            binding.btnClearHistory.layoutParams
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

        binding.rvTracks.layoutParams =
            recyclerParams

        binding.btnClearHistory.layoutParams =
            buttonParams
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}