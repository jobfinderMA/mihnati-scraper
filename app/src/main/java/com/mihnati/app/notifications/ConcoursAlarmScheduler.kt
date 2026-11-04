package com.mihnati.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mihnati.app.data.model.Opportunity

object ConcoursAlarmScheduler {

    private const val PREFS_NAME = "mihnati_alarms_prefs"
    private const val KEY_ACTIVE_IDS = "active_alarm_opportunity_ids"

    fun isAlarmActive(context: Context, opportunityId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_ACTIVE_IDS, emptySet()) ?: emptySet()
        return set.contains(opportunityId)
    }

    fun getActiveAlarmCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_ACTIVE_IDS, emptySet()) ?: emptySet()
        return set.size
    }

    fun scheduleDeadlineAlarms(context: Context, opportunity: Opportunity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // 1. Calculate realistic future trigger timestamps based on remaining days
        val now = System.currentTimeMillis()
        val daysRemaining = maxOf(1, opportunity.deadlineDaysRemaining)

        // 48h trigger (2 days before) or fallback to offset
        val trigger48h = if (daysRemaining > 2) {
            now + ((daysRemaining - 2) * 24 * 3600 * 1000L)
        } else {
            now + (60 * 1000L) // 1 minute demo fallback if already within 48h
        }

        // 24h trigger (1 day before) or fallback to offset
        val trigger24h = if (daysRemaining > 1) {
            now + ((daysRemaining - 1) * 24 * 3600 * 1000L)
        } else {
            now + (120 * 1000L) // 2 minutes demo fallback
        }

        // Schedule 48h Alarm
        val intent48h = createAlarmIntent(context, opportunity, ConcoursAlarmReceiver.ALARM_TYPE_48H)
        val pending48h = PendingIntent.getBroadcast(
            context,
            (opportunity.id + ConcoursAlarmReceiver.ALARM_TYPE_48H).hashCode(),
            intent48h,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setAlarm(alarmManager, trigger48h, pending48h)

        // Schedule 24h Alarm
        val intent24h = createAlarmIntent(context, opportunity, ConcoursAlarmReceiver.ALARM_TYPE_24H)
        val pending24h = PendingIntent.getBroadcast(
            context,
            (opportunity.id + ConcoursAlarmReceiver.ALARM_TYPE_24H).hashCode(),
            intent24h,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setAlarm(alarmManager, trigger24h, pending24h)

        // Save active state to SharedPreferences
        markAlarmActive(context, opportunity.id, true)
    }

    /**
     * Schedules a quick 5-second test alarm so the candidate can immediately see and verify
     * the device-level notification firing on their screen.
     */
    fun scheduleQuickTestAlarm(context: Context, opportunity: Opportunity, delaySeconds: Int = 5) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTime = System.currentTimeMillis() + (delaySeconds * 1000L)

        val intent = createAlarmIntent(context, opportunity, ConcoursAlarmReceiver.ALARM_TYPE_48H)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (opportunity.id + "TEST").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarm(alarmManager, triggerTime, pendingIntent)
        markAlarmActive(context, opportunity.id, true)
    }

    fun cancelDeadlineAlarms(context: Context, opportunityId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Cancel 48h
        val intent48h = Intent(context, ConcoursAlarmReceiver::class.java)
        val pending48h = PendingIntent.getBroadcast(
            context,
            (opportunityId + ConcoursAlarmReceiver.ALARM_TYPE_48H).hashCode(),
            intent48h,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pending48h != null) {
            alarmManager.cancel(pending48h)
            pending48h.cancel()
        }

        // Cancel 24h
        val intent24h = Intent(context, ConcoursAlarmReceiver::class.java)
        val pending24h = PendingIntent.getBroadcast(
            context,
            (opportunityId + ConcoursAlarmReceiver.ALARM_TYPE_24H).hashCode(),
            intent24h,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pending24h != null) {
            alarmManager.cancel(pending24h)
            pending24h.cancel()
        }

        markAlarmActive(context, opportunityId, false)
    }

    private fun setAlarm(alarmManager: AlarmManager, triggerTimeMs: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        }
    }

    private fun createAlarmIntent(context: Context, opportunity: Opportunity, alarmType: String): Intent {
        return Intent(context, ConcoursAlarmReceiver::class.java).apply {
            action = ConcoursAlarmReceiver.ACTION_DEADLINE_ALARM
            putExtra(ConcoursAlarmReceiver.EXTRA_OPPORTUNITY_ID, opportunity.id)
            putExtra(ConcoursAlarmReceiver.EXTRA_ALARM_TYPE, alarmType)
            putExtra(ConcoursAlarmReceiver.EXTRA_TITLE, opportunity.title)
            putExtra(ConcoursAlarmReceiver.EXTRA_ADMINISTRATION, opportunity.organization)
            putExtra(ConcoursAlarmReceiver.EXTRA_DEADLINE, opportunity.deadline)
        }
    }

    private fun markAlarmActive(context: Context, opportunityId: String, active: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentSet = prefs.getStringSet(KEY_ACTIVE_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (active) {
            currentSet.add(opportunityId)
        } else {
            currentSet.remove(opportunityId)
        }
        prefs.edit().putStringSet(KEY_ACTIVE_IDS, currentSet).apply()
    }
}
