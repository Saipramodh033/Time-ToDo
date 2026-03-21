package com.timetodo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.timetodo.data.AppDatabase
import com.timetodo.data.TaskRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToGroups: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TaskRepository(database) }
    val scope = rememberCoroutineScope()

    var autoCompleteOnTimerFinish by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Timer settings
            Text("Timer", style = MaterialTheme.typography.titleLarge)
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-complete on timer finish")
                            Text(
                                "Automatically mark task as complete when timer ends",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoCompleteOnTimerFinish,
                            onCheckedChange = { autoCompleteOnTimerFinish = it }
                        )
                    }
                }
            }

            // Notification settings
            Text("Notifications", style = MaterialTheme.typography.titleLarge)
            
            val notificationPrefs = remember { com.timetodo.data.NotificationPreferences(context) }
            val taskRemindersEnabled by notificationPrefs.taskRemindersEnabled.collectAsState(initial = true)
            val focusReminderEnabled by notificationPrefs.focusReminderEnabled.collectAsState(initial = true)
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Task Reminders Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Task Reminders")
                            Text(
                                "Get reminded every 3 hours (7 AM - 10 PM)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = taskRemindersEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    notificationPrefs.setTaskRemindersEnabled(enabled)
                                    if (enabled) {
                                        com.timetodo.notification.ReminderScheduler.scheduleAllReminders(context)
                                    } else {
                                        com.timetodo.notification.ReminderScheduler.cancelAllReminders(context)
                                    }
                                }
                            }
                        )
                    }
                    
                    HorizontalDivider()
                    
                    // Focus Reminder Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Focus Reminder")
                            Text(
                                "Get reminded when you leave an active task",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = focusReminderEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    notificationPrefs.setFocusReminderEnabled(enabled)
                                }
                            }
                        )
                    }
                }
            }

            // Group management
            Text("Organization", style = MaterialTheme.typography.titleLarge)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToGroups
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Group, contentDescription = null)
                        Text("Manage Groups")
                    }
                }
            }

            // Data management
            Text("Data", style = MaterialTheme.typography.titleLarge)
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { showClearDataDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear All Data")
                    }
                }
            }

            // App info
            Text("About", style = MaterialTheme.typography.titleLarge)
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Task Manager", style = MaterialTheme.typography.titleMedium)
                    Text("Version 1.0", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "A time-boxed task execution app focused on doing, not planning.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Clear data confirmation dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will permanently delete all tasks, groups, and execution history. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.clearAllData()
                            showClearDataDialog = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
