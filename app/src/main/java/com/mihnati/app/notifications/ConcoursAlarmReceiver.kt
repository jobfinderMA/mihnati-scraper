package com.mihnati.app.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mihnati.app.MainActivity
import com.mihnati.app.R

class ConcoursAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val opportunityId = intent.getStringExtra(EXTRA_OPPORTUNITY_ID) ?: return
        val alarmType = intent.getStringExtra(EXTRA_ALARM_TYPE) ?: ALARM_TYPE_48H
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Concours National"
        val administration = intent.getStringExtra(EXTRA_ADMINISTRATION) ?: "Administration Publique"
        val deadline = intent.getStringExtra(EXTRA_DEADLINE) ?: "Prochainement"

        if (!MihnatiNotificationHelper.hasNotificationPermission(context)) return

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MihnatiNotificationHelper.EXTRA_OPPORTUNITY_ID, opportunityId)
            putExtra(MihnatiNotificationHelper.EXTRA_TARGET_TAB, "Opportunites")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (opportunityId + alarmType).hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (notifTitle, notifSummary, accentColor) = when (alarmType) {
            ALARM_TYPE_24H -> Triple(
                "DERNIER JOUR (J-1) : Cloture demain !",
                "Le concours : $title ($administration) se cloture demain ($deadline). Deposez votre dossier sans tarder !",
                0xFFD32F2F.toInt() // Crimson Red
            )
            ALARM_TYPE_EXAM_24H -> Triple(
                "Rappel Convocation : Epreuve ecrite demain !",
                "Votre examen pour : $title ($administration) a lieu demain. Munissez-vous de votre convocation et CNIE.",
                0xFF1976D2.toInt() // Royal Blue
            )
            else -> Triple(
                "Rappel J-2 : Cloture dans 48 heures !",
                "Plus que 2 jours pour postuler au concours : $title ($administration). Dernier delai : $deadline.",
                0xFFF57C00.toInt() // Urgent Amber
            )
        }

        val notification = NotificationCompat.Builder(context, MihnatiNotificationHelper.CHANNEL_DEADLINES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notifTitle)
            .setContentText("$administration - $title")
            .setStyle(NotificationCompat.BigTextStyle().bigText(notifSummary))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setColor(accentColor)
            .setContentIntent(pendingIntent)
            .addAction(0, "Ouvrir l'offre & Postuler", pendingIntent)
            .build()

        val notifId = (opportunityId + alarmType).hashCode()
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    companion object {
        const val ACTION_DEADLINE_ALARM = "com.mihnati.app.ACTION_CONCOURS_DEADLINE_ALARM"
        const val EXTRA_OPPORTUNITY_ID = "extra_opportunity_id"
        const val EXTRA_ALARM_TYPE = "extra_alarm_type"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ADMINISTRATION = "extra_administration"
        const val EXTRA_DEADLINE = "extra_deadline"

        const val ALARM_TYPE_48H = "ALARM_48H"
        const val ALARM_TYPE_24H = "ALARM_24H"
        const val ALARM_TYPE_EXAM_24H = "ALARM_EXAM_24H"
    }
}
