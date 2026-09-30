@file:Suppress("DEPRECATION")

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.adapter.PlaylistSheetAdapter
import com.example.playlistmaker.presentation.fragments.CreatePlaylistFragment
import com.example.playlistmaker.presentation.model.PlayerPlaybackState
import com.example.playlistmaker.presentation.model.PlayerScreenState
import com.example.playlistmaker.presentation.model.PlayerViewModel
import com.example.playlistmaker.presentation.model.TrackParcelable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.text.SimpleDateFormat
import java.util.Locale
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerActivity : AppCompatActivity() {

    private lateinit var track: Track

    private val viewModel: PlayerViewModel by viewModel {
        parametersOf(track)
    }

    private lateinit var btnPlay: ImageView
    private lateinit var btnFavorite: ImageView
    private lateinit var tvProgress: TextView

    private lateinit var playlistBottomSheet: View
    private lateinit var playlistOverlay: View
    private lateinit var playlistRecyclerView: RecyclerView
    private lateinit var playlistCreatedNotification: TextView
    private lateinit var playerFragmentContainer: View

    private lateinit var bottomSheetBehavior:
            BottomSheetBehavior<View>

    private lateinit var playlistSheetAdapter:
            PlaylistSheetAdapter

    private val timeFormat =
        SimpleDateFormat(
            "mm:ss",
            Locale.getDefault()
        )

    private var boundTrackId: Long? = null


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContentView(
            R.layout.activity_player
        )

        val trackParcelable =
            intent.getParcelableExtra<TrackParcelable>(
                EXTRA_TRACK
            )

        if (trackParcelable == null) {

            finish()
            return
        }

        track =
            trackParcelable.toDomain()
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

        btnPlay =
            findViewById(
                R.id.btn_play
            )

        btnFavorite =
            findViewById(
                R.id.btn_favorite
            )

        tvProgress =
            findViewById(
                R.id.tv_progress
            )

        playlistBottomSheet =
            findViewById(
                R.id.playlist_bottom_sheet
            )

        playlistOverlay =
            findViewById(
                R.id.playlist_overlay
            )

        playlistRecyclerView =
            findViewById(
                R.id.rv_playlist_sheet
            )

        playlistCreatedNotification =
            findViewById(
                R.id.playlist_created_notification
            )

        playerFragmentContainer =
            findViewById(
                R.id.player_fragment_container
            )

        playlistCreatedNotification.visibility =
            View.GONE

        playerFragmentContainer.visibility =
            View.GONE

        setupPlaylistBottomSheet()

        findViewById<ImageView>(
            R.id.btn_back
        ).setOnClickListener {

            onBackPressedDispatcher
                .onBackPressed()
        }

        viewModel.preparePlayer()

        btnPlay.setOnClickListener {

            viewModel.onPlayClicked()
        }

        btnFavorite.setOnClickListener {

            viewModel.onFavoriteClicked()
        }

        findViewById<ImageView>(
            R.id.btn_add_to_playlist
        ).setOnClickListener {

            openPlaylistBottomSheet()
        }

        viewModel.state.observe(
            this
        ) { state ->

            renderState(
                state
            )
        }

        viewModel.playlists.observe(
            this
        ) { playlists ->

            playlistSheetAdapter.setPlaylists(
                playlists
            )
        }

        viewModel.playlistAdded.observe(
            this
        ) { message ->

            showPlaylistNotification(
                message
            )
        }
    }

    private fun setupPlaylistBottomSheet() {

        playlistSheetAdapter =
            PlaylistSheetAdapter { playlist ->

                viewModel.addTrackToPlaylist(
                    playlist
                )

                bottomSheetBehavior.state =
                    BottomSheetBehavior.STATE_HIDDEN

                playlistBottomSheet.visibility =
                    View.GONE

                playlistOverlay.visibility =
                    View.GONE
            }

        playlistRecyclerView.layoutManager =
            LinearLayoutManager(this)

        playlistRecyclerView.adapter =
            playlistSheetAdapter

        bottomSheetBehavior =
            BottomSheetBehavior.from(
                playlistBottomSheet
            ).apply {

                state =
                    BottomSheetBehavior.STATE_HIDDEN

                isHideable =
                    true

                skipCollapsed =
                    true

                isFitToContents =
                    true
            }

        findViewById<View>(
            R.id.btn_new_playlist
        ).setOnClickListener {

            bottomSheetBehavior.state =
                BottomSheetBehavior.STATE_HIDDEN

            playlistBottomSheet.visibility =
                View.GONE

            playlistOverlay.visibility =
                View.GONE

            playlistOverlay.alpha =
                0f

            playerFragmentContainer.visibility =
                View.VISIBLE

            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.player_fragment_container,
                    CreatePlaylistFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        bottomSheetBehavior.addBottomSheetCallback(

            object :
                BottomSheetBehavior.BottomSheetCallback() {

                override fun onStateChanged(
                    bottomSheet: View,
                    newState: Int
                ) {

                    if (
                        newState ==
                        BottomSheetBehavior.STATE_HIDDEN
                    ) {

                        playlistOverlay.visibility =
                            View.GONE

                        playlistOverlay.alpha =
                            0f

                    } else {

                        playlistBottomSheet.visibility =
                            View.VISIBLE

                        playlistOverlay.visibility =
                            View.VISIBLE

                        playlistOverlay.alpha =
                            1f
                    }
                }

                override fun onSlide(
                    bottomSheet: View,
                    slideOffset: Float
                ) {

                    playlistOverlay.alpha =
                        slideOffset
                            .coerceIn(0f, 1f)
                }
            }
        )

        playlistOverlay.setOnClickListener {

            bottomSheetBehavior.state =
                BottomSheetBehavior.STATE_HIDDEN

            playlistBottomSheet.visibility =
                View.GONE
        }
    }
    private fun openPlaylistBottomSheet() {

        playlistBottomSheet.visibility =
            View.VISIBLE

        viewModel.loadPlaylists()

        playlistOverlay.visibility =
            View.VISIBLE

        playlistOverlay.alpha =
            1f

        playlistBottomSheet.post {

            bottomSheetBehavior.state =
                BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun showPlaylistNotification(
        message: String
    ) {


        playlistCreatedNotification.text =
            message

        playlistCreatedNotification.visibility =
            View.VISIBLE

        playlistCreatedNotification.alpha =
            1f

        playlistCreatedNotification.bringToFront()

        playlistCreatedNotification.postDelayed({

            playlistCreatedNotification
                .animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction {

                    playlistCreatedNotification.visibility =
                        View.GONE

                    playlistCreatedNotification.alpha =
                        1f
                }
                .start()

        }, 2500)
    }

    private fun renderState(
        state: PlayerScreenState
    ) {

        if (
            boundTrackId !=
            state.track.trackId
        ) {

            bindTrack(
                state.track
            )

            boundTrackId =
                state.track.trackId
        }

        tvProgress.text =
            timeFormat.format(
                state.progress
            )

        btnPlay.isEnabled =
            state.isPlayEnabled

        btnFavorite.setImageResource(

            if (state.track.isFavorite) {

                R.drawable.ic_favorite

            } else {

                R.drawable.ic_favorite_border
            }
        )

        when (
            state.playbackState
        ) {

            PlayerPlaybackState.PLAYING -> {

                setPlayButtonIcon(
                    true
                )
            }

            PlayerPlaybackState.PREPARED,
            PlayerPlaybackState.PAUSED,
            PlayerPlaybackState.DEFAULT -> {

                setPlayButtonIcon(
                    false
                )
            }
        }
    }

    private fun bindTrack(
        track: Track
    ) {

        findViewById<TextView>(
            R.id.tv_track_name
        ).text =
            track.trackName

        findViewById<TextView>(
            R.id.tv_artist_name
        ).text =
            track.artistName

        findViewById<TextView>(
            R.id.tv_duration_value
        ).text =
            timeFormat.format(
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
            findViewById<View>(
                rowId
            )

        if (value.isNullOrBlank()) {

            row.visibility =
                View.GONE

        } else {

            row.visibility =
                View.VISIBLE

            findViewById<TextView>(
                valueViewId
            ).text =
                value
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

        return releaseDate.take(4)
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

        const val EXTRA_TRACK =
            "EXTRA_TRACK"
    }
}