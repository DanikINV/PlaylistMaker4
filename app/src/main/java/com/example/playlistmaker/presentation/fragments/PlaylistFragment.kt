package com.example.playlistmaker.presentation.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.presentation.activities.PlayerActivity
import com.example.playlistmaker.presentation.adapters.TrackAdapter
import com.example.playlistmaker.presentation.model.PlaylistViewModel
import com.example.playlistmaker.presentation.model.TrackParcelable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistFragment :
    Fragment(R.layout.fragment_playlist) {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistViewModel by viewModel()

    private lateinit var trackAdapter: TrackAdapter

    private lateinit var bottomSheetBehavior:
            BottomSheetBehavior<View>

    private lateinit var menuBottomSheetBehavior:
            BottomSheetBehavior<View>

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

        setupWindowInsets()
        setupBackButton()
        setupBottomSheet()
        setupMenuBottomSheet()
        setupShareButton()
        setupMenuButton()
        setupRecyclerView()
        observeState()
        observePlaylistDeleted()

        viewModel.loadPlaylist(
            playlistId
        )
    }

    override fun onResume() {
        super.onResume()

        if (_binding != null) {
            viewModel.loadPlaylist(
                playlistId
            )
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.playlistContent
        ) { root, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            root.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                0
            )

            insets
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            requireActivity()
                .onBackPressedDispatcher
                .onBackPressed()
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior =
            BottomSheetBehavior.from(
                binding.playlistTracksSheet
            )

        bottomSheetBehavior.isHideable = false

        bottomSheetBehavior.state =
            BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setupMenuBottomSheet() {
        menuBottomSheetBehavior =
            BottomSheetBehavior.from(
                binding.playlistMenuSheet
            )

        menuBottomSheetBehavior.isHideable = true
        menuBottomSheetBehavior.skipCollapsed = true

        menuBottomSheetBehavior.state =
            BottomSheetBehavior.STATE_HIDDEN

        menuBottomSheetBehavior.addBottomSheetCallback(
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
                        binding.menuDimView.visibility =
                            View.GONE
                    }
                }

                override fun onSlide(
                    bottomSheet: View,
                    slideOffset: Float
                ) {
                }
            }
        )

        binding.menuDimView.setOnClickListener {
            hideMenu()
        }
    }

    private fun setupShareButton() {
        binding.btnShare.setOnClickListener {
            sharePlaylist()
        }
    }

    private fun setupMenuButton() {
        binding.btnMenu.setOnClickListener {
            showMenu()
        }

        binding.btnMenuShare.setOnClickListener {
            hideMenu()
            sharePlaylist()
        }

        binding.btnMenuEdit.setOnClickListener {
            hideMenu()

            findNavController().navigate(
                R.id.action_playlistFragment_to_editPlaylistFragment,
                bundleOf(
                    "playlistId" to playlistId
                )
            )
        }

        binding.btnMenuDelete.setOnClickListener {
            showDeletePlaylistDialog()
        }
    }

    private fun showMenu() {
        binding.menuDimView.visibility =
            View.VISIBLE

        menuBottomSheetBehavior.state =
            BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideMenu() {
        menuBottomSheetBehavior.state =
            BottomSheetBehavior.STATE_HIDDEN

        binding.menuDimView.visibility =
            View.GONE
    }

    private fun setupRecyclerView() {
        trackAdapter =
            TrackAdapter(
                onTrackClick = { track ->
                    openPlayer(track)
                },
                onTrackLongClick = { track ->
                    showDeleteDialog(track)
                }
            )

        binding.rvPlaylistTracks.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        binding.rvPlaylistTracks.adapter =
            trackAdapter
    }

    private fun observeState() {
        viewModel.state.observe(
            viewLifecycleOwner
        ) { state ->

            val playlist =
                state.playlist
                    ?: return@observe

            binding.tvPlaylistName.text =
                playlist.name

            if (
                playlist.description
                    .isNullOrBlank()
            ) {

                binding.tvPlaylistDescription.visibility =
                    View.GONE

            } else {

                binding.tvPlaylistDescription.visibility =
                    View.VISIBLE

                binding.tvPlaylistDescription.text =
                    playlist.description

                binding.tvPlaylistDescription.setTextColor(
                    Color.BLACK
                )
            }

            binding.tvPlaylistInfo.text =
                getString(
                    R.string.playlist_info,
                    state.duration,
                    state.tracksCount
                )

            showCover(
                playlist.coverPath
            )

            trackAdapter.setTracks(
                state.tracks
            )

            if (state.tracks.isEmpty()) {

                binding.playlistTracksSheet.visibility =
                    View.GONE

            } else {

                binding.playlistTracksSheet.visibility =
                    View.VISIBLE
            }

            binding.tvMenuPlaylistName.text =
                playlist.name

            binding.tvMenuPlaylistInfo.text =
                getString(
                    R.string.playlist_info,
                    state.duration,
                    state.tracksCount
                )

            if (
                playlist.description
                    .isNullOrBlank()
            ) {

                binding.tvMenuPlaylistDescription.visibility =
                    View.GONE

            } else {

                binding.tvMenuPlaylistDescription.visibility =
                    View.VISIBLE

                binding.tvMenuPlaylistDescription.text =
                    playlist.description

                binding.tvMenuPlaylistDescription.setTextColor(
                    Color.BLACK
                )
            }
        }
    }

    private fun observePlaylistDeleted() {
        viewModel.playlistDeleted.observe(
            viewLifecycleOwner
        ) { deleted ->

            if (!deleted) {
                return@observe
            }

            requireActivity()
                .onBackPressedDispatcher
                .onBackPressed()
        }
    }

    private fun sharePlaylist() {

        val state =
            viewModel.state.value
                ?: return

        val playlist =
            state.playlist
                ?: return

        val tracks =
            state.tracks

        if (tracks.isEmpty()) {

            Toast.makeText(
                requireContext(),
                "В этом плейлисте нет списка треков, которым можно поделиться",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val shareText =
            buildShareText(
                playlist.name,
                playlist.description,
                tracks
            )

        val shareIntent =
            Intent(
                Intent.ACTION_SEND
            ).apply {

                type =
                    "text/plain"

                putExtra(
                    Intent.EXTRA_TEXT,
                    shareText
                )
            }

        startActivity(
            Intent.createChooser(
                shareIntent,
                null
            )
        )
    }

    private fun buildShareText(
        playlistName: String,
        description: String?,
        tracks: List<Track>
    ): String {

        val builder =
            StringBuilder()

        builder
            .append(playlistName)
            .append("\n")

        builder
            .append(description.orEmpty())
            .append("\n")

        builder
            .append(tracks.size)
            .append(" треков")
            .append("\n")

        tracks.forEachIndexed { index, track ->

            builder
                .append(index + 1)
                .append(". ")
                .append(track.artistName)
                .append(" - ")
                .append(track.trackName)
                .append(" (")
                .append(
                    formatTrackTime(
                        track.trackTime
                    )
                )
                .append(")")

            if (index < tracks.lastIndex) {
                builder.append("\n")
            }
        }

        return builder.toString()
    }

    private fun formatTrackTime(
        trackTime: Long
    ): String {

        return SimpleDateFormat(
            "mm:ss",
            Locale.getDefault()
        ).format(trackTime)
    }

    private fun showCover(
        coverPath: String?
    ) {

        binding.ivPlaylistCover.setImageResource(
            R.drawable.vector
        )

        if (coverPath.isNullOrEmpty()) {
            return
        }

        val coverFile =
            File(coverPath)

        if (!coverFile.exists()) {
            return
        }

        Glide.with(
            binding.ivPlaylistCover
        )
            .load(coverFile)
            .transform(
                RoundedCorners(
                    (
                            16 *
                                    resources
                                        .displayMetrics
                                        .density
                            ).toInt()
                )
            )
            .placeholder(
                R.drawable.vector
            )
            .error(
                R.drawable.vector
            )
            .into(
                binding.ivPlaylistCover
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
                    PlayerActivity.EXTRA_TRACK,
                    TrackParcelable.fromDomain(
                        track
                    )
                )
            }

        startActivity(intent)
    }

    private fun showDeleteDialog(
        track: Track
    ) {

        val dialog =
            MaterialAlertDialogBuilder(
                requireContext(),
                R.style.DeleteTrackDialogTheme
            )
                .setMessage(
                    "Хотите удалить трек?"
                )
                .setNegativeButton(
                    "НЕТ",
                    null
                )
                .setPositiveButton(
                    "ДА"
                ) { _, _ ->

                    viewModel.deleteTrack(
                        playlistId,
                        track.trackId
                    )
                }
                .create()

        dialog.setOnShowListener {

            dialog.getButton(
                android.app.AlertDialog.BUTTON_NEGATIVE
            ).setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.blue
                )
            )

            dialog.getButton(
                android.app.AlertDialog.BUTTON_POSITIVE
            ).setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.blue
                )
            )
        }

        dialog.show()
    }

    private fun showDeletePlaylistDialog() {

        hideMenu()

        val dialog =
            MaterialAlertDialogBuilder(
                requireContext(),
                R.style.DeleteTrackDialogTheme
            )
                .setTitle(
                    "Удалить плейлист"
                )
                .setMessage(
                    "Хотите удалить плейлист?"
                )
                .setNegativeButton(
                    "Нет",
                    null
                )
                .setPositiveButton(
                    "Да"
                ) { _, _ ->

                    viewModel.deletePlaylist(
                        playlistId
                    )
                }
                .create()

        dialog.setOnShowListener {

            dialog.getButton(
                android.app.AlertDialog.BUTTON_NEGATIVE
            ).setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.blue
                )
            )

            dialog.getButton(
                android.app.AlertDialog.BUTTON_POSITIVE
            ).setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.blue
                )
            )
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}