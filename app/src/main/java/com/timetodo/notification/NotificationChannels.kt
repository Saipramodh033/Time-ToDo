package com.timetodo.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val TASK_REMINDERS_CHANNEL_ID = "task_reminders"
    const val FOCUS_REMINDER_CHANNEL_ID = "focus_reminder"
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Task Reminders Channel
            val taskRemindersChannel = NotificationChannel(
                TASK_REMINDERS_CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic reminders about your tasks"
                enableVibration(true)
            }
            
            // Focus Reminder Channel
            val focusReminderChannel = NotificationChannel(
                FOCUS_REMINDER_CHANNEL_ID,
                "Focus Mode",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you to return to active tasks"
                enableVibration(true)
            }
            
            notificationManager.createNotificationChannel(taskRemindersChannel)
            notificationManager.createNotificationChannel(focusReminderChannel)
        }
    }
}
