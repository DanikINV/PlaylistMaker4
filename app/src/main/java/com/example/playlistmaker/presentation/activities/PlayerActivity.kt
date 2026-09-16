package com.example.playlistmaker.presentation.activities

import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.NestedScrollView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.model.TrackParcelable
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var playerState = STATE_DEFAULT
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var btnPlay: ImageView
    private lateinit var tvProgress: TextView

    private val timeFormat =
        SimpleDateFormat("mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContentView(R.layout.activity_player)

        val isNightMode =
            resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = !isNightMode
        }

        val rootView =
            findViewById<NestedScrollView>(
                R.id.root_layout
            )

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView
        ) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                0,
                systemBars.top,
                0,
                systemBars.bottom
            )

            insets
        }

        btnPlay = findViewById(R.id.btn_play)
        tvProgress = findViewById(R.id.tv_progress)

        findViewById<ImageView>(
            R.id.btn_back
        ).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val trackParcelable =
            intent.getParcelableExtra<TrackParcelable>(
                EXTRA_TRACK
            ) ?: run {
                finish()
                return
            }

        val track = trackParcelable.toDomain()

        bindTrack(track)
        preparePlayer(track.previewUrl)

        btnPlay.setOnClickListener {
            playbackControl()
        }
    }

    private fun bindTrack(track: Track) {

        findViewById<TextView>(
            R.id.tv_track_name
        ).text = track.trackName

        findViewById<TextView>(
            R.id.tv_artist_name
        ).text = track.artistName

        val durationFormat =
            SimpleDateFormat(
                "mm:ss",
                Locale.getDefault()
            )

        findViewById<TextView>(
            R.id.tv_duration_value
        ).text = durationFormat.format(
            track.trackTime
        )

        bindOptionalRow(
            R.id.row_album,
            R.id.tv_album_value,
            track.collectionName
        )

        bindOptionalRow(
            R.id.row_year,
            R.id.tv_year_value,
            extractYear(track.releaseDate)
        )

        bindOptionalRow(
            R.id.row_genre,
            R.id.tv_genre_value,
            track.primaryGenreName
        )

        bindOptionalRow(
            R.id.row_country,
            R.id.tv_country_value,
            track.country
        )

        val cornerRadiusPx =
            resources.getDimensionPixelSize(
                R.dimen.player_artwork_corner_radius
            )

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.vector)
            .error(R.drawable.vector)
            .transform(
                MultiTransformation(
                    CenterCrop(),
                    RoundedCorners(cornerRadiusPx)
                )
            )
            .into(
                findViewById(
                    R.id.iv_artwork
                )
            )
    }

    private fun bindOptionalRow(
        rowId: Int,
        valueViewId: Int,
        value: String?
    ) {

        val row = findViewById<View>(rowId)

        if (value.isNullOrBlank()) {

            row.visibility = View.GONE

        } else {

            row.visibility = View.VISIBLE

            findViewById<TextView>(
                valueViewId
            ).text = value
        }
    }

    private fun extractYear(
        releaseDate: String?
    ): String? {

        if (
            releaseDate.isNullOrBlank() ||
            releaseDate.length < 4
        ) {
            return null
        }

        return releaseDate.substring(0, 4)
    }

    private fun preparePlayer(
        previewUrl: String?
    ) {

        if (previewUrl.isNullOrBlank()) {

            btnPlay.isEnabled = false

            return
        }

        mediaPlayer = MediaPlayer().apply {

            setDataSource(previewUrl)

            prepareAsync()

            setOnPreparedListener {

                playerState = STATE_PREPARED

                btnPlay.isEnabled = true
            }

            setOnCompletionListener {

                mainHandler.removeCallbacks(
                    progressUpdateRunnable
                )

                tvProgress.text =
                    getString(
                        R.string.default_progress
                    )

                setPlayButtonIcon(
                    isPlaying = false
                )

                playerState = STATE_PREPARED
            }

            setOnErrorListener { _, _, _ ->

                playerState = STATE_DEFAULT
                btnPlay.isEnabled = false

                mainHandler.removeCallbacks(
                    progressUpdateRunnable
                )

                false
            }
        }
    }

    private fun playbackControl() {

        when (playerState) {

            STATE_PLAYING -> {
                pausePlayer()
            }

            STATE_PREPARED,
            STATE_PAUSED -> {
                startPlayer()
            }

            else -> Unit
        }
    }

    private fun startPlayer() {

        mediaPlayer?.start()

        setPlayButtonIcon(
            isPlaying = true
        )

        playerState = STATE_PLAYING

        mainHandler.post(
            progressUpdateRunnable
        )
    }

    private fun pausePlayer() {

        mediaPlayer?.pause()

        setPlayButtonIcon(
            isPlaying = false
        )

        playerState = STATE_PAUSED

        mainHandler.removeCallbacks(
            progressUpdateRunnable
        )
    }

    private fun setPlayButtonIcon(
        isPlaying: Boolean
    ) {

        btnPlay.setImageResource(
            if (isPlaying) {
                R.drawable.ic_pause_button
            } else {
                R.drawable.ic_play_button
            }
        )
    }

    private val progressUpdateRunnable =
        object : Runnable {

            override fun run() {

                mediaPlayer?.let {
                    tvProgress.text =
                        timeFormat.format(
                            it.currentPosition
                        )
                }

                mainHandler.postDelayed(
                    this,
                    PROGRESS_UPDATE_DELAY_MS
                )
            }
        }

    override fun onPause() {

        super.onPause()

        if (playerState == STATE_PLAYING) {
            pausePlayer()
        }
    }

    override fun onDestroy() {

        super.onDestroy()

        mainHandler.removeCallbacks(
            progressUpdateRunnable
        )

        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {

        const val EXTRA_TRACK = "EXTRA_TRACK"

        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3

        private const val PROGRESS_UPDATE_DELAY_MS = 300L
    }
}