package com.timetodo.ui.screens

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timetodo.data.AppDatabase
import com.timetodo.data.TaskRepository
import com.timetodo.data.ThemePreferences
import com.timetodo.data.ThemeMode
import com.timetodo.data.entity.TaskStatus
import com.timetodo.domain.TimerManager
import com.timetodo.theme.GroupColors
import com.timetodo.theme.GradientStart
import com.timetodo.theme.GradientEnd
import com.timetodo.ui.components.TaskCard
import com.timetodo.ui.viewmodels.TodayViewModel
import com.timetodo.ui.viewmodels.TodayViewModelFactory
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onNavigateToTaskForm: (Long?, String?) -> Unit,
    onNavigateToExecution: (Long) -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TaskRepository(database) }
    val timerManager = remember { TimerManager.getInstance(context) }

    val viewModel: TodayViewModel = viewModel(
        factory = TodayViewModelFactory(repository, timerManager)
    )

    val groupedTasks by viewModel.groupedTasks.collectAsState()
    val timerState by timerManager.timerState.collectAsState()

    // Action states
    var taskToDelete by remember { mutableStateOf<com.timetodo.data.entity.Task?>(null) }
    var taskToReset by remember { mutableStateOf<com.timetodo.data.entity.Task?>(null) }

    // State to track which groups are expanded. Initialize all as expanded.
    val expandedGroups = remember { mutableStateMapOf<Long?, Boolean>() }

    // Update expandedGroups when groupedTasks changes
    LaunchedEffect(groupedTasks) {
        groupedTasks.keys.forEach { group ->
            if (expandedGroups[group?.id] == null) {
                expandedGroups[group?.id] = true
            }
        }
    }

    Scaffold(
        topBar = {
            BrandedHeader(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToTaskForm(null, LocalDate.now().toString()) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        }
    ) { padding ->
        if (groupedTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tasks for today.\nTap + to create one.",
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
                groupedTasks.forEach { (group, tasks) ->
                    val isExpanded = expandedGroups[group?.id] ?: true

                    item(key = "group_${group?.id ?: "none"}") {
                        GroupHeader(
                            groupName = group?.name ?: "No Group",
                            color = group?.let {
                                GroupColors.getOrElse(it.color % GroupColors.size) { GroupColors[0] }
                            } ?: MaterialTheme.colorScheme.outline,
                            isExpanded = isExpanded,
                            onToggle = { expandedGroups[group?.id] = !isExpanded }
                        )
                    }

                    if (isExpanded) {
                        items(tasks, key = { it.id }) { task ->
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
                                        TaskStatus.COMPLETED -> onNavigateToTaskForm(task.id, null)
                                    }
                                },
                                onEdit = { onNavigateToTaskForm(task.id, null) },
                                onDelete = { taskToDelete = task },
                                onReset = { taskToReset = task }
                            )
                        }
                    }
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

@Composable
private fun GroupHeader(
    groupName: String,
    color: androidx.compose.ui.graphics.Color,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp, 24.dp)
                    .padding(end = 8.dp)
                    .background(color, MaterialTheme.shapes.extraSmall)
            )
            Text(
                text = groupName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
