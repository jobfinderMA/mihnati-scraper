package com.mihnati.app.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val flagEmoji: String,
    val isRtl: Boolean
) {
    FRENCH("fr", "Français", "🇫🇷", false),
    ARABIC("ar", "العربية", "🇲🇦", true)
}

object LanguageManager {
    private const val PREFS_NAME = "mihnati_language_prefs"
    private const val KEY_SELECTED_LANGUAGE = "selected_app_language"

    private val _currentLanguage = MutableStateFlow(AppLanguage.FRENCH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_SELECTED_LANGUAGE, "fr") ?: "fr"
        _currentLanguage.value = if (code == "ar") AppLanguage.ARABIC else AppLanguage.FRENCH
    }

    fun setLanguage(context: Context, lang: AppLanguage) {
        _currentLanguage.value = lang
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_LANGUAGE, lang.code)
            .apply()
    }

    fun toggleLanguage(context: Context) {
        val next = if (_currentLanguage.value == AppLanguage.FRENCH) AppLanguage.ARABIC else AppLanguage.FRENCH
        setLanguage(context, next)
    }

    val isArabic: Boolean
        get() = _currentLanguage.value == AppLanguage.ARABIC
}
