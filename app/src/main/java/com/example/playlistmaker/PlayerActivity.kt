package com.example.playlistmaker

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_player)

        val isNightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isNightMode
        }

        val rootView = findViewById<androidx.core.widget.NestedScrollView>(R.id.root_layout)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val trackJson = intent.getStringExtra(EXTRA_TRACK)
        val track = trackJson?.let { Gson().fromJson(it, Track::class.java) } ?: run {
            finish()
            return
        }

        bindTrack(track)
    }

    private fun bindTrack(track: Track) {
        findViewById<TextView>(R.id.tv_track_name).text = track.trackName
        findViewById<TextView>(R.id.tv_artist_name).text = track.artistName

        val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        findViewById<TextView>(R.id.tv_duration_value).text = timeFormat.format(track.trackTime)

        bindOptionalRow(R.id.row_album, R.id.tv_album_value, track.collectionName)
        bindOptionalRow(R.id.row_year, R.id.tv_year_value, extractYear(track.releaseDate))
        bindOptionalRow(R.id.row_genre, R.id.tv_genre_value, track.primaryGenreName)
        bindOptionalRow(R.id.row_country, R.id.tv_country_value, track.country)

        val cornerRadiusPx = resources.getDimensionPixelSize(R.dimen.player_artwork_corner_radius)
        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.vector)
            .error(R.drawable.vector)
            .transform(MultiTransformation(CenterCrop(), RoundedCorners(cornerRadiusPx)))
            .into(findViewById(R.id.iv_artwork))
    }

    private fun bindOptionalRow(rowId: Int, valueViewId: Int, value: String?) {
        val row = findViewById<android.view.View>(rowId)
        if (value.isNullOrBlank()) {
            row.visibility = android.view.View.GONE
        } else {
            row.visibility = android.view.View.VISIBLE
            findViewById<TextView>(valueViewId).text = value
        }
    }

    private fun extractYear(releaseDate: String?): String? {
        if (releaseDate.isNullOrBlank() || releaseDate.length < 4) return null
        return releaseDate.substring(0, 4)
    }

    companion object {
        const val EXTRA_TRACK = "EXTRA_TRACK"
    }
}