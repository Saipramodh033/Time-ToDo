package com.timetodo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.timetodo.data.TaskRepository
import com.timetodo.domain.RecurrenceCalculator
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    val recentHistory = repository.getRecentExecutions(2)

    fun getTaskCountForMonth(yearMonth: YearMonth): Flow<Map<LocalDate, Int>> {
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()
        
        return repository.getTasksBetweenDates(firstDay, lastDay)
            .map { tasks ->
                val countMap = mutableMapOf<LocalDate, Int>()
                
                for (day in 1..yearMonth.lengthOfMonth()) {
                    val date = yearMonth.atDay(day)
                    val count = tasks.count { task ->
                        RecurrenceCalculator.shouldShowOnDate(task, date)
                    }
                    if (count > 0) {
                        countMap[date] = count
                    }
                }
                
                countMap
            }
    }
}

class CalendarViewModelFactory(
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
