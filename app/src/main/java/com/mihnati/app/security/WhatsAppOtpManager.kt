package com.mihnati.app.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.mihnati.app.notifications.MihnatiNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Enterprise WhatsApp OTP & Concours Alert Dispatcher (Mihnati مهنتي).
 *
 * Capabilities:
 * - Moroccan mobile number parsing & normalization (+212 6/7...)
 * - 6-Digit PIN generation with 10-minute expiry & brute-force throttling
 * - Automated WhatsApp message delivery via REST API (Green-API / UltraMsg / Custom Webhook)
 * - Android heads-up notification for zero-friction testing
 * - 1-Tap WhatsApp Click-to-Chat deep links (wa.me)
 * - Automated Écrit, Oral, and Success list convocation alerts
 */
object WhatsAppOtpManager {

    private const val TAG = "WhatsAppOtpManager"
    private const val OTP_EXPIRY_DURATION_MS = 10 * 60 * 1000L // 10 minutes

    @Volatile
    private var lastGeneratedOtp: String? = null

    @Volatile
    private var targetPhoneNumber: String? = null

    @Volatile
    private var otpExpiryEpochMs: Long = 0L

    @Volatile
    private var attemptsCount: Int = 0

    @Volatile
    private var lastSentEpochMs: Long = 0L

    /**
     * Optional custom REST Gateway endpoint (e.g., Green-API, UltraMsg, or your server webhook)
     */
    private const val PREFS_NAME = "mihnati_whatsapp_gateway_config"
    private const val KEY_GREEN_API_ID = "green_api_instance_id"
    private const val KEY_GREEN_API_TOKEN = "green_api_token"
    private const val KEY_CUSTOM_GATEWAY_URL = "custom_gateway_url"

    var configuredInstanceId: String? = null
        private set
    var configuredToken: String? = null
        private set

    fun loadGatewayConfig(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_GREEN_API_ID, null)
        val token = prefs.getString(KEY_GREEN_API_TOKEN, null)
        val customUrl = prefs.getString(KEY_CUSTOM_GATEWAY_URL, null)

        configuredInstanceId = id
        configuredToken = token

