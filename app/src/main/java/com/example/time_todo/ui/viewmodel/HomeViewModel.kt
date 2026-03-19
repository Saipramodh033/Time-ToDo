package com.example.time_todo.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.time_todo.data.local.AppDatabase
import com.example.time_todo.data.local.TaskEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).taskDao()

    val tasks = dao.observeTasks()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun addSampleTaskIfEmpty() {
        viewModelScope.launch {
            if (tasks.value.isEmpty()) {
                dao.insert(
                    TaskEntity(
                        title = "Sample Focus Task",
                        plannedMinutes = 25,
                        status = "PENDING"
                    )
                )
            }
        }
    }
}
