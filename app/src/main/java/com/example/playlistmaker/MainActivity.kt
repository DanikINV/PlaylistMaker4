package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("playlist_maker_prefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_theme", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }

        val rootView = findViewById<View>(R.id.root_layout)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = view.resources.displayMetrics.density
            val horizontalPadding = (16 * density).toInt()
            view.setPaddingRelative(
                horizontalPadding,
                systemBars.top,
                horizontalPadding,
                horizontalPadding + systemBars.bottom
            )
            insets
        }

        val searchButton = findViewById<MaterialButton>(R.id.btn_search)
        val mediaLibraryButton = findViewById<MaterialButton>(R.id.btn_media)
        val settingsButton = findViewById<MaterialButton>(R.id.btn_settings)

        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
        mediaLibraryButton.setOnClickListener {
            startActivity(Intent(this, MediaLibraryActivity::class.java))
        }
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}
