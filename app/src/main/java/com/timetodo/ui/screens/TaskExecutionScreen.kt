package com.timetodo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterCenterFocus
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
import com.timetodo.theme.StatusCompleted
import com.timetodo.theme.StatusInProgress
import com.timetodo.theme.StatusPaused
import com.timetodo.ui.components.TimerDisplay
import com.timetodo.ui.viewmodels.TaskExecutionViewModel
import com.timetodo.ui.viewmodels.TaskExecutionViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskExecutionScreen(
    taskId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToFocus: (Long) -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TaskRepository(database) }
    val timerManager = remember { TimerManager.getInstance(context) }

    val viewModel: TaskExecutionViewModel = viewModel(
        factory = TaskExecutionViewModelFactory(taskId, repository, timerManager)
    )

    val task by viewModel.task.collectAsState()
    val timerState by timerManager.timerState.collectAsState()
    val scope = rememberCoroutineScope()

//    var beforeNote by remember { mutableStateOf("") }
//    var afterNote by remember { mutableStateOf("") }
    var showExtendDialog by remember { mutableStateOf(false) }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(task?.title ?: "Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToFocus(taskId) }) {
                        Icon(Icons.Default.FilterCenterFocus, contentDescription = "Focus Mode")
                    }
                }
            )
        }
    ) { padding ->
        task?.let { currentTask ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Task description
                if (currentTask.description.isNotBlank()) {
                    Text(
                        text = currentTask.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Timer display
                TimerDisplay(
                    elapsedSeconds = timerState.elapsedSeconds,
                    totalSeconds = currentTask.durationMinutes * 60,
                    isLarge = true,
                    modifier = Modifier.weight(1f)
                )

                // Notes section
//                Column(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    OutlinedTextField(
//                        value = beforeNote,
//                        onValueChange = { beforeNote = it },
//                        label = { Text("Before") },
//                        modifier = Modifier.fillMaxWidth(),
//                        maxLines = 2
//                    )
//
//                    OutlinedTextField(
//                        value = afterNote,
//                        onValueChange = { afterNote = it },
//                        label = { Text("After") },
//                        modifier = Modifier.fillMaxWidth(),
//                        maxLines = 2
//                    )
//                }

                Spacer(modifier = Modifier.height(24.dp))

                // Control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (currentTask.status) {
                        TaskStatus.PENDING -> {
                            Button(
                                onClick = { viewModel.startTask() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StatusInProgress
                                )
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start")
                            }
                        }
                        TaskStatus.IN_PROGRESS -> {
                            Button(
                                onClick = { viewModel.pauseTask() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StatusPaused
                                )
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pause")
                            }
                        }
                        TaskStatus.PAUSED -> {
                            Button(
                                onClick = { viewModel.resumeTask() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StatusInProgress
                                )
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Resume")
                            }
                        }
                        else -> {}
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.completeTask()
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusCompleted
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Complete")
                    }
                }

                // Check if timer finished
                if (timerState.elapsedSeconds >= currentTask.durationMinutes * 60 &&
                    currentTask.status == TaskStatus.IN_PROGRESS) {
                    LaunchedEffect(Unit) {
                        showExtendDialog = true
                    }
                }
            }
        }
    }

    // Extend timer dialog
    if (showExtendDialog) {
        AlertDialog(
            onDismissRequest = { showExtendDialog = false },
            title = { Text("Timer Finished") },
            text = { Text("The timer has completed. Would you like to extend it or mark the task as complete?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.completeTask()
                            showExtendDialog = false
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Complete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.extendTimer(15)
                        showExtendDialog = false
                    }
                ) {
                    Text("Extend 15 min")
                }
            }
        )
    }
}