        if (!id.isNullOrEmpty() && !token.isNullOrEmpty()) {
            externalWhatsAppGatewayUrl = "https://api.green-api.com/waInstance$id/sendMessage/$token"
            externalWhatsAppApiKey = token
            Log.i(TAG, "Green-API Gateway initialized with instance: $id")
        } else if (!customUrl.isNullOrEmpty()) {
            externalWhatsAppGatewayUrl = customUrl
            Log.i(TAG, "Custom Gateway initialized: $customUrl")
        }
    }

    fun saveGreenApiConfig(context: Context, instanceId: String, apiToken: String) {
        val cleanId = instanceId.trim()
        val cleanToken = apiToken.trim()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_GREEN_API_ID, cleanId)
            .putString(KEY_GREEN_API_TOKEN, cleanToken)
            .apply()

        configuredInstanceId = cleanId
        configuredToken = cleanToken
        externalWhatsAppGatewayUrl = "https://api.green-api.com/waInstance$cleanId/sendMessage/$cleanToken"
        externalWhatsAppApiKey = cleanToken
        Log.i(TAG, "Green-API Gateway updated: instance $cleanId")
    }

    fun isGatewayConfigured(): Boolean {
        return !externalWhatsAppGatewayUrl.isNullOrEmpty()
    }

    var externalWhatsAppGatewayUrl: String? = null
    var externalWhatsAppApiKey: String? = null

    /**
     * Normalizes any Moroccan phone input into standard international format +212XXXXXXXXX
     * Supported inputs:
     * - "0612345678" -> "+212612345678"
     * - "0712345678" -> "+212712345678"
     * - "612345678"  -> "+212612345678"
     * - "+212 6 12 34 56 78" -> "+212612345678"
     */
    fun normalizeMoroccanPhoneNumber(rawInput: String): String? {
        val digitsOnly = rawInput.replace(Regex("[^0-9+]"), "").trim()
        
        return when {
            digitsOnly.startsWith("+212") -> {
                val afterCode = digitsOnly.removePrefix("+212")
                if (afterCode.length == 9 && (afterCode.startsWith("6") || afterCode.startsWith("7") || afterCode.startsWith("5"))) {
                    "+212$afterCode"
                } else null
            }
            digitsOnly.startsWith("00212") -> {
                val afterCode = digitsOnly.removePrefix("00212")
                if (afterCode.length == 9 && (afterCode.startsWith("6") || afterCode.startsWith("7") || afterCode.startsWith("5"))) {
                    "+212$afterCode"
                } else null
            }
            digitsOnly.startsWith("212") -> {
                val afterCode = digitsOnly.removePrefix("212")
                if (afterCode.length == 9 && (afterCode.startsWith("6") || afterCode.startsWith("7") || afterCode.startsWith("5"))) {
                    "+212$afterCode"
                } else null
            }
            digitsOnly.startsWith("0") && digitsOnly.length == 10 -> {
                val sub = digitsOnly.substring(1)
                if (sub.startsWith("6") || sub.startsWith("7") || sub.startsWith("5")) {
                    "+212$sub"
                } else null
            }
            (digitsOnly.startsWith("6") || digitsOnly.startsWith("7") || digitsOnly.startsWith("5")) && digitsOnly.length == 9 -> {
                "+212$digitsOnly"
            }
            // Fallback for valid international numbers (+33, +1, etc.)
            digitsOnly.startsWith("+") && digitsOnly.length >= 10 -> digitsOnly
            else -> null
        }
    }

    /**
     * Generates a 6-digit OTP code and dispatches via WhatsApp + heads-up notification
     */
    suspend fun generateAndSendOtp(
        context: Context,
        rawPhone: String,
        candidateName: String = "Candidat Mihnati"
    ): Result<String> = withContext(Dispatchers.IO) {
        val normalized = normalizeMoroccanPhoneNumber(rawPhone)
            ?: return@withContext Result.failure(
                IllegalArgumentException("Numéro de téléphone marocain invalide. Exemple: 06 12 34 56 78")
            )

        // 6-digit PIN (e.g. 849201)
        val otpCode = String.format("%06d", (100000..999999).random())

        lastGeneratedOtp = otpCode
        targetPhoneNumber = normalized
        otpExpiryEpochMs = System.currentTimeMillis() + OTP_EXPIRY_DURATION_MS
        attemptsCount = 0
        lastSentEpochMs = System.currentTimeMillis()

        Log.i(TAG, "Generated WhatsApp OTP for $normalized: $otpCode (Valid 10 min)")

        // 1. Android Heads-up Notification (instant on-device delivery)
        try {
            MihnatiNotificationHelper.showWhatsAppAuthOtpNotification(context, normalized, otpCode)
        } catch (e: Exception) {
            Log.w(TAG, "Notification dispatch failed: ${e.message}")
        }

        // 2. Automated REST Gateway Delivery (if configured)
        val gatewayUrl = externalWhatsAppGatewayUrl
        if (!gatewayUrl.isNullOrEmpty()) {
            try {
                sendViaWhatsAppGateway(
                    gatewayUrl = gatewayUrl,
                    apiKey = externalWhatsAppApiKey,
                    phone = normalized,
                    message = "🇲🇦 *Mihnati (مهنتي)*\n\nBonjour $candidateName,\nVotre code de vérification WhatsApp est :\n\n🔐 *$otpCode*\n\nCe code expire dans 10 minutes. Ne le transmettez à personne."
                )
            } catch (e: Exception) {
                Log.w(TAG, "WhatsApp Gateway dispatch note: ${e.message}")
            }
        }

        Result.success(otpCode)
    }

    /**
     * Verifies the 6-digit code entered by the candidate
     */
    fun verifyOtp(rawPhone: String, enteredCode: String): Result<Boolean> {
        val normalized = normalizeMoroccanPhoneNumber(rawPhone)
            ?: return Result.failure(IllegalArgumentException("Format de téléphone invalide."))

        val cleanCode = enteredCode.trim()

        if (cleanCode.length != 6 || !cleanCode.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("Veuillez saisir un code à 6 chiffres."))
        }

        if (targetPhoneNumber != normalized || lastGeneratedOtp == null) {
            return Result.failure(IllegalStateException("Aucun code actif pour ce numéro. Veuillez en demander un nouveau."))
        }

        if (System.currentTimeMillis() > otpExpiryEpochMs) {
            lastGeneratedOtp = null
            return Result.failure(IllegalStateException("Ce code a expiré. Veuillez demander un nouveau code WhatsApp."))
        }

        if (attemptsCount >= 5) {
            lastGeneratedOtp = null
            return Result.failure(IllegalStateException("Trop de tentatives infructueuses. Veuillez générer un nouveau code."))
        }

        if (cleanCode != lastGeneratedOtp) {
            attemptsCount++
            val remainingAttempts = 5 - attemptsCount
            return Result.failure(IllegalArgumentException("Code WhatsApp incorrect. Il vous reste $remainingAttempts tentative(s)."))
        }

        // Code matches! Clear OTP state
        lastGeneratedOtp = null
        targetPhoneNumber = null
        attemptsCount = 0

        Log.i(TAG, "WhatsApp OTP successfully verified for $normalized")
        return Result.success(true)
    }

    /**
     * Builds a WhatsApp 1-tap direct chat URL (wa.me)
     */
    fun createWhatsAppDirectIntent(phone: String, text: String): Intent {
        val cleanPhone = phone.replace("+", "").replace(" ", "")
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val url = "https://wa.me/$cleanPhone?text=$encodedText"
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    /**
     * Dispatches automatic Écrit / Oral / Success list alerts to candidate WhatsApp
     */
    suspend fun dispatchConcoursUpdateAlert(
        context: Context,
        rawPhone: String,
        concoursTitle: String,
        ministry: String,
        stageType: String, // "ECRIT", "ORAL", "RESULTAT"
        pdfUrl: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val normalized = normalizeMoroccanPhoneNumber(rawPhone) ?: return@withContext Result.failure(
            IllegalArgumentException("Numéro WhatsApp invalide")
        )

        // Show Heads-Up Notification on device
        try {
            MihnatiNotificationHelper.showWhatsAppConvocationAlert(
                context = context,
                phone = normalized,
                concoursTitle = concoursTitle,
                ministry = ministry,
                stage = stageType,
                pdfUrl = pdfUrl
            )
        } catch (e: Exception) {
            Log.w(TAG, "Notification alert failed: ${e.message}")
        }

        // Format official WhatsApp message
        val (emoji, stageHeader) = when (stageType.uppercase()) {
            "ORAL" -> "🎙️" to "Convocations Épreuve Orale"
            "RESULTAT", "ADMIS" -> "🏆" to "Résultats Définitifs & Liste des Admis"
            else -> "📝" to "Convocations Épreuve Écrite"
        }

        val messageText = """
            $emoji *Mihnati (مهنتي) - Alerte Convocations*
            
            🏛️ *Administration :* $ministry
            📋 *Concours :* $concoursTitle
            📌 *Étape :* $stageHeader
            
            📄 *Télécharger la liste officielle (PDF) :*
            $pdfUrl
            
            _(Alerte automatique transmise à vos candidatures suivies sur Mihnati)_
        """.trimIndent()

        val gatewayUrl = externalWhatsAppGatewayUrl
        if (!gatewayUrl.isNullOrEmpty()) {
            try {
                // 1. Send structured text alert with PDF link
                sendViaWhatsAppGateway(
                    gatewayUrl = gatewayUrl,
                    apiKey = externalWhatsAppApiKey,
                    phone = normalized,
                    message = messageText
                )

                // 2. If Green-API instance is configured and URL is a PDF, also deliver the PDF document straight to WhatsApp
                val instId = configuredInstanceId
                val token = configuredToken
                if (!instId.isNullOrEmpty() && !token.isNullOrEmpty() && (pdfUrl.contains("pdf", ignoreCase = true) || pdfUrl.startsWith("http"))) {
                    try {
                        sendPdfFileViaGreenApi(
                            instanceId = instId,
                            token = token,
                            phone = normalized,
                            pdfUrl = pdfUrl,
                            fileName = "Convocation_${stageType}.pdf",
                            caption = "📄 Liste officielle $stageHeader - $ministry"
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Green-API sendFileByUrl failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed sending WhatsApp webhook: ${e.message}")
            }
        }

        Result.success(true)
    }

    suspend fun sendGreenApiTestMessage(context: Context, targetPhone: String): Result<String> = withContext(Dispatchers.IO) {
        val normalized = normalizeMoroccanPhoneNumber(targetPhone)
            ?: return@withContext Result.failure(IllegalArgumentException("Numéro marocain invalide."))

        val gatewayUrl = externalWhatsAppGatewayUrl
        if (gatewayUrl.isNullOrEmpty()) {
            return@withContext Result.failure(IllegalStateException("Passerelle Green-API non configurée."))
        }

        try {
            sendViaWhatsAppGateway(
                gatewayUrl = gatewayUrl,
                apiKey = externalWhatsAppApiKey,
                phone = normalized,
                message = "🇲🇦 *Mihnati (مهنتي)*\n\n✅ *Passerelle WhatsApp Connectée !*\n\nVotre compte Green-API est opérationnel. Vous recevrez désormais automatiquement :\n- Vos codes de validation\n- Les listes des convoqués à l'écrit\n- Les convocations aux oraux\n- Les résultats finaux & listes des admis avec fichiers PDF."
            )
            Result.success("Message de test WhatsApp envoyé avec succès !")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLastActiveOtpForDebug(): String? = lastGeneratedOtp

    fun secondsUntilResend(): Int {
        val elapsedSec = ((System.currentTimeMillis() - lastSentEpochMs) / 1000).toInt()
        val cooldownSec = 45
        return (cooldownSec - elapsedSec).coerceAtLeast(0)
    }

    private fun sendViaWhatsAppGateway(
        gatewayUrl: String,
        apiKey: String?,
        phone: String,
        message: String
    ) {
        val url = URL(gatewayUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        if (!apiKey.isNullOrEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.setRequestProperty("x-api-key", apiKey)
        }
        conn.doOutput = true

        val cleanPhone = phone.replace("+", "")
        val jsonPayload = """
            {
              "phone": "$cleanPhone",
              "chatId": "$cleanPhone@c.us",
              "message": ${jsonEscape(message)}
            }
        """.trimIndent()

        OutputStreamWriter(conn.outputStream).use { it.write(jsonPayload) }
        val responseCode = conn.responseCode
        Log.d(TAG, "WhatsApp Gateway response code: $responseCode")
        conn.disconnect()
    }

    private fun sendPdfFileViaGreenApi(
        instanceId: String,
        token: String,
        phone: String,
        pdfUrl: String,
        fileName: String,
        caption: String
    ) {
        val sendFileUrl = "https://api.green-api.com/waInstance$instanceId/sendFileByUrl/$token"
        val url = URL(sendFileUrl)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        val cleanPhone = phone.replace("+", "").replace(" ", "")
        val jsonPayload = """
            {
              "chatId": "$cleanPhone@c.us",
              "urlFile": "$pdfUrl",
              "fileName": "$fileName",
              "caption": ${jsonEscape(caption)}
            }
        """.trimIndent()

        OutputStreamWriter(conn.outputStream).use { it.write(jsonPayload) }
        val code = conn.responseCode
        Log.d(TAG, "Green-API sendFileByUrl response code: $code")
        conn.disconnect()
    }

    private fun jsonEscape(str: String): String {
        val escaped = str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
        return "\"$escaped\""
    }
}
