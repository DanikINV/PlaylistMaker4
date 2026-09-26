package com.example.playlistmaker.presentation.adapters

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlistmaker.presentation.fragments.FavoritesFragment
import com.example.playlistmaker.presentation.fragments.PlaylistsFragment

class MediaLibraryPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): androidx.fragment.app.Fragment {
        return when (position) {
            0 -> FavoritesFragment.newInstance()
            1 -> PlaylistsFragment.newInstance()
            else -> throw IllegalArgumentException(
                "Unknown position: $position"
            )
        }
    }
}