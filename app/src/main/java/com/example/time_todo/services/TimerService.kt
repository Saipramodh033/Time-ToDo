package com.example.time_todo.services

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.time_todo.R
import com.example.time_todo.core.ActiveTaskStore
import com.example.time_todo.data.local.AppDatabase
import com.example.time_todo.ui.TimerState
import kotlinx.coroutines.*

class TimerService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_COMPLETE = "ACTION_COMPLETE"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "timer_channel"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var store: ActiveTaskStore
    private val dao by lazy { AppDatabase.get(this).taskDao() }

    private var running = false
    private var elapsed = 0
    private var activeTaskId: Long = -1L

    override fun onCreate() {
        super.onCreate()
        store = ActiveTaskStore(this)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                activeTaskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                startTask()
            }
            ACTION_PAUSE -> pauseTask()
            ACTION_RESUME -> resumeTask()
            ACTION_COMPLETE -> completeTask()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    /* ===================== TASK CONTROL ===================== */

    private fun startTask() {
        if (running) return

        running = true
        elapsed = store.elapsed()

        // MUST be called immediately
        startForeground(
            NOTIFICATION_ID,
            buildNotification(elapsed)
        )

        if (activeTaskId != -1L) {
            serviceScope.launch {
                dao.updateStatus(activeTaskId, "IN_PROGRESS")
            }
        }

        startTimerLoop()
    }




    private fun pauseTask() {
        running = false
    }

    private fun resumeTask() {
        if (running || activeTaskId == -1L) return
        running = true
        startTimerLoop()
    }

    private fun completeTask() {
        running = false

        serviceScope.launch {
            dao.updateStatus(activeTaskId, "COMPLETED")
        }

        store.clear()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /* ===================== TIMER LOOP ===================== */

    private fun startTimerLoop() {
        serviceScope.launch {
            while (running) {
                delay(1_000) // ✅ CRITICAL FIX
                elapsed++

                store.updateElapsed(elapsed)
                dao.updateElapsed(activeTaskId, elapsed)

                TimerState.elapsedSeconds.value = elapsed
                updateNotification(elapsed)
            }
        }
    }

    /* ===================== NOTIFICATION ===================== */

    private fun buildNotification(seconds: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Task running")
            .setContentText("Elapsed: ${seconds}s")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(seconds: Int) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(seconds))
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Task Timer",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    override fun onDestroy() {
        running = false
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
