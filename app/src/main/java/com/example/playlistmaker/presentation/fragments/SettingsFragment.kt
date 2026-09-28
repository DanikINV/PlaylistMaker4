package com.example.playlistmaker.presentation.fragments

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.model.SettingsScreenState
import com.example.playlistmaker.presentation.model.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.activity_settings) {

    private val viewModel: SettingsViewModel by viewModel()

    private lateinit var switchDarkTheme: SwitchCompat

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupWindowInsets(view)
        initViews(view)
        initListeners(view)
        observeViewModel()
    }

    private fun setupWindowInsets(view: View) {

        val rootView = view.findViewById<View>(
            R.id.root_layout
        )

        val toolbarLayout = view.findViewById<LinearLayout>(
            R.id.toolbar_layout
        )

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView
        ) { _, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            val density = resources.displayMetrics.density
            val spacing = (16 * density).toInt()

            toolbarLayout.setPadding(
                spacing,
                systemBars.top + spacing,
                spacing,
                spacing
            )

            insets
        }
    }

    private fun initViews(view: View) {

        switchDarkTheme = view.findViewById(
            R.id.switch_dark_theme
        )
    }

    private fun initListeners(view: View) {

        switchDarkTheme.setOnCheckedChangeListener {
                _, isChecked ->

            viewModel.onThemeChanged(isChecked)
        }

        view.findViewById<TextView>(
            R.id.btn_share
        ).setOnClickListener {

            val intent = Intent(
                Intent.ACTION_SEND
            ).apply {
                type = "text/plain"

                putExtra(
                    Intent.EXTRA_TEXT,
                    getString(R.string.share_app)
                )
            }

            startActivity(
                Intent.createChooser(
                    intent,
                    null
                )
            )
        }

        view.findViewById<TextView>(
            R.id.btn_support
        ).setOnClickListener {

            val intent = Intent(
                Intent.ACTION_SENDTO
            ).apply {

                data = Uri.parse(
                    "mailto:${getString(R.string.support_email)}"
                )

                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.support_subject)
                )

                putExtra(
                    Intent.EXTRA_TEXT,
                    getString(R.string.support)
                )
            }

            startActivity(intent)
        }

        view.findViewById<TextView>(
            R.id.btn_user_agreement
        ).setOnClickListener {

            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    getString(R.string.user_agreement_url)
                )
            )

            startActivity(intent)
        }
    }

    private fun observeViewModel() {

        viewModel.state.observe(viewLifecycleOwner) { state ->

            renderState(state)
        }
    }

    private fun renderState(
        state: SettingsScreenState
    ) {

        switchDarkTheme.setOnCheckedChangeListener(null)

        switchDarkTheme.isChecked =
            state.isDarkTheme

        switchDarkTheme.setOnCheckedChangeListener {
                _, isChecked ->

            viewModel.onThemeChanged(isChecked)
        }

        val currentIsNightMode =
            resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES

        if (currentIsNightMode != state.isDarkTheme) {

            AppCompatDelegate.setDefaultNightMode(
                if (state.isDarkTheme) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            )
        }
    }

    companion object {

        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}