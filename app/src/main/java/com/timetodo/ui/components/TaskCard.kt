package com.timetodo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.timetodo.data.entity.Task
import com.timetodo.data.entity.TaskStatus
import com.timetodo.theme.*

@Composable
fun TaskCard(
    task: Task,
    groupColor: Color?,
    elapsedSeconds: Int = 0,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val statusColor = when (task.status) {
        TaskStatus.PENDING -> StatusPending
        TaskStatus.IN_PROGRESS -> StatusInProgress
        TaskStatus.PAUSED -> StatusPaused
        TaskStatus.COMPLETED -> StatusCompleted
    }

    val statusIcon = when (task.status) {
        TaskStatus.PENDING -> Icons.Default.PlayArrow
        TaskStatus.IN_PROGRESS -> Icons.Default.Pause
        TaskStatus.PAUSED -> Icons.Default.PlayArrow
        TaskStatus.COMPLETED -> Icons.Default.CheckCircle
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group color indicator
                if (groupColor != null) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(groupColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                // Task content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Duration display
                        Text(
                            text = if (task.status == TaskStatus.IN_PROGRESS || task.status == TaskStatus.PAUSED) {
                                "${formatTime(elapsedSeconds)} / ${formatTime(task.durationMinutes * 60)}"
                            } else {
                                "${task.durationMinutes} min"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Status indicator
                        Surface(
                            color = statusColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = task.status.name.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Action Menu
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Actions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status icon
                Icon(
                    imageVector = statusIcon,
                    contentDescription = task.status.name,
                    tint = statusColor,
                    modifier = Modifier.size(32.dp)
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Reset Progress") },
                        onClick = {
                            showMenu = false
                            onReset()
                        },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            ) 
                        }
                    )
                }
            }
        }

        // Progress bar for active tasks
        if (task.status == TaskStatus.IN_PROGRESS || task.status == TaskStatus.PAUSED) {
            val progress = (elapsedSeconds.toFloat() / (task.durationMinutes * 60)).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = statusColor,
                trackColor = SurfaceVariantDark
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}
