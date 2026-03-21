package com.timetodo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.timetodo.data.TaskRepository
import com.timetodo.data.entity.Task
import com.timetodo.data.entity.TaskExecution
import com.timetodo.data.entity.TaskStatus
import com.timetodo.domain.TimerManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

class TaskExecutionViewModel(
    private val taskId: Long,
    private val repository: TaskRepository,
    private val timerManager: TimerManager
) : ViewModel() {

    val task: StateFlow<Task?> = repository.getTaskById(taskId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun startTask() {
        viewModelScope.launch {
            task.value?.let { currentTask ->
                if (currentTask.status == TaskStatus.PENDING) {
                    repository.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS)
                    // executionId will be handled when the task is completed or we can create a placeholder
                    timerManager.startTimer(taskId, 0L) 
                }
            }
        }
    }

    fun pauseTask() {
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, TaskStatus.PAUSED)
            timerManager.pauseTimer()
        }
    }

    fun resumeTask() {
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, TaskStatus.IN_PROGRESS)
            timerManager.resumeTimer()
        }
    }

    suspend fun completeTask() {
        val currentTask = task.value ?: return
        val elapsedSeconds = timerManager.timerState.value.elapsedSeconds

        // Create execution record
        val now = System.currentTimeMillis()
        val execution = TaskExecution(
            taskId = taskId,
            startTime = now - (elapsedSeconds * 1000L),
            elapsedSeconds = elapsedSeconds,
            completedAt = now,
//            beforeNote = beforeNote,
//            afterNote = afterNote,
            executionDate = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )

        repository.insertTaskExecution(execution)
        repository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
        timerManager.stopTimer()
    }

    fun extendTimer(additionalMinutes: Int) {
        viewModelScope.launch {
            task.value?.let { currentTask ->
                val newDuration = currentTask.durationMinutes + additionalMinutes
                repository.updateTask(currentTask.copy(durationMinutes = newDuration))
            }
        }
    }
}

class TaskExecutionViewModelFactory(
    private val taskId: Long,
    private val repository: TaskRepository,
    private val timerManager: TimerManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskExecutionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskExecutionViewModel(taskId, repository, timerManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
