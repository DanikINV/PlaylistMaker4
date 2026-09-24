package com.example.playlistmaker.presentation.activities

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.model.TrackParcelable
import com.example.playlistmaker.presentation.model.PlayerPlaybackState
import com.example.playlistmaker.presentation.model.PlayerScreenState
import com.example.playlistmaker.presentation.model.PlayerViewModel
import com.example.playlistmaker.presentation.model.PlayerViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private lateinit var viewModel: PlayerViewModel

    private lateinit var btnPlay: ImageView
    private lateinit var tvProgress: TextView

    private val timeFormat =
        SimpleDateFormat(
            "mm:ss",
            Locale.getDefault()
        )

    private var boundTrackId: Long? = null

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
            isAppearanceLightStatusBars =
                !isNightMode
        }

        val rootView =
            findViewById<NestedScrollView>(
                R.id.root_layout
            )

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView
        ) { view, insets ->

            val systemBars =
                insets.getInsets(
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

        btnPlay = findViewById(
            R.id.btn_play
        )

        tvProgress = findViewById(
            R.id.tv_progress
        )

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

        val track =
            trackParcelable.toDomain()

        val factory =
            PlayerViewModelFactory(track)

        viewModel = ViewModelProvider(
            this,
            factory
        )[PlayerViewModel::class.java]

        btnPlay.setOnClickListener {
            viewModel.onPlayClicked()
        }

        viewModel.state.observe(
            this
        ) { state ->
            renderState(state)
        }
    }

    private fun renderState(
        state: PlayerScreenState
    ) {

        if (
            boundTrackId != state.track.trackId
        ) {

            bindTrack(state.track)

            boundTrackId =
                state.track.trackId
        }

        tvProgress.text =
            timeFormat.format(
                state.progress
            )

        btnPlay.isEnabled =
            state.isPlayEnabled

        when (
            state.playbackState
        ) {

            PlayerPlaybackState.PLAYING -> {
                setPlayButtonIcon(true)
            }

            PlayerPlaybackState.PREPARED,
            PlayerPlaybackState.PAUSED,
            PlayerPlaybackState.DEFAULT -> {
                setPlayButtonIcon(false)
            }
        }
    }

    private fun bindTrack(
        track: Track
    ) {

        findViewById<TextView>(
            R.id.tv_track_name
        ).text = track.trackName

        findViewById<TextView>(
            R.id.tv_artist_name
        ).text = track.artistName

        findViewById<TextView>(
            R.id.tv_duration_value
        ).text = timeFormat.format(
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
            extractYear(
                track.releaseDate
            )
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
            .load(
                track.getCoverArtwork()
            )
            .placeholder(
                R.drawable.vector
            )
            .error(
                R.drawable.vector
            )
            .transform(
                MultiTransformation(
                    CenterCrop(),
                    RoundedCorners(
                        cornerRadiusPx
                    )
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

        val row =
            findViewById<View>(rowId)

        if (value.isNullOrBlank()) {

            row.visibility =
                View.GONE

        } else {

            row.visibility =
                View.VISIBLE

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

        return releaseDate.substring(
            0,
            4
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

    override fun onPause() {
        super.onPause()

        viewModel.onPause()
    }

    companion object {

        const val EXTRA_TRACK = "EXTRA_TRACK"
    }
}