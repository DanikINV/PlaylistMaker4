package com.example.playlistmaker.domain.interactors

import com.example.playlistmaker.domain.model.Track

class PlayerInteractorImpl(
    private val track: Track
) : PlayerInteractor {

    override fun getTrack(): Track {
        return track
    }
}