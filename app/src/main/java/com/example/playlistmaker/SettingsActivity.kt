package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_settings)

        val rootView = findViewById<android.view.View>(R.id.root_layout)

        val toolbarLayout = findViewById<LinearLayout>(R.id.toolbar_layout)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            val spacing = (16 * density).toInt()
            toolbarLayout.setPadding(spacing, systemBars.top, spacing, spacing)
            insets
        }

        // Back button
        val backButton = findViewById<ImageView>(R.id.btn_back)
        backButton.setOnClickListener { finish() }

        // Share app
        val btnShare = findViewById<TextView>(R.id.btn_share)
        btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)))
        }

        // Support
        val btnSupport = findViewById<TextView>(R.id.btn_support)
        btnSupport.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${getString(R.string.support_email)}")
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_body))
            }
            startActivity(emailIntent)
        }

        // User agreement
        val btnUserAgreement = findViewById<TextView>(R.id.btn_user_agreement)
        btnUserAgreement.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.user_agreement_url))
            )
            startActivity(browserIntent)
        }
    }
}
