package com.example.playlistmaker.presentation.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.domain.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.File

class CreatePlaylistFragment :
    Fragment(R.layout.fragment_create_playlist) {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private val playlistInteractor:
            PlaylistInteractor by inject()

    private var selectedImageUri: Uri? = null

    private var currentPlaylist: Playlist? = null

    private val editPlaylistId: Long
        get() = requireArguments()
            .getLong(
                "playlistId",
                -1L
            )

    private val isEditMode: Boolean
        get() =
            editPlaylistId != -1L

    private val pickImageLauncher =
        registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->

            if (uri != null) {

                selectedImageUri =
                    uri

                showSelectedImage(
                    uri
                )

                updateButton()
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        _binding =
            FragmentCreatePlaylistBinding.bind(
                view
            )

        setupWindowInsets()
        setupBackButton()
        setupSystemBack()
        setupImagePicker()
        setupNameField()
        setupButton()

        if (isEditMode) {

            binding.tvTitle.text =
                "Редактировать"

            binding.btnCreatePlaylist.text =
                "Сохранить"

            loadPlaylist()

        } else {

            binding.tvTitle.text =
                getString(
                    R.string.new_playlist
                )

            binding.btnCreatePlaylist.text =
                getString(
                    R.string.create_new_playlist
                )

            updateButton()
        }
    }

    private fun setupWindowInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { _, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            binding.btnBack.updateLayoutParams<
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                    > {

                topMargin =
                    systemBars.top +
                            (
                                    8 *
                                            resources
                                                .displayMetrics
                                                .density
                                    ).toInt()
            }

            insets
        }

        ViewCompat.requestApplyInsets(
            binding.root
        )
    }

    private fun setupBackButton() {

        binding.btnBack.setOnClickListener {

            if (isEditMode) {

                findNavController()
                    .navigateUp()

            } else {

                handleBack()
            }
        }
    }

    private fun setupSystemBack() {

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object :
                    OnBackPressedCallback(true) {

                    override fun handleOnBackPressed() {

                        if (isEditMode) {

                            findNavController()
                                .navigateUp()

                        } else {

                            handleBack()
                        }
                    }
                }
            )
    }

    private fun setupImagePicker() {

        binding.playlistCoverContainer
            .setOnClickListener {

                pickImageLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts
                            .PickVisualMedia
                            .ImageOnly
                    )
                )
            }
    }

    private fun setupNameField() {

        binding.etPlaylistName
            .doAfterTextChanged {

                updateButton()
            }
    }

    private fun setupButton() {

        binding.btnCreatePlaylist
            .setOnClickListener {

                if (isEditMode) {

                    saveEditedPlaylist()

                } else {

                    createPlaylist()
                }
            }

        binding.btnCreatePlaylist.setTextColor(
            resources.getColor(
                R.color.white,
                requireContext().theme
            )
        )
    }

    private fun updateButton() {

        val isEnabled =
            binding.etPlaylistName.text
                .toString()
                .trim()
                .isNotBlank()

        binding.btnCreatePlaylist.isEnabled =
            isEnabled

        binding.btnCreatePlaylist.background =
            if (isEnabled) {

                resources.getDrawable(
                    R.drawable.bg_button_enabled,
                    requireContext().theme
                )

            } else {

                resources.getDrawable(
                    R.drawable.bg_button_disabled,
                    requireContext().theme
                )
            }

        binding.btnCreatePlaylist.setTextColor(
            resources.getColor(
                R.color.white_crate_playlist,
                requireContext().theme
            )
        )
    }

    private fun loadPlaylist() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                val playlist =
                    playlistInteractor.getPlaylist(
                        editPlaylistId
                    )

                if (!isAdded) {
                    return@launch
                }

                if (playlist == null) {

                    findNavController()
                        .navigateUp()

                    return@launch
                }

                currentPlaylist =
                    playlist

                binding.etPlaylistName
                    .setText(
                        playlist.name
                    )

                binding.etPlaylistDescription
                    .setText(
                        playlist.description
                            .orEmpty()
                    )

                showCurrentCover(
                    playlist.coverPath
                )

                updateButton()
            }
    }

    private fun createPlaylist() {

        val playlistName =
            binding.etPlaylistName.text
                .toString()
                .trim()

        if (playlistName.isBlank()) {
            return
        }

        val playlistDescription =
            binding.etPlaylistDescription.text
                .toString()
                .trim()
                .ifBlank {
                    null
                }

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                val copiedCoverPath =
                    selectedImageUri?.let { uri ->
                        copyImageToInternalStorage(
                            uri
                        )
                    }

                val playlist =
                    Playlist(
                        name = playlistName,
                        description =
                            playlistDescription,
                        coverPath =
                            copiedCoverPath,
                        trackIds =
                            emptyList(),
                        tracksCount =
                            0
                    )

                playlistInteractor
                    .createPlaylist(
                        playlist
                    )

                parentFragmentManager
                    .setFragmentResult(
                        "playlist_created",
                        bundleOf(
                            "playlist_name" to
                                    playlistName
                        )
                    )

                findNavController()
                    .navigateUp()
            }
    }

    private fun saveEditedPlaylist() {

        val playlist =
            currentPlaylist
                ?: return

        val playlistName =
            binding.etPlaylistName.text
                .toString()
                .trim()

        if (playlistName.isBlank()) {
            return
        }

        val playlistDescription =
            binding.etPlaylistDescription.text
                .toString()
                .trim()
                .ifBlank {
                    null
                }

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                val coverPath =
                    if (
                        selectedImageUri != null
                    ) {

                        copyImageToInternalStorage(
                            selectedImageUri!!
                        ) ?: playlist.coverPath

                    } else {

                        playlist.coverPath
                    }

                val updatedPlaylist =
                    playlist.copy(
                        name =
                            playlistName,
                        description =
                            playlistDescription,
                        coverPath =
                            coverPath
                    )

                playlistInteractor
                    .updatePlaylist(
                        updatedPlaylist
                    )

                findNavController()
                    .navigateUp()
            }
    }

    private suspend fun copyImageToInternalStorage(
        uri: Uri
    ): String? {

        val context =
            requireContext()

        return withContext(
            Dispatchers.IO
        ) {

            try {

                val coversDirectory =
                    File(
                        context.filesDir,
                        "playlist_covers"
                    )

                if (
                    !coversDirectory.exists()
                ) {

                    coversDirectory.mkdirs()
                }

                val extension =
                    getFileExtension(
                        uri
                    )

                val fileName =
                    "playlist_cover_${System.currentTimeMillis()}$extension"

                val destinationFile =
                    File(
                        coversDirectory,
                        fileName
                    )

                val inputStream =
                    context.contentResolver
                        .openInputStream(uri)
                        ?: return@withContext null

                inputStream.use { input ->

                    destinationFile
                        .outputStream()
                        .use { output ->

                            input.copyTo(
                                output
                            )
                        }
                }

                destinationFile.absolutePath

            } catch (
                _: Exception
            ) {

                null
            }
        }
    }

    private fun getFileExtension(
        uri: Uri
    ): String {

        val mimeType =
            requireContext()
                .contentResolver
                .getType(uri)

        return when (mimeType) {

            "image/png" ->
                ".png"

            "image/webp" ->
                ".webp"

            "image/gif" ->
                ".gif"

            else ->
                ".jpg"
        }
    }

    private fun showSelectedImage(
        uri: Uri
    ) {

        binding.ivPlaylistCover.visibility =
            View.VISIBLE

        binding.ivAddPhoto.visibility =
            View.GONE

        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(
                binding.ivPlaylistCover
            )
    }

    private fun showCurrentCover(
        coverPath: String?
    ) {

        if (
            coverPath.isNullOrBlank()
        ) {

            binding.ivPlaylistCover.visibility =
                View.GONE

            binding.ivAddPhoto.visibility =
                View.VISIBLE

            return
        }

        val file =
            File(coverPath)

        if (!file.exists()) {

            binding.ivPlaylistCover.visibility =
                View.GONE

            binding.ivAddPhoto.visibility =
                View.VISIBLE

            return
        }

        binding.ivPlaylistCover.visibility =
            View.VISIBLE

        binding.ivAddPhoto.visibility =
            View.GONE

        Glide.with(this)
            .load(file)
            .centerCrop()
            .into(
                binding.ivPlaylistCover
            )
    }

    private fun handleBack() {

        if (hasUnsavedData()) {

            showExitDialog()

        } else {

            findNavController()
                .navigateUp()
        }
    }

    private fun hasUnsavedData(): Boolean {

        return binding.etPlaylistName
            .text
            .toString()
            .trim()
            .isNotBlank()

                ||

                binding.etPlaylistDescription
                    .text
                    .toString()
                    .trim()
                    .isNotBlank()

                ||

                selectedImageUri != null
    }

    private fun showExitDialog() {

        AlertDialog.Builder(
            requireContext(),
            R.style.PlaylistMakerDialogTheme
        )
            .setTitle(
                getString(
                    R.string.finish_playlist_creation_title
                )
            )
            .setMessage(
                getString(
                    R.string.finish_playlist_creation_message
                )
            )
            .setNegativeButton(
                getString(
                    R.string.cancel
                ),
                null
            )
            .setPositiveButton(
                getString(
                    R.string.finish
                )
            ) { _, _ ->

                findNavController()
                    .navigateUp()
            }
            .show()
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}