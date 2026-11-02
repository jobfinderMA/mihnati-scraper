package com.mihnati.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.mihnati.app.MainActivity
import com.mihnati.app.R
import java.util.concurrent.TimeUnit

object MihnatiNotificationHelper {
    const val CHANNEL_CONCOURS_NEW = "channel_mihnati_concours"
    const val CHANNEL_DEADLINES = "channel_mihnati_deadlines"
    const val CHANNEL_CONVOCATIONS = "channel_mihnati_convocations"
    const val CHANNEL_AUTH_OTP = "channel_mihnati_auth_otp"

    const val EXTRA_OPPORTUNITY_ID = "extra_opportunity_id"
    const val EXTRA_TARGET_TAB = "extra_target_tab"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Channel: New Concours & Opportunities
            val concoursChannel = NotificationChannel(
                CHANNEL_CONCOURS_NEW,
                "Nouveaux Concours & Postes 🇲🇦",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertes instantanées dès qu'un concours est publié sur emploi-public.ma"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            // 2. Channel: Urgent Deadlines
            val deadlinesChannel = NotificationChannel(
                CHANNEL_DEADLINES,
                "Rappels d'Échéances & Clôtures ⏳",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Rappels 48h et 24h avant la fermeture du dépôt de dossier"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            // 3. Channel: Exam Lists & Convocations
            val convocationsChannel = NotificationChannel(
                CHANNEL_CONVOCATIONS,
                "Listes des Convoqués & Résultats 📋",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Publication des listes écrites et orales pour vos concours suivis"
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(concoursChannel, deadlinesChannel, convocationsChannel))
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun showConcoursAlert(
        context: Context,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt(),
        concoursId: String,
        title: String,
        administration: String,
        postsCount: Int,
        deadline: String
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPPORTUNITY_ID, concoursId)
            putExtra(EXTRA_TARGET_TAB, "Opportunites")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bigText = StringBuilder()
            .append("🏛️ $administration\n")
            .append("💼 $postsCount poste(s) ouvert(s)\n")
            .append("⏳ Dernier délai : $deadline")
            .toString()

        val builder = NotificationCompat.Builder(context, CHANNEL_CONCOURS_NEW)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📢 Nouveau Concours : $title")
            .setContentText("$administration • $postsCount postes disponibles")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF006C4A.toInt()) // Mihnati Chérifien Green
            .addAction(0, "Voir l'offre & Postuler", pendingIntent)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    /**
     * Dispatches a high-priority heads-up notification with the 6-digit Gmail verification code
     */
    fun showAuthOtpNotification(
        context: Context,
        email: String,
        otpCode: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            2002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_AUTH_OTP)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Code de vérification Gmail : $otpCode")
            .setContentText("Utilisez le code $otpCode pour valider votre compte Mihnati ($email).")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Votre code secret de confirmation Mihnati est :\n\n🔐 $otpCode\n\nCe code est valable 10 minutes pour l'adresse $email. Ne le partagez avec personne.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(9001, notification)
    }

    /**
     * Dispatches a high-priority heads-up notification with the 6-digit WhatsApp verification code
     */
    fun showWhatsAppAuthOtpNotification(
        context: Context,
        phone: String,
        otpCode: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            2003,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_AUTH_OTP)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Code WhatsApp Mihnati : $otpCode 💬")
            .setContentText("Code secret $otpCode pour valider votre numéro WhatsApp ($phone).")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Votre code de vérification WhatsApp Mihnati est :\n\n💬 $otpCode\n\nCe code est valable 10 minutes pour le $phone. Ne le communiquez à personne.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setColor(0xFF25D366.toInt()) // WhatsApp Green
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(9002, notification)
    }

    /**
     * Dispatches an automatic notification for published Écrit, Oral or Final Admis lists
     */
    fun showWhatsAppConvocationAlert(
        context: Context,
        phone: String,
        concoursTitle: String,
        ministry: String,
        stage: String,
        pdfUrl: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            2004,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (stageIcon, stageTitle) = when {
            stage.contains("oral", ignoreCase = true) -> "🎙️" to "Liste de l'épreuve orale disponible !"
            stage.contains("admis", ignoreCase = true) || stage.contains("resultat", ignoreCase = true) -> "🏆" to "Résultats définitifs & Liste des admis !"
            else -> "📝" to "Liste des convoqués à l'épreuve écrite !"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_CONVOCATIONS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$stageIcon Mihnati WhatsApp : $stageTitle")
            .setContentText("$ministry • $concoursTitle")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$stageIcon Mihnati Alert Convocations\n\nLa liste officielle pour :\n$ministry - $concoursTitle vient d'être publiée sur emploi-public.ma.\n\n📲 Alerte envoyée sur votre WhatsApp : $phone")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(0xFF25D366.toInt())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify((1000..9999).random(), notification)
    }

    fun showTestNotification(context: Context) {
        showConcoursAlert(
            context = context,
            notificationId = 1001,
            concoursId = "mef-admin-2026",
            title = "Administrateur 2ème grade - Échelle 11",
            administration = "Ministère de l'Économie et des Finances",
            postsCount = 120,
            deadline = "15 Novembre 2026 (16:30)"
        )
    }

    fun schedulePeriodicCloudSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicSyncRequest = PeriodicWorkRequestBuilder<ConcoursSyncWorker>(
            1, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "mihnati_periodic_cloud_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }
}
