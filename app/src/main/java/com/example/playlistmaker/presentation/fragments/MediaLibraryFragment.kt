package com.example.playlistmaker.presentation.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityMediaLibraryBinding
import com.example.playlistmaker.presentation.adapters.MediaLibraryPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class MediaLibraryFragment :
    Fragment(R.layout.activity_media_library) {

    private var _binding: ActivityMediaLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        _binding =
            ActivityMediaLibraryBinding.bind(view)

        setupWindowInsets()
        setupViewPager()

        parentFragmentManager.setFragmentResultListener(
            "playlist_created",
            viewLifecycleOwner
        ) { _, bundle ->

            val playlistName =
                bundle.getString(
                    "playlist_name"
                )

            if (!playlistName.isNullOrBlank()) {

                showPlaylistCreatedNotification(
                    playlistName
                )
            }
        }
    }

    fun openCreatePlaylist() {

        findNavController().navigate(
            R.id.action_mediaLibraryFragment_to_createPlaylistFragment
        )
    }

    private fun setupWindowInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.rootLayout
        ) { root, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            root.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }

    private fun setupViewPager() {

        binding.viewPager.adapter =
            MediaLibraryPagerAdapter(this)

        TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager
        ) { tab, position ->

            tab.text =
                when (position) {

                    0 ->
                        getString(
                            R.string.favorite_tracks
                        )

                    else ->
                        getString(
                            R.string.playlists
                        )
                }

        }.attach()
    }

    private fun showPlaylistCreatedNotification(
        playlistName: String
    ) {

        val notification =
            binding.rootLayout.findViewById<TextView>(
                R.id.playlist_created_notification
            )

        notification.text =
            "Плейлист $playlistName создан"

        notification.visibility =
            View.VISIBLE

        notification.alpha =
            1f

        notification.translationY =
            notification.height.toFloat()

        notification.post {

            notification.animate()
                .translationY(0f)
                .setDuration(300)
                .start()
        }

        notification.postDelayed({

            notification
                .animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {

                    notification.visibility =
                        View.GONE

                    notification.alpha =
                        1f

                    notification.translationY =
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