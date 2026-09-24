package com.example.playlistmaker.presentation.model

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.model.Track

class PlayerViewModel(
    private val track: Track
) : ViewModel() {

    private val _state = MutableLiveData(
        PlayerScreenState(
            track = track
        )
    )

    val state: LiveData<PlayerScreenState> = _state

    private var mediaPlayer: MediaPlayer? = null

    private val mainHandler = Handler(
        Looper.getMainLooper()
    )

    private val progressUpdateRunnable =
        object : Runnable {

            override fun run() {

                val player = mediaPlayer

                if (
                    player != null &&
                    player.isPlaying
                ) {
                    updateState {
                        it.copy(
                            progress = player.currentPosition
                        )
                    }

                    mainHandler.postDelayed(
                        this,
                        PROGRESS_UPDATE_DELAY_MS
                    )
                }
            }
        }

    init {
        preparePlayer()
    }

    private fun preparePlayer() {

        val previewUrl = track.previewUrl

        Log.d(
            TAG,
            "Preview URL = $previewUrl"
        )

        if (previewUrl.isNullOrBlank()) {

            Log.e(
                TAG,
                "Preview URL is empty"
            )

            updateState {
                it.copy(
                    playbackState =
                        PlayerPlaybackState.DEFAULT,
                    isPlayEnabled = false
                )
            }

            return
        }

        try {

            mediaPlayer = MediaPlayer().apply {

                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(
                            AudioAttributes.USAGE_MEDIA
                        )
                        .setContentType(
                            AudioAttributes.CONTENT_TYPE_MUSIC
                        )
                        .build()
                )

                setOnPreparedListener {

                    Log.d(
                        TAG,
                        "MediaPlayer prepared"
                    )

                    updateState {
                        it.copy(
                            playbackState =
                                PlayerPlaybackState.PREPARED,
                            progress = 0,
                            isPlayEnabled = true
                        )
                    }
                }

                setOnCompletionListener {

                    Log.d(
                        TAG,
                        "Playback completed"
                    )

                    mainHandler.removeCallbacks(
                        progressUpdateRunnable
                    )

                    updateState {
                        it.copy(
                            playbackState =
                                PlayerPlaybackState.PREPARED,
                            progress = 0,
                            isPlayEnabled = true
                        )
                    }
                }

                setOnErrorListener { _, what, extra ->

                    Log.e(
                        TAG,
                        "MediaPlayer error: what=$what extra=$extra"
                    )

                    mainHandler.removeCallbacks(
                        progressUpdateRunnable
                    )

                    updateState {
                        it.copy(
                            playbackState =
                                PlayerPlaybackState.DEFAULT,
                            progress = 0,
                            isPlayEnabled = false
                        )
                    }

                    true
                }

                setDataSource(previewUrl)

                prepareAsync()
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "MediaPlayer prepare exception",
                e
            )

            updateState {
                it.copy(
                    playbackState =
                        PlayerPlaybackState.DEFAULT,
                    progress = 0,
                    isPlayEnabled = false
                )
            }
        }
    }

    fun onPlayClicked() {

        val currentState = _state.value
            ?.playbackState

        Log.d(
            TAG,
            "Play clicked. State = $currentState"
        )

        when (currentState) {

            PlayerPlaybackState.PLAYING -> {
                pausePlayer()
            }

            PlayerPlaybackState.PREPARED,
            PlayerPlaybackState.PAUSED -> {
                startPlayer()
            }

            else -> {
                Log.d(
                    TAG,
                    "Player is not ready"
                )
            }
        }
    }

    fun onPause() {

        if (
            _state.value?.playbackState ==
            PlayerPlaybackState.PLAYING
        ) {
            pausePlayer()
        }
    }

    private fun startPlayer() {

        val player = mediaPlayer

        if (player == null) {
            Log.e(
                TAG,
                "MediaPlayer is null"
            )
            return
        }

        try {

            if (!player.isPlaying) {
                player.start()
            }

            updateState {
                it.copy(
                    playbackState =
                        PlayerPlaybackState.PLAYING
                )
            }

            mainHandler.removeCallbacks(
                progressUpdateRunnable
            )

            mainHandler.post(
                progressUpdateRunnable
            )

            Log.d(
                TAG,
                "Playback started"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Start playback exception",
                e
            )

            updateState {
                it.copy(
                    playbackState =
                        PlayerPlaybackState.DEFAULT,
                    isPlayEnabled = false
                )
            }
        }
    }

    private fun pausePlayer() {

        try {

            mediaPlayer?.pause()

            mainHandler.removeCallbacks(
                progressUpdateRunnable
            )

            updateState {
                it.copy(
                    playbackState =
                        PlayerPlaybackState.PAUSED
                )
            }

            Log.d(
                TAG,
                "Playback paused"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Pause playback exception",
                e
            )
        }
    }

    private fun updateState(
        reducer: (
            PlayerScreenState
        ) -> PlayerScreenState
    ) {

        val currentState =
            _state.value
                ?: PlayerScreenState(track)

        _state.value = reducer(
            currentState
        )
    }

    override fun onCleared() {

        mainHandler.removeCallbacks(
            progressUpdateRunnable
        )

        mediaPlayer?.release()
        mediaPlayer = null

        Log.d(
            TAG,
            "MediaPlayer released"
        )

        super.onCleared()
    }

    companion object {

        private const val TAG = "PlayerViewModel"

        private const val PROGRESS_UPDATE_DELAY_MS = 300L
    }
}