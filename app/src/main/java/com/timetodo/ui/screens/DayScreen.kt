package com.timetodo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timetodo.data.AppDatabase
import com.timetodo.data.TaskRepository
import com.timetodo.data.entity.TaskStatus
import com.timetodo.domain.TimerManager
import com.timetodo.theme.GroupColors
import com.timetodo.ui.components.TaskCard
import com.timetodo.ui.viewmodels.DayViewModel
import com.timetodo.ui.viewmodels.DayViewModelFactory
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(
    date: LocalDate,
    onNavigateBack: () -> Unit,
    onNavigateToTaskForm: (Long?) -> Unit,
    onNavigateToExecution: (Long) -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TaskRepository(database) }
    val timerManager = remember { TimerManager.getInstance(context) }
    
    val viewModel: DayViewModel = viewModel(
        factory = DayViewModelFactory(date, repository, timerManager)
    )

    val tasks by viewModel.tasksForDate.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val timerState by timerManager.timerState.collectAsState()

    // Action states
    var taskToDelete by remember { mutableStateOf<com.timetodo.data.entity.Task?>(null) }
    var taskToReset by remember { mutableStateOf<com.timetodo.data.entity.Task?>(null) }

    val plannedMinutes = tasks.sumOf { it.durationMinutes }
    val completedMinutes = tasks.filter { it.status == TaskStatus.COMPLETED }.sumOf { it.durationMinutes }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(date.toString())
                        Text(
                            text = "$completedMinutes / $plannedMinutes min",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToTaskForm(null) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks for this day.\nTap + to create one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    val group = groups.find { it.id == task.groupId }
                    val groupColor = group?.let { 
                        GroupColors.getOrElse(it.color % GroupColors.size) { GroupColors[0] }
                    }
                    
                    val elapsedSeconds = if (timerState.taskId == task.id) {
                        timerState.elapsedSeconds
                    } else {
                        0
                    }

                    TaskCard(
                        task = task,
                        groupColor = groupColor,
                        elapsedSeconds = elapsedSeconds,
                        onClick = {
                            when (task.status) {
                                TaskStatus.PENDING -> onNavigateToExecution(task.id)
                                TaskStatus.IN_PROGRESS -> onNavigateToExecution(task.id)
                                TaskStatus.PAUSED -> onNavigateToExecution(task.id)
                                TaskStatus.COMPLETED -> onNavigateToTaskForm(task.id)
                            }
                        },
                        onEdit = { onNavigateToTaskForm(task.id) },
                        onDelete = { taskToDelete = task },
                        onReset = { taskToReset = task }
                    )
                }
            }
        }

        // Reset Confirmation Dialog
        if (taskToReset != null) {
            AlertDialog(
                onDismissRequest = { taskToReset = null },
                title = { Text("Reset Task Progress") },
                text = { Text("Are you sure you want to reset today's progress for \"${taskToReset?.title}\"? This will delete today's timer data and set the task back to Pending.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            taskToReset?.let { viewModel.resetTask(it.id) }
                            taskToReset = null
                        }
                    ) {
                        Text("Reset", color = MaterialTheme.colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { taskToReset = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (taskToDelete != null) {
            AlertDialog(
                onDismissRequest = { taskToDelete = null },
                title = { Text("Delete Task") },
                text = { Text("Choose how you want to delete \"${taskToDelete?.title}\":") },
                confirmButton = {
                    Column(horizontalAlignment = Alignment.End) {
                        TextButton(
                            onClick = {
                                taskToDelete?.let { viewModel.deleteTaskForToday(it.id) }
                                taskToDelete = null
                            }
                        ) {
                            Text("Delete for Today Only")
                        }
                        TextButton(
                            onClick = {
                                taskToDelete?.let { viewModel.deleteTaskPermanently(it) }
                                taskToDelete = null
                            }
                        ) {
                            Text("Delete Permanently", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { taskToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
