package com.timetodo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.timetodo.MainActivity
import com.timetodo.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TimerService.EXTRA_TASK_ID, -1)
        val executionId = intent.getLongExtra(TimerService.EXTRA_EXECUTION_ID, -1)

        if (taskId != -1L && executionId != -1L) {
            showCompletionNotification(context, taskId, executionId)
        }
    }

    private fun showCompletionNotification(context: Context, taskId: Long, executionId: Long) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("taskId", taskId)
            putExtra("executionId", executionId)
            putExtra("showCompletion", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setContentTitle("Task Completed!")
            .setContentText("Your task timer has finished. Tap to complete or extend.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(COMPLETION_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Task Completion",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when task timer finishes"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ALARM_CHANNEL_ID = "alarm_channel"
        private const val COMPLETION_NOTIFICATION_ID = 2
    }
}
