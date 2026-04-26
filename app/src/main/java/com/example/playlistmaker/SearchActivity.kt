package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    private var searchText: String = ""
    private lateinit var iTunesService: ITunesApi
    private val tracks = ArrayList<Track>()
    private lateinit var adapter: TrackAdapter
    private lateinit var placeholderMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_search)

        val isNightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isNightMode
        }

        val rootView = findViewById<View>(R.id.root_layout)
        val toolbarLayout = findViewById<View>(R.id.toolbar_layout)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            val spacing = (16 * density).toInt()
            toolbarLayout.setPadding(spacing, systemBars.top + spacing, spacing, spacing)
            insets
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        iTunesService = retrofit.create(ITunesApi::class.java)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        val etSearch = findViewById<EditText>(R.id.et_search)
        val btnClear = findViewById<ImageView>(R.id.btn_clear_search)
        placeholderMessage = findViewById(R.id.placeholder_message)

        btnClear.setOnClickListener {
            etSearch.text.clear()
            hideKeyboard(etSearch)
            tracks.clear()
            adapter.notifyDataSetChanged()
            placeholderMessage.visibility = View.GONE
        }

        etSearch.doOnTextChanged { text, _, _, _ ->
            btnClear.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
            searchText = text?.toString() ?: ""
        }

        val rvTracks = findViewById<RecyclerView>(R.id.rv_tracks)
        rvTracks.layoutManager = LinearLayoutManager(this)
        adapter = TrackAdapter(tracks)
        rvTracks.adapter = adapter

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (etSearch.text.isNotEmpty()) {
                    search(etSearch.text.toString())
                }
                true
            }
            false
        }
    }

    private fun search(query: String) {
        iTunesService.search(query).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
                if (response.code() == 200) {
                    tracks.clear()
                    if (response.body()?.results?.isNotEmpty() == true) {
                        tracks.addAll(response.body()?.results!!)
                        adapter.notifyDataSetChanged()
                    }
                    if (tracks.isEmpty()) {
                        showMessage(getString(R.string.nothing_found))
                    } else {
                        placeholderMessage.visibility = View.GONE
                    }
                } else {
                    showMessage(getString(R.string.something_went_wrong))
                }
            }

            override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                showMessage(getString(R.string.something_went_wrong))
            }
        })
    }

    private fun showMessage(text: String) {
        placeholderMessage.visibility = View.VISIBLE
        tracks.clear()
        adapter.notifyDataSetChanged()
        placeholderMessage.text = text
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        findViewById<EditText>(R.id.et_search).setText(searchText)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT"
    }
}