package com.timetodo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.timetodo.data.TaskRepository
import com.timetodo.data.entity.Group
import com.timetodo.data.entity.Task
import com.timetodo.domain.RecurrenceCalculator
import com.timetodo.domain.TimerManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class DayViewModel(
    private val date: LocalDate,
    private val repository: TaskRepository,
    private val timerManager: TimerManager
) : ViewModel() {

    val tasksForDate: StateFlow<List<Task>> = combine(
        repository.getTasksForDate(date),
        repository.getExecutionsForDate(date)
    ) { tasks, executions ->
        val skippedTaskIds = executions.filter { it.isSkipped }.map { it.taskId }.toSet()
        tasks.filter { task ->
            task.id !in skippedTaskIds && RecurrenceCalculator.shouldShowOnDate(task, date)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<Group>> = repository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteTaskPermanently(task: Task) {
        viewModelScope.launch {
            // Stop timer if this task is currently active
            val timerState = timerManager.timerState.value
            if (timerState.taskId == task.id) {
                timerManager.stopTimer()
            }
            repository.deleteTask(task)
        }
    }

    fun deleteTaskForToday(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTaskForToday(taskId)
        }
    }

    fun resetTask(taskId: Long) {
        viewModelScope.launch {
            repository.resetTask(taskId)
        }
    }
}

class DayViewModelFactory(
    private val date: LocalDate,
    private val repository: TaskRepository,
    private val timerManager: TimerManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DayViewModel(date, repository, timerManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
