package com.mihnati.app.translation

import android.content.Context
import androidx.compose.runtime.*
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.mihnati.app.ui.theme.LanguageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

/**
 * On-Device Automatic Translation Service powered by Google ML Kit.
 * Handles on-the-fly French to Arabic translation at runtime without
 * requiring any hardcoded Arabic text in the codebase.
 */
object AutoTranslationService {
    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.FRENCH)
        .setTargetLanguage(TranslateLanguage.ARABIC)
        .build()

    private val translator: Translator = Translation.getClient(options)
    
    // In-memory cache for instant zero-latency lookups
    private val cache = ConcurrentHashMap<String, String>()
    
    private val _isModelReady = MutableStateFlow(false)
    val isModelReady: StateFlow<Boolean> = _isModelReady

    private val _translationVersion = MutableStateFlow(0)
    val translationVersion: StateFlow<Int> = _translationVersion

    private var cacheFile: File? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initialize(context: Context) {
        try {
            val dir = File(context.filesDir, "mlkit_translations")
            if (!dir.exists()) dir.mkdirs()
            cacheFile = File(dir, "fr_ar_cache.tsv")

            // Load persisted cache entries from disk
            loadDiskCache()

            // Trigger Google ML Kit on-device model download
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    _isModelReady.value = true
                }
                .addOnFailureListener {
                    // Retry with standard conditions if needed
                    _isModelReady.value = false
                }
        } catch (_: Exception) {}
    }

    private fun loadDiskCache() {
        val file = cacheFile ?: return
        if (!file.exists()) return
        try {
            file.forEachLine { line ->
                val parts = line.split("\t")
                if (parts.size >= 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    if (key.isNotEmpty() && value.isNotEmpty()) {
                        cache[key] = value
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun saveToDisk(source: String, translated: String) {
        val file = cacheFile ?: return
        scope.launch {
            try {
                file.appendText("$source\t$translated\n")
            } catch (_: Exception) {}
        }
    }

    /**
     * Translates a single text from French to Arabic on-device using ML Kit.
     */
    suspend fun translate(text: String): String {
        if (text.isBlank()) return text
        cache[text]?.let { return it }

        return suspendCancellableCoroutine { continuation ->
            translator.translate(text)
                .addOnSuccessListener { result ->
                    if (result.isNotBlank()) {
                        cache[text] = result
                        saveToDisk(text, result)
                        _translationVersion.value += 1
                        continuation.resume(result)
                    } else {
                        continuation.resume(text)
                    }
                }
                .addOnFailureListener {
                    // Fallback to source text if model is still downloading or offline
                    continuation.resume(text)
                }
        }
    }

    /**
     * Batch translation for pre-warming UI elements.
     */
    fun preWarm(texts: List<String>) {
        scope.launch {
            texts.forEach { text ->
                if (!cache.containsKey(text) && text.isNotBlank()) {
                    translate(text)
                }
            }
        }
    }

    fun getCached(text: String): String? {
        return cache[text]
    }
}

/**
 * Composable extension to automatically translate French text to Arabic
 * when Arabic mode is active using Google ML Kit.
 */
@Composable
fun autoTranslate(text: String): String {
    val isArabic = LanguageManager.isArabic
    if (!isArabic || text.isBlank()) return text

    // Quick lookup in cache
    val cached = AutoTranslationService.getCached(text)
    if (cached != null) return cached

    // Reactive state updated once ML Kit completes translation
    var translated by remember(text, isArabic) { mutableStateOf(text) }

    LaunchedEffect(text, isArabic) {
        val result = AutoTranslationService.translate(text)
        translated = result
    }

    return translated
}
