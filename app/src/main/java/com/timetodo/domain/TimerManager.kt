package com.timetodo.domain

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TimerState(
    val taskId: Long? = null,
    val executionId: Long? = null,
    val startTimeMillis: Long = 0,
    val elapsedSeconds: Int = 0,
    val isPaused: Boolean = false,
    val isRunning: Boolean = false
)

class TimerManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var tickerJob: Job? = null

    init {
        // Restore state from SharedPreferences
        restoreState()
    }

    fun startTimer(taskId: Long, executionId: Long) {
        stopTicker()
        val state = TimerState(
            taskId = taskId,
            executionId = executionId,
            startTimeMillis = System.currentTimeMillis(),
            elapsedSeconds = 0,
            isPaused = false,
            isRunning = true
        )
        _timerState.value = state
        saveState(state)
        startTicker()
    }

    fun pauseTimer() {
        val current = _timerState.value
        if (current.isRunning && !current.isPaused) {
            stopTicker()
            val state = current.copy(isPaused = true)
            _timerState.value = state
            saveState(state)
        }
    }

    fun resumeTimer() {
        val current = _timerState.value
        if (current.isRunning && current.isPaused) {
            val state = current.copy(
                isPaused = false,
                startTimeMillis = System.currentTimeMillis() - (current.elapsedSeconds * 1000L)
            )
            _timerState.value = state
            saveState(state)
            startTicker()
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                delay(1000)
                val current = _timerState.value
                if (current.isRunning && !current.isPaused) {
                    val newElapsed = ((System.currentTimeMillis() - current.startTimeMillis) / 1000).toInt()
                    updateElapsedTime(newElapsed)
                }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun updateElapsedTime(elapsedSeconds: Int) {
        val current = _timerState.value
        if (current.isRunning) {
            val state = current.copy(elapsedSeconds = elapsedSeconds)
            _timerState.value = state
            saveState(state)
        }
    }

    fun stopTimer() {
        stopTicker()
        _timerState.value = TimerState()
        clearState()
    }

    private fun saveState(state: TimerState) {
        prefs.edit().apply {
            putLong(KEY_TASK_ID, state.taskId ?: -1)
            putLong(KEY_EXECUTION_ID, state.executionId ?: -1)
            putLong(KEY_START_TIME, state.startTimeMillis)
            putInt(KEY_ELAPSED_SECONDS, state.elapsedSeconds)
            putBoolean(KEY_IS_PAUSED, state.isPaused)
            putBoolean(KEY_IS_RUNNING, state.isRunning)
            apply()
        }
    }

    private fun restoreState() {
        val taskId = prefs.getLong(KEY_TASK_ID, -1)
        val executionId = prefs.getLong(KEY_EXECUTION_ID, -1)
        val isRunning = prefs.getBoolean(KEY_IS_RUNNING, false)

        if (isRunning && taskId != -1L && executionId != -1L) {
            // Validate task still exists in database before restoring
            CoroutineScope(Dispatchers.IO).launch {
                val database = com.timetodo.data.AppDatabase.getDatabase(context)
                val task = database.taskDao().getTaskByIdSync(taskId)
                
                if (task != null) {
                    // Task exists, restore timer state
                    val state = TimerState(
                        taskId = taskId,
                        executionId = executionId,
                        startTimeMillis = prefs.getLong(KEY_START_TIME, 0),
                        elapsedSeconds = prefs.getInt(KEY_ELAPSED_SECONDS, 0),
                        isPaused = prefs.getBoolean(KEY_IS_PAUSED, false),
                        isRunning = true
                    )
                    _timerState.value = state
                    if (state.isRunning && !state.isPaused) {
                        startTicker()
                    }
                } else {
                    // Task was deleted, clear timer state
                    clearState()
                }
            }
        }
    }

    private fun clearState() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "timer_prefs"
        private const val KEY_TASK_ID = "task_id"
        private const val KEY_EXECUTION_ID = "execution_id"
        private const val KEY_START_TIME = "start_time"
        private const val KEY_ELAPSED_SECONDS = "elapsed_seconds"
        private const val KEY_IS_PAUSED = "is_paused"
        private const val KEY_IS_RUNNING = "is_running"

        @Volatile
        private var INSTANCE: TimerManager? = null

        fun getInstance(context: Context): TimerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TimerManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
