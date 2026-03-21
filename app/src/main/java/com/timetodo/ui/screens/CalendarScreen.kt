package com.timetodo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timetodo.data.AppDatabase
import com.timetodo.data.TaskRepository
import com.timetodo.ui.components.CalendarGrid
import com.timetodo.ui.viewmodels.CalendarViewModel
import com.timetodo.ui.viewmodels.CalendarViewModelFactory
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToDay: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TaskRepository(database) }
    
    val viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModelFactory(repository)
    )

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val taskCountByDate by viewModel.getTaskCountForMonth(currentMonth).collectAsState(initial = emptyMap())
    val recentHistory by viewModel.recentHistory.collectAsState(initial = emptyList())
    val today = LocalDate.now()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                        }
                        
                        Text(
                            "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}"
                        )
                        
                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            CalendarGrid(
                yearMonth = currentMonth,
                selectedDate = null,
                today = today,
                taskCountByDate = taskCountByDate,
                onDateClick = onNavigateToDay
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Recent History (Past 2 Days)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (recentHistory.isEmpty()) {
                Text(
                    "No task history available for the last 2 days.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(recentHistory) { execution ->
                        HistoryItem(execution)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(execution: com.timetodo.data.dao.TaskExecutionWithDetails) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(execution.taskTitle, style = MaterialTheme.typography.titleSmall)
                execution.completedAt?.let { timestamp ->
                    val localDateTime = java.time.Instant.ofEpochMilli(timestamp)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime()
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, hh:mm a")
                    Text(
                        "Completed: ${localDateTime.format(formatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                "${execution.elapsedSeconds / 60}m ${execution.elapsedSeconds % 60}s",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
