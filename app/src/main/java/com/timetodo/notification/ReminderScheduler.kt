package com.timetodo.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderScheduler {
    private val REMINDER_HOURS = listOf(7, 10, 13, 16, 19, 22) // 7 AM, 10 AM, 1 PM, 4 PM, 7 PM, 10 PM
    
    fun scheduleAllReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        REMINDER_HOURS.forEachIndexed { index, hour ->
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.timetodo.TASK_REMINDER"
                putExtra("REMINDER_HOUR", hour)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1000 + index, // Unique request code for each alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                
                // If the time has passed today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            
            // Schedule repeating alarm
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }
    
    fun cancelAllReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        REMINDER_HOURS.forEachIndexed { index, hour ->
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.timetodo.TASK_REMINDER"
                putExtra("REMINDER_HOUR", hour)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1000 + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
        }
    }
}
