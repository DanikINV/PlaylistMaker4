package com.example.playlistmaker.presentation.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
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


    private lateinit var playlistCreatedNotification: TextView


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        _binding =
            FragmentPlaylistsBinding.bind(view)

        playlistCreatedNotification =
            binding.root.findViewById(
                R.id.playlist_created_notification
            )

        setupRecyclerView()

        observePlaylists()

        setupNewPlaylistButton()

        parentFragmentManager
            .setFragmentResultListener(
                "playlist_created",
                viewLifecycleOwner
            ) { _, bundle ->


                val name =
                    bundle.getString(
                        "playlist_name"
                    )


                if (!name.isNullOrBlank()) {

                    showPlaylistCreatedNotification(
                        name
                    )
                }
            }
    }

    private fun setupRecyclerView() {


        playlistAdapter =
            PlaylistAdapter {


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

    private fun setupNewPlaylistButton() {


        binding.btnNewPlaylist.setOnClickListener {


            findNavController()
                .navigate(
                    R.id.action_mediaLibraryFragment_to_createPlaylistFragment
                )
        }
    }

    private fun showPlaylistCreatedNotification(
        playlistName: String
    ) {
        playlistCreatedNotification.setBackgroundColor(
            requireContext().getColor(
                R.color.notification
            )
        )

        playlistCreatedNotification.setTextColor(
            requireContext().getColor(
                R.color.notification_text
            )
        )

        playlistCreatedNotification.text =
            "Плейлист $playlistName создан"

        playlistCreatedNotification.visibility =
            View.VISIBLE

        playlistCreatedNotification.alpha =
            0f

        playlistCreatedNotification.translationY =
            40f

        playlistCreatedNotification.bringToFront()

        playlistCreatedNotification.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .start()

        playlistCreatedNotification.postDelayed({

            playlistCreatedNotification
                .animate()
                .alpha(0f)
                .translationY(40f)
                .setDuration(300)
                .withEndAction {

                    playlistCreatedNotification.visibility =
                        View.GONE

                    playlistCreatedNotification.alpha =
                        1f

                    playlistCreatedNotification.translationY =
                        0f
                }
                .start()

        }, 2500)
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}