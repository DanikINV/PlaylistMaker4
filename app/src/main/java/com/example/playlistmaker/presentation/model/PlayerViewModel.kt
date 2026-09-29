package com.example.playlistmaker.presentation.model

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactors.PlayerInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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

    private var progressJob: Job? = null

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

                progressJob?.cancel()
            }

            setOnErrorListener { _, _, _ ->

                progressJob?.cancel()

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

                progressJob?.cancel()
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

        progressJob?.cancel()
    }

    private fun updateProgress() {

        progressJob?.cancel()

        progressJob = viewModelScope.launch {

            while (isActive) {

                val player = mediaPlayer ?: break

                if (!player.isPlaying) {
                    break
                }

                _state.value = _state.value?.copy(
                    progress = player.currentPosition
                )

                delay(300)
            }
        }
    }

    override fun onCleared() {

        progressJob?.cancel()

        mediaPlayer?.release()
        mediaPlayer = null

        super.onCleared()
    }
}