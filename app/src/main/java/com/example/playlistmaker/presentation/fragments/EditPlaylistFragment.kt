package com.example.playlistmaker.presentation.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.example.playlistmaker.domain.model.Playlist
import com.example.playlistmaker.presentation.model.EditPlaylistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class EditPlaylistFragment :
    Fragment(R.layout.fragment_create_playlist) {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditPlaylistViewModel by viewModel()

    private var selectedImageUri: Uri? = null

    private var currentPlaylist: Playlist? = null

    private val playlistId: Long
        get() = requireArguments()
            .getLong("playlistId")

    private val pickImageLauncher =
        registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->

            if (uri != null) {
                selectedImageUri = uri

                showSelectedImage(uri)
                updateSaveButton()
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
            FragmentCreatePlaylistBinding.bind(view)

        setupWindowInsets()
        setupBackButton()
        setupSystemBack()
        setupImagePicker()
        setupNameField()
        setupSaveButton()

        loadPlaylist()
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
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupSystemBack() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {

                    override fun handleOnBackPressed() {
                        parentFragmentManager
                            .popBackStack()
                    }
                }
            )
    }

    private fun setupImagePicker() {
        binding.playlistCoverContainer.setOnClickListener {

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
        binding.etPlaylistName.doAfterTextChanged {
            updateSaveButton()
        }
    }

    private fun setupSaveButton() {

        binding.btnCreatePlaylist.text =
            "Сохранить"

        binding.btnCreatePlaylist.setOnClickListener {
            savePlaylist()
        }

        binding.btnCreatePlaylist.setTextColor(
            resources.getColor(
                R.color.white,
                requireContext().theme
            )
        )
    }

    private fun updateSaveButton() {

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

        viewModel.getPlaylist(
            playlistId
        ) { playlist ->

            if (!isAdded) {
                return@getPlaylist
            }

            if (playlist == null) {
                parentFragmentManager
                    .popBackStack()
                return@getPlaylist
            }

            currentPlaylist = playlist

            binding.tvTitle.text =
                "Редактировать"

            binding.etPlaylistName.setText(
                playlist.name
            )

            binding.etPlaylistDescription.setText(
                playlist.description.orEmpty()
            )

            showCurrentCover(
                playlist.coverPath
            )

            updateSaveButton()
        }
    }

    private fun showCurrentCover(
        coverPath: String?
    ) {

        if (coverPath.isNullOrBlank()) {
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
            .into(binding.ivPlaylistCover)
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
            .into(binding.ivPlaylistCover)
    }

    private fun savePlaylist() {

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

        viewLifecycleOwner.lifecycleScope.launch {

            val coverPath =
                if (selectedImageUri != null) {
                    copyImageToInternalStorage(
                        selectedImageUri!!
                    ) ?: playlist.coverPath
                } else {
                    playlist.coverPath
                }

            val updatedPlaylist =
                playlist.copy(
                    name = playlistName,
                    description = playlistDescription,
                    coverPath = coverPath
                )

            viewModel.updatePlaylist(
                updatedPlaylist
            ) {
                parentFragmentManager
                    .popBackStack()
            }
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

                if (!coversDirectory.exists()) {
                    coversDirectory.mkdirs()
                }

                val extension =
                    getFileExtension(uri)

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
                            input.copyTo(output)
                        }
                }

                destinationFile.absolutePath

            } catch (_: Exception) {
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
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/gif" -> ".gif"
            else -> ".jpg"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}