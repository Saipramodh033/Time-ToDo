package com.timetodo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.timetodo.data.TaskRepository
import com.timetodo.data.entity.Group
import com.timetodo.data.entity.Task
import kotlinx.coroutines.flow.*

class TaskFormViewModel(
    private val taskId: Long?,
    private val repository: TaskRepository
) : ViewModel() {

    val task: StateFlow<Task?> = if (taskId != null) {
        repository.getTaskById(taskId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    } else {
        MutableStateFlow(null)
    }

    val groups: StateFlow<List<Group>> = repository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class TaskFormViewModelFactory(
    private val taskId: Long?,
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskFormViewModel(taskId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
