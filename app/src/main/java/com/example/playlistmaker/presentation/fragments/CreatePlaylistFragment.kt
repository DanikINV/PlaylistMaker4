package com.example.playlistmaker.presentation.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
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
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.example.playlistmaker.domain.interactors.PlaylistInteractor
import com.example.playlistmaker.domain.model.Playlist
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class CreatePlaylistFragment :
    Fragment(R.layout.fragment_create_playlist) {


    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!


    private val playlistInteractor: PlaylistInteractor by inject()


    private var selectedImageUri: Uri? = null



    private val pickImageLauncher =
        registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->


            if (uri != null) {


                try {

                    requireContext()
                        .contentResolver
                        .takePersistableUriPermission(
                            uri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )

                } catch (_: SecurityException) {
                }


                selectedImageUri = uri


                showSelectedImage(uri)

                updateCreateButton()
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

        setupImagePicker()

        setupNameField()

        setupCreateButton()


        restoreSelectedImage()

        updateCreateButton()
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

            handleBack()
        }
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

            updateCreateButton()
        }
    }




    private fun setupCreateButton() {


        binding.btnCreatePlaylist.setOnClickListener {

            createPlaylist()
        }
    }




    private fun updateCreateButton() {


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



        val playlist =
            Playlist(

                name = playlistName,

                description = playlistDescription,

                coverPath = selectedImageUri?.toString(),

                trackIds = emptyList(),

                tracksCount = 0
            )



        viewLifecycleOwner.lifecycleScope.launch {


            playlistInteractor.createPlaylist(
                playlist
            )



            parentFragmentManager.setFragmentResult(

                "playlist_created",

                bundleOf(

                    "playlist_name" to playlistName
                )
            )



            parentFragmentManager
                .popBackStack()
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





    private fun restoreSelectedImage() {


        selectedImageUri?.let { uri ->

            showSelectedImage(uri)
        }
    }





    private fun handleBack() {


        if (hasUnsavedData()) {


            showExitDialog()

        } else {


            parentFragmentManager
                .popBackStack()
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
            requireContext()
        )

            .setTitle(
                "Завершить создание плейлиста?"
            )

            .setMessage(
                "Все несохраненные данные будут потеряны"
            )

            .setNegativeButton(
                "Отмена",
                null
            )

            .setPositiveButton(
                "Завершить"
            ) { _, _ ->


                parentFragmentManager
                    .popBackStack()
            }

            .show()
    }





    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}