package com.mihnati.app.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ConcoursSyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "ConcoursSyncWorker"
        private const val PREFS_NAME = "mihnati_sync_prefs"
        private const val KEY_KNOWN_IDS = "known_concours_ids"
        private const val CLOUD_CDN_URL = "https://raw.githubusercontent.com/jobfinderMA/mihnati-scraper/main/scraper/scraped_data.json"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val url = URL(CLOUD_CDN_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.requestMethod = "GET"

            if (connection.responseCode == 200) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonString)
                val jsonArray = root.optJSONArray("concours")

                if (jsonArray != null && jsonArray.length() > 0) {
                    val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val knownIds = prefs.getStringSet(KEY_KNOWN_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()

                    val currentIds = mutableSetOf<String>()
                    var newestConcoursToAlert: JSONObject? = null

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val id = obj.optString("id", "")
                        if (id.isNotEmpty()) {
                            currentIds.add(id)
                            if (!knownIds.contains(id) && newestConcoursToAlert == null) {
                                newestConcoursToAlert = obj
                            }
                        }
                    }

                    // If we found a brand-new concours that wasn't in our known database:
                    if (newestConcoursToAlert != null && knownIds.isNotEmpty()) {
                        val title = newestConcoursToAlert.optString("title", "Nouveau Concours de Recrutement")
                        val admin = newestConcoursToAlert.optString("administration", "Administration Publique")
                        val posts = newestConcoursToAlert.optInt("posts_count", 1)
                        val deadline = newestConcoursToAlert.optString("deadline", "Bientôt").replace("\n", " ").trim()
                        val id = newestConcoursToAlert.optString("id", "ep-alert")

                        MihnatiNotificationHelper.showConcoursAlert(
                            context = appContext,
                            concoursId = id,
                            title = title,
                            administration = admin,
                            postsCount = posts,
                            deadline = deadline
                        )
                    }

                    // Save the updated set of known IDs
                    prefs.edit().putStringSet(KEY_KNOWN_IDS, currentIds).apply()
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Background sync error: ${e.message}")
            Result.retry()
        }
    }
}
