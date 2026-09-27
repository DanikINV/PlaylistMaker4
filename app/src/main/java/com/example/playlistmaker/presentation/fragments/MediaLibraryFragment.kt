package com.example.playlistmaker.presentation.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.adapters.MediaLibraryPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MediaLibraryFragment : Fragment(R.layout.activity_media_library) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val rootView = view.findViewById<View>(
            R.id.root_layout
        )

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView
        ) { root, insets ->

            val systemBars = insets.getInsets(
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

        val viewPager = view.findViewById<ViewPager2>(
            R.id.view_pager
        )

        val tabLayout = view.findViewById<TabLayout>(
            R.id.tab_layout
        )

        viewPager.adapter =
            MediaLibraryPagerAdapter(requireActivity())

        TabLayoutMediator(
            tabLayout,
            viewPager
        ) { tab, position ->

            tab.text = when (position) {
                0 -> getString(R.string.favorite_tracks)
                else -> getString(R.string.playlists)
            }

        }.attach()
    }

    companion object {

        fun newInstance(): MediaLibraryFragment {
            return MediaLibraryFragment()
        }
    }
}