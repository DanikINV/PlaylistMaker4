package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class SearchActivity : AppCompatActivity() {

    // Сохраняем текст поиска при повороте экрана
    private var searchText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_search)

        // Экран поиска светлый/тёмный → иконки статус-бара подстраиваются
        val isNightMode = resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isNightMode
        }

        val rootView = findViewById<View>(R.id.root_layout)
        val toolbarLayout = findViewById<View>(R.id.toolbar_layout)

        // Отступы под статус-бар
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            val spacing = (16 * density).toInt()
            toolbarLayout.setPadding(spacing, systemBars.top + spacing, spacing, spacing)
            insets
        }

        // Кнопка «Назад»
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        val etSearch = findViewById<EditText>(R.id.et_search)
        val btnClear = findViewById<ImageView>(R.id.btn_clear_search)

        // Кнопка X: очищает поле, прячет клавиатуру, скрывает себя
        btnClear.setOnClickListener {
            etSearch.text.clear()
            hideKeyboard(etSearch)
        }

        // TextWatcher: сохраняет актуальный текст в переменную,
        // показывает/скрывает кнопку X в зависимости от наличия текста
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Заглушка для будущих задач
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Показываем X только когда есть текст
                btnClear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                searchText = s?.toString() ?: ""
                // Заглушка: здесь будет запрос к API
            }

            override fun afterTextChanged(s: Editable?) {
                // Заглушка для будущих задач
            }
        })
    }

    // Сохраняем текст поиска перед уничтожением Activity (поворот экрана и т.д.)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }

    // Восстанавливаем текст поиска после воссоздания Activity
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
