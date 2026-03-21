package com.timetodo.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.timetodo.MainActivity
import com.timetodo.R
import com.timetodo.data.AppDatabase
import com.timetodo.data.TaskRepository
import com.timetodo.data.entity.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.timetodo.TASK_REMINDER" -> {
                handleTaskReminder(context)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // Reschedule alarms after device reboot
                ReminderScheduler.scheduleAllReminders(context)
            }
        }
    }
    
    private fun handleTaskReminder(context: Context) {
        val database = AppDatabase.getDatabase(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            val today = LocalDate.now()
            val dateMillis = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val tasks = database.taskDao().getTasksForDateSync(dateMillis)
            val pendingTasks = tasks.filter { it.status == TaskStatus.PENDING }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val title: String
            val text: String
            
            if (pendingTasks.isNotEmpty()) {
                title = "Time ToDo Reminder"
                text = "You have ${pendingTasks.size} pending task${if (pendingTasks.size > 1) "s" else ""} for today"
            } else if (tasks.isEmpty()) {
                title = "Time ToDo Reminder"
                text = "Plan your day! Create tasks to stay productive"
            } else {
                // All tasks completed, no need to remind
                return@launch
            }
            
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                2000,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(context, NotificationChannels.TASK_REMINDERS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            
            notificationManager.notify(3000, notification)
        }
    }
}
