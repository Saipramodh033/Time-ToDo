package com.timetodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timetodo.data.AppDatabase
import com.timetodo.data.TaskRepository
import com.timetodo.data.entity.TaskStatus
import com.timetodo.domain.TimerManager
import com.timetodo.theme.BackgroundDark
import com.timetodo.theme.StatusInProgress
import com.timetodo.theme.StatusPaused
import com.timetodo.theme.SurfaceDark
import com.timetodo.ui.components.TimerDisplay
import com.timetodo.ui.viewmodels.TaskExecutionViewModel
import com.timetodo.ui.viewmodels.TaskExecutionViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(
    taskId: Long,
    onNavigateBack: () -> Unit
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundDark, SurfaceDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Exit Focus Mode",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Task Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = task?.title ?: "Focusing...",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (task?.description?.isNotBlank() == true) {
                    Text(
                        text = task!!.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Central Timer
            TimerDisplay(
                elapsedSeconds = timerState.elapsedSeconds,
                totalSeconds = (task?.durationMinutes ?: 0) * 60,
                isLarge = true,
                modifier = Modifier.weight(1f)
            )

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (task?.status) {
                    TaskStatus.IN_PROGRESS -> {
                        LargeIconButton(
                            icon = Icons.Default.Pause,
                            color = StatusPaused,
                            onClick = { viewModel.pauseTask() }
                        )
                    }
                    TaskStatus.PAUSED -> {
                        LargeIconButton(
                            icon = Icons.Default.PlayArrow,
                            color = StatusInProgress,
                            onClick = { viewModel.resumeTask() }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun LargeIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = color
        )
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
    }
}
