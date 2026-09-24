package com.example.playlistmaker.presentation.activities

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.Creator
import com.example.playlistmaker.presentation.model.SettingsScreenState
import com.example.playlistmaker.presentation.model.SettingsViewModel
import com.example.playlistmaker.presentation.model.SettingsViewModelFactory

class SettingsActivity : AppCompatActivity() {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var switchDarkTheme: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContentView(R.layout.activity_settings)

        val rootView = findViewById<View>(
            R.id.root_layout
        )

        val toolbarLayout = findViewById<LinearLayout>(
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

        val factory = SettingsViewModelFactory(
            Creator.provideSettingsInteractor(
                applicationContext
            )
        )

        viewModel = ViewModelProvider(
            this,
            factory
        )[SettingsViewModel::class.java]

        initViews()
        initListeners()
        observeViewModel()
    }

    private fun initViews() {
        switchDarkTheme = findViewById(
            R.id.switch_dark_theme
        )
    }

    private fun initListeners() {

        switchDarkTheme.setOnCheckedChangeListener {
                _, isChecked ->

            viewModel.onThemeChanged(isChecked)
        }

        findViewById<ImageView>(
            R.id.btn_back
        ).setOnClickListener {

            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<TextView>(
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

        findViewById<TextView>(
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

        findViewById<TextView>(
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

        viewModel.state.observe(this) { state ->

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
}