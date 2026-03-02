package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        val rootView = findViewById<View>(R.id.root_layout)

        // Добавляем только top/bottom отступы для системных баров,
        // не трогая горизонтальные padding из XML
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = view.resources.displayMetrics.density
            val horizontalPadding = (16 * density).toInt()
            // setPaddingRelative сохраняет RTL-направление и не ломает горизонтальные отступы
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
            startActivity(Intent(this@MainActivity, SearchActivity::class.java))
        }

        mediaLibraryButton.setOnClickListener {
            startActivity(Intent(this, MediaLibraryActivity::class.java))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
}