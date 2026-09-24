package com.example.playlistmaker.presentation.activities

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.interactors.SettingsInteractor
import com.example.playlistmaker.presentation.Creator

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsInteractor: SettingsInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsInteractor =
            Creator.provideSettingsInteractor(applicationContext)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_settings)

        val isNightMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = !isNightMode
        }

        val rootView = findViewById<android.view.View>(
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

        val backButton = findViewById<ImageView>(
            R.id.btn_back
        )

        backButton.setOnClickListener {
            finish()
        }

        val switchDarkTheme = findViewById<SwitchCompat>(
            R.id.switch_dark_theme
        )

        switchDarkTheme.isChecked =
            settingsInteractor.isDarkTheme()

        switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->

            settingsInteractor.saveDarkTheme(
                isChecked
            )

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            )
        }

        val btnShare = findViewById<TextView>(
            R.id.btn_share
        )

        btnShare.setOnClickListener {

            val shareIntent = Intent(
                Intent.ACTION_SEND
            ).apply {

                type = "text/plain"

                putExtra(
                    Intent.EXTRA_TEXT,
                    getString(R.string.share_message)
                )
            }

            startActivity(
                Intent.createChooser(
                    shareIntent,
                    getString(R.string.share_app)
                )
            )
        }

        val btnSupport = findViewById<TextView>(
            R.id.btn_support
        )

        btnSupport.setOnClickListener {

            val emailIntent = Intent(
                Intent.ACTION_SENDTO
            ).apply {

                data = Uri.parse("mailto:")

                putExtra(
                    Intent.EXTRA_EMAIL,
                    arrayOf(
                        getString(
                            R.string.support_email
                        )
                    )
                )

                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(
                        R.string.support_subject
                    )
                )

                putExtra(
                    Intent.EXTRA_TEXT,
                    getString(
                        R.string.support_body
                    )
                )
            }

            startActivity(emailIntent)
        }

        val btnUserAgreement = findViewById<TextView>(
            R.id.btn_user_agreement
        )

        btnUserAgreement.setOnClickListener {

            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        getString(
                            R.string.user_agreement_url
                        )
                    )
                )
            )
        }
    }
}