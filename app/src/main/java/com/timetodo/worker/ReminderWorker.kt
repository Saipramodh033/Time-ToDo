package com.timetodo.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.timetodo.data.AppDatabase
import com.timetodo.data.TaskRepository
import com.timetodo.domain.RecurrenceCalculator
import com.timetodo.util.NotificationHelper
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database)
        val notificationHelper = NotificationHelper(applicationContext)

        val today = LocalDate.now()
        val allTasks = repository.getTasksForDate(today).first()
        val pendingTasks = allTasks.filter { task ->
            RecurrenceCalculator.shouldShowOnDate(task, today)
        }

        if (pendingTasks.isNotEmpty()) {
            val title = "You have ${pendingTasks.size} tasks today"
            val message = "Don't forget to focus and get things done!"
            notificationHelper.showReminderNotification(title, message)
        }

        return Result.success()
    }
}
