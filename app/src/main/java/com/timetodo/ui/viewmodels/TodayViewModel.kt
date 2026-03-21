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

class TodayViewModel(
    private val repository: TaskRepository,
    private val timerManager: TimerManager
) : ViewModel() {

    val groupedTasks: StateFlow<Map<Group?, List<Task>>> = combine(
        repository.getTasksForDate(LocalDate.now()),
        repository.getAllGroups(),
        repository.getExecutionsForDate(LocalDate.now())
    ) { tasks, groups, executions ->
        val skippedTaskIds = executions.filter { it.isSkipped }.map { it.taskId }.toSet()
        
        val filteredTasks = tasks.filter { task ->
            task.id !in skippedTaskIds && RecurrenceCalculator.shouldShowOnDate(task, LocalDate.now())
        }

        filteredTasks.groupBy { task ->
            groups.find { it.id == task.groupId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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

class TodayViewModelFactory(
    private val repository: TaskRepository,
    private val timerManager: TimerManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodayViewModel(repository, timerManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
