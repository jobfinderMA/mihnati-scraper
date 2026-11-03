package com.mihnati.app.translation

import android.content.Context
import androidx.compose.runtime.*
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.mihnati.app.ui.theme.AppLanguage
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
 * On-Device Automatic Bi-Directional Translation Service powered by Google ML Kit.
 * Handles instant zero-latency French <-> Arabic translation at runtime without
 * requiring any hardcoded Arabic text in the codebase.
 */
object AutoTranslationService {
    private val frToArOptions = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.FRENCH)
        .setTargetLanguage(TranslateLanguage.ARABIC)
        .build()

    private val arToFrOptions = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ARABIC)
        .setTargetLanguage(TranslateLanguage.FRENCH)
        .build()

    private val frToArTranslator: Translator = Translation.getClient(frToArOptions)
    private val arToFrTranslator: Translator = Translation.getClient(arToFrOptions)

    // In-memory dictionaries for instant 0ms lookups
    private val frToArDict = ConcurrentHashMap<String, String>()
    private val arToFrDict = ConcurrentHashMap<String, String>()

    private val _isModelReady = MutableStateFlow(false)
    val isModelReady: StateFlow<Boolean> = _isModelReady

    private val _translationVersion = MutableStateFlow(0)
    val translationVersion: StateFlow<Int> = _translationVersion

    private var cacheFile: File? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun initialize(context: Context) {
        try {
            // 1. Load pre-bundled official Moroccan dictionary from assets
            loadAssetDictionary(context)

            // 2. Load disk cache
            val dir = File(context.filesDir, "mlkit_translations")
            if (!dir.exists()) dir.mkdirs()
            cacheFile = File(dir, "bilingual_cache.tsv")
            loadDiskCache()

            // 3. Trigger Google ML Kit on-device model download in background
            val conditions = DownloadConditions.Builder().build()
            frToArTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    _isModelReady.value = true
                }
            arToFrTranslator.downloadModelIfNeeded(conditions)
        } catch (_: Exception) {}
    }

    private fun loadAssetDictionary(context: Context) {
        try {
            context.assets.open("mihnati_dictionary.tsv").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split("\t")
                    if (parts.size >= 2) {
                        val fr = parts[0].trim()
                        val ar = parts[1].trim()
                        if (fr.isNotEmpty() && ar.isNotEmpty()) {
                            frToArDict[fr] = ar
                            arToFrDict[ar] = fr
                            frToArDict[fr.lowercase()] = ar
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun loadDiskCache() {
        val file = cacheFile ?: return
        if (!file.exists()) return
        try {
            file.forEachLine { line ->
                val parts = line.split("\t")
                if (parts.size >= 3) {
                    val lang = parts[0].trim()
                    val src = parts[1].trim()
                    val dst = parts[2].trim()
                    if (src.isNotEmpty() && dst.isNotEmpty()) {
                        if (lang == "fr_ar") {
                            frToArDict[src] = dst
                        } else {
                            arToFrDict[src] = dst
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun saveToDisk(direction: String, source: String, translated: String) {
        val file = cacheFile ?: return
        scope.launch {
            try {
                file.appendText("$direction\t$source\t$translated\n")
            } catch (_: Exception) {}
        }
    }

    fun isTextArabic(text: String): Boolean {
        for (char in text) {
            val block = Character.UnicodeBlock.of(char)
            if (block == Character.UnicodeBlock.ARABIC ||
                block == Character.UnicodeBlock.ARABIC_SUPPLEMENT ||
                block == Character.UnicodeBlock.ARABIC_EXTENDED_A ||
                block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
                block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B) {
                return true
            }
        }
        return false
    }

    fun getArabic(frenchText: String): String? {
        return frToArDict[frenchText] ?: frToArDict[frenchText.trim()] ?: frToArDict[frenchText.lowercase().trim()]
    }

    fun getFrench(arabicText: String): String? {
        return arToFrDict[arabicText] ?: arToFrDict[arabicText.trim()]
    }

    suspend fun translateFrToAr(text: String): String {
        if (text.isBlank()) return text
        getArabic(text)?.let { return it }

        return suspendCancellableCoroutine { continuation ->
            frToArTranslator.translate(text)
                .addOnSuccessListener { result ->
                    if (result.isNotBlank()) {
                        frToArDict[text] = result
                        arToFrDict[result] = text
                        saveToDisk("fr_ar", text, result)
                        _translationVersion.value += 1
                        continuation.resume(result)
                    } else {
                        continuation.resume(text)
                    }
                }
                .addOnFailureListener {
                    continuation.resume(text)
                }
        }
    }

    suspend fun translateArToFr(text: String): String {
        if (text.isBlank()) return text
        getFrench(text)?.let { return it }

        return suspendCancellableCoroutine { continuation ->
            arToFrTranslator.translate(text)
                .addOnSuccessListener { result ->
                    if (result.isNotBlank()) {
                        arToFrDict[text] = result
                        frToArDict[result] = text
                        saveToDisk("ar_fr", text, result)
                        _translationVersion.value += 1
                        continuation.resume(result)
                    } else {
                        continuation.resume(text)
                    }
                }
                .addOnFailureListener {
                    continuation.resume(text)
                }
        }
    }
}

/**
 * Reactive Composable extension to automatically translate UI strings
 * to Arabic when Arabic mode is selected, or to French when French mode is selected.
 * Reacts instantaneously to language swap.
 */
@Composable
fun autoTranslate(text: String): String {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val isArabic = currentLang == AppLanguage.ARABIC

    if (text.isBlank()) return text

    if (isArabic) {
        if (AutoTranslationService.isTextArabic(text)) return text

        val cached = AutoTranslationService.getArabic(text)
        if (cached != null) return cached

        var translated by remember(text, currentLang) { mutableStateOf(text) }
        LaunchedEffect(text, currentLang) {
            val result = AutoTranslationService.translateFrToAr(text)
            translated = result
        }
        return translated
    } else {
        if (!AutoTranslationService.isTextArabic(text)) return text

        val cached = AutoTranslationService.getFrench(text)
        if (cached != null) return cached

        var translated by remember(text, currentLang) { mutableStateOf(text) }
        LaunchedEffect(text, currentLang) {
            val result = AutoTranslationService.translateArToFr(text)
            translated = result
        }
        return translated
    }
}
