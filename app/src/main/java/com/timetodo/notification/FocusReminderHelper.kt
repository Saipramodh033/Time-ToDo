package com.timetodo.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.timetodo.MainActivity
import com.timetodo.R
import com.timetodo.data.AppDatabase
import com.timetodo.domain.TimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FocusReminderHelper {
    private const val FOCUS_NOTIFICATION_ID = 4000
    
    fun showFocusReminder(context: Context) {
        val timerManager = TimerManager.getInstance(context)
        val timerState = timerManager.timerState.value
        
        // Only show if timer is actively running (not paused, not stopped)
        if (timerState.taskId == null || 
            timerState.elapsedSeconds == 0 || 
            !timerState.isRunning || 
            timerState.isPaused) {
            return
        }
        
        // Format elapsed time
        val hours = timerState.elapsedSeconds / 3600
        val minutes = (timerState.elapsedSeconds % 3600) / 60
        val seconds = timerState.elapsedSeconds % 60
        val timeString = if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
        
        // Fetch task title from database and verify task still exists
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(context)
            val task = database.taskDao().getTaskByIdSync(timerState.taskId!!)
            
            // If task was deleted, stop timer and don't show notification
            if (task == null) {
                timerManager.stopTimer()
                return@launch
            }
            
            val taskTitle = task.title
            
            // Create intent to return to task execution screen
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to_task", timerState.taskId)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                5000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(context, NotificationChannels.FOCUS_REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Task in Progress")
                .setContentText("$taskTitle - $timeString elapsed")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(FOCUS_NOTIFICATION_ID, notification)
        }
    }
    
    fun cancelFocusReminder(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(FOCUS_NOTIFICATION_ID)
    }
}
