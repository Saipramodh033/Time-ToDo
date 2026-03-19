package com.example.time_todo.core

import android.content.Context

class ActiveTaskStore(context: Context) {

    private val prefs =
        context.getSharedPreferences("active_task_store", Context.MODE_PRIVATE)

    fun startTask(taskId: Long) {
        prefs.edit()
            .putLong("task_id", taskId)
            .putLong("start_time", System.currentTimeMillis())
            .putInt("elapsed", 0)
            .apply()
    }

    fun updateElapsed(seconds: Int) {
        prefs.edit().putInt("elapsed", seconds).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun hasActiveTask(): Boolean =
        prefs.getLong("task_id", -1L) != -1L

    fun elapsed(): Int =
        prefs.getInt("elapsed", 0)
}
