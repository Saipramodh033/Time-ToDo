package com.timetodo.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.timetodo.MainActivity
import com.timetodo.R
import com.timetodo.data.AppDatabase
import com.timetodo.data.TaskRepository
import com.timetodo.domain.TimerManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var timerManager: TimerManager
    private lateinit var repository: TaskRepository
    private var timerJob: Job? = null
    private var alarmManager: AlarmManager? = null

    override fun onCreate() {
        super.onCreate()
        timerManager = TimerManager.getInstance(this)
        val database = AppDatabase.getDatabase(this)
        repository = TaskRepository(database)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
                val executionId = intent.getLongExtra(EXTRA_EXECUTION_ID, -1)
                val durationMinutes = intent.getIntExtra(EXTRA_DURATION, 25)
                
                if (taskId != -1L && executionId != -1L) {
                    startForeground(NOTIFICATION_ID, createTimerNotification(0, taskId))
                    startTimerLoop(taskId, executionId, durationMinutes)
                }
            }
            ACTION_PAUSE_TIMER -> {
                timerManager.pauseTimer()
                timerJob?.cancel()
            }
            ACTION_RESUME_TIMER -> {
                val state = timerManager.timerState.value
                if (state.taskId != null && state.executionId != null) {
                    val task = runBlocking {
                        repository.getTaskByIdSync(state.taskId)
                    }
                    startTimerLoop(state.taskId, state.executionId, task?.durationMinutes ?: 25)
                }
            }
            ACTION_STOP_TIMER -> {
                stopTimerAndService()
            }
        }
        return START_STICKY
    }

    private fun startTimerLoop(taskId: Long, executionId: Long, durationMinutes: Int) {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            val state = timerManager.timerState.first()
            var elapsed = state.elapsedSeconds
            val targetSeconds = durationMinutes * 60

            while (isActive && elapsed < targetSeconds) {
                if (!timerManager.timerState.value.isPaused) {
                    elapsed++
                    timerManager.updateElapsedTime(elapsed)
                    
                    // Update execution in database
                    val execution = repository.getExecutionById(executionId)
                    execution?.let {
                        repository.updateExecution(it.copy(elapsedSeconds = elapsed))
                    }

                    // Update notification
                    val notification = createTimerNotification(elapsed, taskId)
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
                delay(1000)
            }

            // Timer completed
            if (elapsed >= targetSeconds) {
                scheduleAlarm(taskId, executionId)
            }
        }
    }

    private fun scheduleAlarm(taskId: Long, executionId: Long) {
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_EXECUTION_ID, executionId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 100 // Immediate

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager?.canScheduleExactAlarms() == true) {
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    private fun createTimerNotification(elapsedSeconds: Int, taskId: Long): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("taskId", taskId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        return NotificationCompat.Builder(this, TIMER_CHANNEL_ID)
            .setContentTitle("Task in Progress")
            .setContentText("Time: $timeString")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timerChannel = NotificationChannel(
                TIMER_CHANNEL_ID,
                "Task Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active task timer"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(timerChannel)
        }
    }

    private fun stopTimerAndService() {
        timerJob?.cancel()
        timerManager.stopTimer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START_TIMER = "com.timetodo.START_TIMER"
        const val ACTION_PAUSE_TIMER = "com.timetodo.PAUSE_TIMER"
        const val ACTION_RESUME_TIMER = "com.timetodo.RESUME_TIMER"
        const val ACTION_STOP_TIMER = "com.timetodo.STOP_TIMER"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_EXECUTION_ID = "execution_id"
        const val EXTRA_DURATION = "duration"
        const val TIMER_CHANNEL_ID = "timer_channel"
        private const val NOTIFICATION_ID = 1
        private const val ALARM_REQUEST_CODE = 100
    }
}
