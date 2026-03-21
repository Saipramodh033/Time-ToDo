package com.timetodo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.timetodo.data.TaskRepository
import com.timetodo.data.entity.Group
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GroupManagementViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    val groups: StateFlow<List<Group>> = repository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createGroup(name: String, colorIndex: Int) {
        viewModelScope.launch {
            val group = Group(
                name = name,
                color = colorIndex,
                icon = "work"
            )
            repository.insertGroup(group)
        }
    }

    fun updateGroup(group: Group) {
        viewModelScope.launch {
            repository.updateGroup(group)
        }
    }

    fun deleteGroup(groupId: Long) {
        viewModelScope.launch {
            repository.deleteGroup(groupId)
        }
    }
}

class GroupManagementViewModelFactory(
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupManagementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
