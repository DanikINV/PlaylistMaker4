package com.example.playlistmaker

import android.content.Intent
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

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_settings)

        // Экран настроек светлый/тёмный → иконки статус-бара подстраиваются под тему
        val isNightMode = resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES

        WindowInsetsControllerCompat(window, window.decorView).apply {
            // Светлая тема → тёмные иконки; тёмная → светлые
            isAppearanceLightStatusBars = !isNightMode
        }

        val rootView = findViewById<android.view.View>(R.id.root_layout)
        val toolbarLayout = findViewById<LinearLayout>(R.id.toolbar_layout)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            val spacing = (16 * density).toInt()
            toolbarLayout.setPadding(spacing, systemBars.top + spacing, spacing, spacing)
            insets
        }

        val backButton = findViewById<ImageView>(R.id.btn_back)
        backButton.setOnClickListener { finish() }

        val switchDarkTheme = findViewById<SwitchCompat>(R.id.switch_dark_theme)
        val prefs = getSharedPreferences("playlist_maker_prefs", MODE_PRIVATE)
        switchDarkTheme.isChecked = prefs.getBoolean("dark_theme", false)

        switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_theme", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        val btnShare = findViewById<TextView>(R.id.btn_share)
        btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
        }

        val btnSupport = findViewById<TextView>(R.id.btn_support)
        btnSupport.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_body))
            }
            startActivity(emailIntent)
        }

        val btnUserAgreement = findViewById<TextView>(R.id.btn_user_agreement)
        btnUserAgreement.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.user_agreement_url))))
        }
    }
}
