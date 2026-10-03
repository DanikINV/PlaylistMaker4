package com.example.playlistmaker.presentation.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlistmaker.presentation.fragments.FavoritesFragment
import com.example.playlistmaker.presentation.fragments.PlaylistsFragment

class MediaLibraryPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {


    override fun getItemCount(): Int = 2


    override fun createFragment(position: Int): Fragment {

        return when(position) {

            0 -> FavoritesFragment.newInstance()

            1 -> PlaylistsFragment()

            else -> throw IllegalArgumentException(
                "Unknown position"
            )
        }
    }
}