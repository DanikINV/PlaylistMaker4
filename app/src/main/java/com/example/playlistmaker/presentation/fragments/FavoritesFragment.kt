package com.example.playlistmaker.presentation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavoritesBinding
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.activities.PlayerActivity
import com.example.playlistmaker.presentation.adapters.TrackAdapter
import com.example.playlistmaker.presentation.model.FavoritesViewModel
import com.example.playlistmaker.presentation.model.TrackParcelable
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment :
    Fragment(R.layout.fragment_favorites) {

    private var _binding:
            FragmentFavoritesBinding? = null

    private val binding
        get() = _binding!!

    private val viewModel:
            FavoritesViewModel by viewModel()

    private lateinit var adapter:
            TrackAdapter

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        _binding =
            FragmentFavoritesBinding.bind(view)

        setupRecyclerView()
        observeFavoriteTracks()
    }

    private fun setupRecyclerView() {

        binding.rvFavorites.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        adapter = TrackAdapter(
            onTrackClick = { track ->
                openPlayer(track)
            }
        )

        binding.rvFavorites.adapter =
            adapter
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

        binding.placeholderContainer.visibility =
            View.VISIBLE

        binding.rvFavorites.visibility =
            View.GONE
    }

    private fun showFavorites(
        tracks: List<Track>
    ) {

        binding.placeholderContainer.visibility =
            View.GONE

        binding.rvFavorites.visibility =
            View.VISIBLE

        adapter.setTracks(
            tracks
        )
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

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }

    companion object {

        fun newInstance():
                FavoritesFragment {

            return FavoritesFragment()
        }
    }
}