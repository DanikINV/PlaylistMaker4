package com.example.playlistmaker.presentation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.activities.PlayerActivity
import com.example.playlistmaker.presentation.adapters.TrackAdapter
import com.example.playlistmaker.presentation.model.PlaylistScreenState
import com.example.playlistmaker.presentation.model.PlaylistViewModel
import com.example.playlistmaker.presentation.model.TrackParcelable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class PlaylistFragment :
    Fragment(R.layout.fragment_playlist) {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModel()

    private lateinit var trackAdapter: TrackAdapter

    private lateinit var menuSheetBehavior:
            BottomSheetBehavior<LinearLayout>

    private lateinit var menuSheetCallback:
            BottomSheetBehavior.BottomSheetCallback

    private val playlistId: Long
        get() = requireArguments()
            .getLong("playlistId")

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        _binding =
            FragmentPlaylistBinding.bind(view)

        binding.playlistMenuSheet.visibility =
            View.GONE

        binding.menuDimView.visibility =
            View.GONE

        binding.menuDimView.alpha =
            0f

        setupRecyclerView()
        setupButtons()
        setupMenuBottomSheet()
        observeState()

        hideMenu()

        viewModel.loadPlaylist(
            playlistId
        )
    }

    private fun setupRecyclerView() {

        trackAdapter =
            TrackAdapter(
                onTrackClick = { track ->
                    openPlayer(track)
                },
                onTrackLongClick = { track ->
                    showDeleteTrackDialog(track)
                }
            )

        binding.rvPlaylistTracks.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        binding.rvPlaylistTracks.adapter =
            trackAdapter
    }

    private fun setupButtons() {

        binding.btnBack.setOnClickListener {

            findNavController()
                .navigateUp()
        }

        binding.btnShare.setOnClickListener {

            val state =
                viewModel.state.value
                    ?: return@setOnClickListener

            val playlist =
                state.playlist
                    ?: return@setOnClickListener

            sharePlaylist(
                playlist.name,
                playlist.description,
                state.tracks
            )
        }

        binding.btnMenu.setOnClickListener {
            showMenu()
        }

        binding.menuDimView.setOnClickListener {
            closeMenu()
        }
    }

    private fun setupMenuBottomSheet() {

        menuSheetBehavior =
            BottomSheetBehavior.from(
                binding.playlistMenuSheet
            )

        menuSheetBehavior.isHideable =
            true

        menuSheetBehavior.skipCollapsed =
            true

        menuSheetBehavior.state =
            BottomSheetBehavior.STATE_HIDDEN

        binding.playlistMenuSheet.visibility =
            View.GONE

        binding.menuDimView.visibility =
            View.GONE

        binding.menuDimView.alpha =
            0f

        binding.btnMenuShare.setOnClickListener {

            val state =
                viewModel.state.value
                    ?: return@setOnClickListener

            val playlist =
                state.playlist
                    ?: return@setOnClickListener

            closeMenu()

            sharePlaylist(
                playlist.name,
                playlist.description,
                state.tracks
            )
        }

        binding.btnMenuEdit.setOnClickListener {

            closeMenu()

            findNavController().navigate(
                R.id.action_global_createPlaylistFragment,
                bundleOf(
                    "playlistId" to playlistId
                )
            )
        }

        binding.btnMenuDelete.setOnClickListener {

            closeMenu()

            showDeletePlaylistDialog()
        }

        menuSheetCallback =
            object :
                BottomSheetBehavior.BottomSheetCallback() {

                override fun onStateChanged(
                    bottomSheet: View,
                    newState: Int
                ) {

                    val currentBinding =
                        _binding
                            ?: return

                    if (
                        newState ==
                        BottomSheetBehavior.STATE_HIDDEN
                    ) {

                        currentBinding
                            .playlistMenuSheet
                            .visibility =
                            View.GONE

                        currentBinding
                            .menuDimView
                            .visibility =
                            View.GONE

                        currentBinding
                            .menuDimView
                            .alpha =
                            0f
                    }
                }

                override fun onSlide(
                    bottomSheet: View,
                    slideOffset: Float
                ) {
                }
            }

        menuSheetBehavior.addBottomSheetCallback(
            menuSheetCallback
        )
    }

    private fun showMenu() {

        val currentBinding =
            _binding
                ?: return

        currentBinding
            .playlistMenuSheet
            .visibility =
            View.VISIBLE

        currentBinding
            .menuDimView
            .visibility =
            View.VISIBLE

        currentBinding
            .menuDimView
            .alpha =
            1f

        currentBinding
            .menuDimView
            .bringToFront()

        currentBinding
            .playlistMenuSheet
            .bringToFront()

        menuSheetBehavior.state =
            BottomSheetBehavior.STATE_EXPANDED
    }

    private fun closeMenu() {

        if (
            ::menuSheetBehavior.isInitialized
        ) {

            menuSheetBehavior.state =
                BottomSheetBehavior.STATE_HIDDEN
        }

        _binding?.playlistMenuSheet?.visibility =
            View.GONE

        _binding?.menuDimView?.visibility =
            View.GONE

        _binding?.menuDimView?.alpha =
            0f
    }

    private fun hideMenu() {

        if (
            ::menuSheetBehavior.isInitialized
        ) {

            menuSheetBehavior.state =
                BottomSheetBehavior.STATE_HIDDEN
        }

        _binding?.playlistMenuSheet?.visibility =
            View.GONE

        _binding?.menuDimView?.visibility =
            View.GONE

        _binding?.menuDimView?.alpha =
            0f
    }

    private fun observeState() {

        viewModel.state.observe(
            viewLifecycleOwner
        ) { state ->

            renderState(state)
        }

        viewModel.playlistDeleted.observe(
            viewLifecycleOwner
        ) { deleted ->

            if (deleted) {

                findNavController()
                    .navigateUp()
            }
        }
    }

    private fun renderState(
        state: PlaylistScreenState
    ) {

        val playlist =
            state.playlist
                ?: return

        binding.tvPlaylistName.text =
            playlist.name

        if (
            playlist.description.isNullOrBlank()
        ) {

            binding.tvPlaylistDescription.visibility =
                View.GONE

        } else {

            binding.tvPlaylistDescription.visibility =
                View.VISIBLE

            binding.tvPlaylistDescription.text =
                playlist.description
        }

        binding.tvPlaylistInfo.text =
            getString(
                R.string.playlist_info_format,
                state.duration,
                state.tracksCount
            )

        trackAdapter.setTracks(
            state.tracks
        )

        showPlaylistCover(
            playlist.coverPath
        )

        showMenuPlaylistCover(
            playlist.coverPath
        )

        binding.tvMenuPlaylistName.text =
            playlist.name

        binding.tvMenuPlaylistInfo.text =
            getString(
                R.string.playlist_tracks_count,
                state.tracksCount
            )

        if (
            state.tracks.isEmpty()
        ) {

            binding.playlistTracksSheet.visibility =
                View.VISIBLE

            binding.tvEmptyPlaylist.visibility =
                View.VISIBLE

            binding.rvPlaylistTracks.visibility =
                View.GONE

        } else {

            binding.playlistTracksSheet.visibility =
                View.VISIBLE

            binding.tvEmptyPlaylist.visibility =
                View.GONE

            binding.rvPlaylistTracks.visibility =
                View.VISIBLE
        }
    }

    private fun showPlaylistCover(
        coverPath: String?
    ) {

        binding.ivPlaylistCover.visibility =
            View.VISIBLE

        if (
            coverPath.isNullOrBlank()
        ) {

            binding.ivPlaylistCover
                .setImageResource(
                    R.drawable.vector
                )

            return
        }

        val file =
            File(coverPath)

        if (
            !file.exists()
        ) {

            binding.ivPlaylistCover
                .setImageResource(
                    R.drawable.vector
                )

            return
        }

        Glide.with(this)
            .load(file)
            .centerCrop()
            .into(
                binding.ivPlaylistCover
            )
    }

    private fun showMenuPlaylistCover(
        coverPath: String?
    ) {

        binding.ivMenuPlaylistCover.visibility =
            View.VISIBLE

        if (
            coverPath.isNullOrBlank()
        ) {

            binding.ivMenuPlaylistCover
                .setImageResource(
                    R.drawable.vector
                )

            return
        }

        val file =
            File(coverPath)

        if (
            !file.exists()
        ) {

            binding.ivMenuPlaylistCover
                .setImageResource(
                    R.drawable.vector
                )

            return
        }

        Glide.with(this)
            .load(file)
            .centerCrop()
            .into(
                binding.ivMenuPlaylistCover
            )
    }

    private fun openPlayer(
        track: Track
    ) {

        val intent =
            Intent(
                requireContext(),
                PlayerActivity::class.java
            ).apply {

                putExtra(
                    "track",
                    TrackParcelable.fromDomain(
                        track
                    )
                )
            }

        startActivity(intent)
    }

    private fun showDeleteTrackDialog(
        track: Track
    ) {

        AlertDialog.Builder(
            requireContext()
        )
            .setTitle(
                getString(
                    R.string.delete_track_title
                )
            )
            .setMessage(
                getString(
                    R.string.delete_track_message,
                    track.trackName
                )
            )
            .setNegativeButton(
                getString(
                    R.string.no
                ),
                null
            )
            .setPositiveButton(
                getString(
                    R.string.yes
                )
            ) { _, _ ->

                viewModel.deleteTrack(
                    playlistId,
                    track.trackId
                )
            }
            .show()
    }

    private fun showDeletePlaylistDialog() {

        AlertDialog.Builder(
            requireContext()
        )
            .setTitle(
                getString(
                    R.string.delete_playlist_title
                )
            )
            .setMessage(
                getString(
                    R.string.delete_playlist_message
                )
            )
            .setNegativeButton(
                getString(
                    R.string.no
                ),
                null
            )
            .setPositiveButton(
                getString(
                    R.string.yes
                )
            ) { _, _ ->

                viewModel.deletePlaylist(
                    playlistId
                )
            }
            .show()
    }

    private fun sharePlaylist(
        name: String,
        description: String?,
        tracks: List<Track>
    ) {

        if (
            tracks.isEmpty()
        ) {

            AlertDialog.Builder(
                requireContext()
            )
                .setMessage(
                    getString(
                        R.string.playlist_empty_share
                    )
                )
                .setPositiveButton(
                    getString(
                        R.string.yes
                    ),
                    null
                )
                .show()

            return
        }

        val text =
            buildString {

                append(name)
                append("\n")

                if (
                    !description.isNullOrBlank()
                ) {

                    append(description)
                    append("\n")
                }

                append(
                    resources.getQuantityString(
                        R.plurals.tracks_count,
                        tracks.size,
                        tracks.size
                    )
                )

                append("\n\n")

                tracks.forEachIndexed { index, track ->

                    val minutes =
                        track.trackTime / 60000

                    val seconds =
                        (track.trackTime % 60000) / 1000

                    val formattedTime =
                        String.format(
                            "%02d:%02d",
                            minutes,
                            seconds
                        )

                    append(
                        getString(
                            R.string.track_share_format,
                            index + 1,
                            track.artistName,
                            track.trackName,
                            formattedTime
                        )
                    )

                    append("\n")
                }
            }

        val intent =
            Intent(
                Intent.ACTION_SEND
            ).apply {

                type =
                    "text/plain"

                putExtra(
                    Intent.EXTRA_TEXT,
                    text
                )
            }

        startActivity(
            Intent.createChooser(
                intent,
                getString(
                    R.string.share_playlist
                )
            )
        )
    }

    override fun onDestroyView() {

        if (
            ::menuSheetCallback.isInitialized
        ) {

            menuSheetBehavior
                .removeBottomSheetCallback(
                    menuSheetCallback
                )
        }

        super.onDestroyView()

        _binding = null
    }
}