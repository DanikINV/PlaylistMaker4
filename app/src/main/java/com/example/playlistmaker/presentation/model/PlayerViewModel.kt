package com.example.playlistmaker.presentation.model

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactors.PlayerInteractor

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor
) : ViewModel() {

    private val track = playerInteractor.getTrack()

    private val _state = MutableLiveData(
        PlayerScreenState(
            track = track
        )
    )

    val state: LiveData<PlayerScreenState> = _state

    private var mediaPlayer: MediaPlayer? = null

    private val handler = Handler(
        Looper.getMainLooper()
    )

    fun preparePlayer() {

        if (mediaPlayer != null) {
            return
        }

        val previewUrl = track.previewUrl

        if (previewUrl.isNullOrEmpty()) {
            return
        }

        mediaPlayer = MediaPlayer().apply {

            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_MUSIC
                    )
                    .setUsage(
                        AudioAttributes.USAGE_MEDIA
                    )
                    .build()
            )

            setDataSource(previewUrl)

            setOnPreparedListener {

                _state.value = _state.value?.copy(
                    playbackState = PlayerPlaybackState.PREPARED,
                    isPlayEnabled = true,
                    progress = 0
                )
            }

            setOnCompletionListener {

                _state.value = _state.value?.copy(
                    playbackState = PlayerPlaybackState.PREPARED,
                    progress = 0
                )

                handler.removeCallbacksAndMessages(null)
            }

            setOnErrorListener { _, _, _ ->

                _state.value = _state.value?.copy(
                    playbackState = PlayerPlaybackState.DEFAULT,
                    isPlayEnabled = false
                )

                true
            }

            prepareAsync()
        }
    }

    fun onPlayClicked() {

        val player = mediaPlayer ?: return

        when (_state.value?.playbackState) {

            PlayerPlaybackState.PREPARED,
            PlayerPlaybackState.PAUSED -> {

                player.start()

                _state.value = _state.value?.copy(
                    playbackState = PlayerPlaybackState.PLAYING
                )

                updateProgress()
            }

            PlayerPlaybackState.PLAYING -> {

                player.pause()

                _state.value = _state.value?.copy(
                    playbackState = PlayerPlaybackState.PAUSED
                )

                handler.removeCallbacksAndMessages(null)
            }

            else -> Unit
        }
    }

    fun onPause() {

        val player = mediaPlayer ?: return

        if (player.isPlaying) {
            player.pause()

            _state.value = _state.value?.copy(
                playbackState = PlayerPlaybackState.PAUSED
            )
        }

        handler.removeCallbacksAndMessages(null)
    }

    private fun updateProgress() {

        val player = mediaPlayer ?: return

        if (!player.isPlaying) {
            return
        }

        _state.value = _state.value?.copy(
            progress = player.currentPosition
        )

        handler.postDelayed(
            {
                updateProgress()
            },
            300
        )
    }

    override fun onCleared() {

        handler.removeCallbacksAndMessages(null)

        mediaPlayer?.release()
        mediaPlayer = null

        super.onCleared()
    }
}