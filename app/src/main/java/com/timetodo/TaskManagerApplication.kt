package com.timetodo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class TaskManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        com.timetodo.notification.NotificationChannels.createNotificationChannels(this)
    }

    private fun createNotificationChannels() {
        val timerChannel = NotificationChannel(
            TIMER_CHANNEL_ID,
            "Task Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows the remaining time for the current task"
        }

        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Task Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for task reminders and end of timers"
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(timerChannel)
        notificationManager.createNotificationChannel(alarmChannel)
    }

    companion object {
        const val TIMER_CHANNEL_ID = "task_timer_channel"
        const val ALARM_CHANNEL_ID = "task_alarm_channel"
    }
}
