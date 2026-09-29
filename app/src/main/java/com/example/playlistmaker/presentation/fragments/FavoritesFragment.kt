package com.example.playlistmaker.presentation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.activities.PlayerActivity
import com.example.playlistmaker.presentation.adapters.TrackAdapter
import com.example.playlistmaker.presentation.model.FavoritesViewModel
import com.example.playlistmaker.presentation.model.TrackParcelable
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private val viewModel: FavoritesViewModel by viewModel()

    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var placeholderContainer: View

    private var trackAdapter: TrackAdapter? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        favoritesRecyclerView =
            view.findViewById(R.id.rv_favorites)

        placeholderContainer =
            view.findViewById(R.id.placeholder_container)

        setupRecyclerView()
        observeFavoriteTracks()
    }

    private fun setupRecyclerView() {

        favoritesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
    }

    private fun observeFavoriteTracks() {

        viewModel.favoriteTracks.observe(
            viewLifecycleOwner
        ) { tracks ->

            if (tracks.isEmpty()) {
                showPlaceholder()
            } else {
                showFavorites(tracks)
            }
        }
    }

    private fun showPlaceholder() {

        placeholderContainer.visibility =
            View.VISIBLE

        favoritesRecyclerView.visibility =
            View.GONE
    }

    private fun showFavorites(
        tracks: List<Track>
    ) {

        placeholderContainer.visibility =
            View.GONE

        favoritesRecyclerView.visibility =
            View.VISIBLE

        trackAdapter = TrackAdapter(
            tracks = tracks
        ) { track ->
            openPlayer(track)
        }

        favoritesRecyclerView.adapter =
            trackAdapter
    }

    private fun openPlayer(track: Track) {

        val intent = Intent(
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

    companion object {

        fun newInstance(): FavoritesFragment {
            return FavoritesFragment()
        }
    }
}