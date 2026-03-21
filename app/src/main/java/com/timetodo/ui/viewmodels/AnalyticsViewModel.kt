package com.timetodo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.timetodo.data.TaskRepository
import com.timetodo.data.entity.Group
import com.timetodo.domain.AnalyticsEngine
import kotlinx.coroutines.flow.*
import java.time.LocalDate

class AnalyticsViewModel(
    private val analyticsEngine: AnalyticsEngine
) : ViewModel() {

    val groups: StateFlow<List<Group>> = analyticsEngine.repository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getDayStats(date: LocalDate) = analyticsEngine.getDayStats(date)
    
    fun getWeekStats(date: LocalDate) = analyticsEngine.getWeekStats(date)
    
    fun getGroupStats() = analyticsEngine.getGroupStats()
}

class AnalyticsViewModelFactory(
    private val analyticsEngine: AnalyticsEngine
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(analyticsEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
