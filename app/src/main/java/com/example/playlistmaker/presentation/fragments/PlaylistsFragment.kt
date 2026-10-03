package com.example.playlistmaker.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.presentation.adapter.PlaylistAdapter
import com.example.playlistmaker.presentation.model.PlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment :
    Fragment(R.layout.fragment_playlists) {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistsViewModel by viewModel()

    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        _binding = FragmentPlaylistsBinding.bind(view)

        setupRecyclerView()
        setupCreateButton()
        observePlaylists()
    }

    private fun setupCreateButton() {

        binding.btnNewPlaylist.setOnClickListener {

            findNavController().navigate(
                R.id.action_global_createPlaylistFragment,
                bundleOf(
                    "playlistId" to -1L
                )
            )
        }
    }

    private fun setupRecyclerView() {

        playlistAdapter =
            PlaylistAdapter { playlist ->

                findNavController().navigate(
                    R.id.action_global_playlistFragment,
                    bundleOf(
                        "playlistId" to playlist.playlistId
                    )
                )
            }

        binding.rvPlaylists.layoutManager =
            GridLayoutManager(
                requireContext(),
                2
            )

        binding.rvPlaylists.adapter =
            playlistAdapter
    }

    private fun observePlaylists() {

        viewModel.playlists.observe(
            viewLifecycleOwner
        ) { playlists ->

            playlistAdapter.setPlaylists(
                playlists
            )

            if (playlists.isEmpty()) {

                binding.rvPlaylists.visibility =
                    View.GONE

                binding.placeholderContainer.visibility =
                    View.VISIBLE

            } else {

                binding.rvPlaylists.visibility =
                    View.VISIBLE

                binding.placeholderContainer.visibility =
                    View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}