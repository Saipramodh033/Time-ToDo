package com.timetodo.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.timetodo.domain.TimerManager

class TimerWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val timerManager = TimerManager.getInstance(applicationContext)
        val state = timerManager.timerState.value

        // If timer is supposed to be running but service might have been killed
        if (state.isRunning && !state.isPaused) {
            // Restart the service
            // This is a backup mechanism to ensure timer continuity
            // In practice, the foreground service should handle most cases
        }

        return Result.success()
    }
}
