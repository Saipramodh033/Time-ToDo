package com.timetodo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timetodo.data.AppDatabase
import com.timetodo.data.TaskRepository
import com.timetodo.data.entity.RecurrenceType
import com.timetodo.data.entity.Task
import com.timetodo.data.entity.TaskStatus
import com.timetodo.ui.components.GroupSelector
import com.timetodo.ui.viewmodels.TaskFormViewModel
import com.timetodo.ui.viewmodels.TaskFormViewModelFactory
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.Instant
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    taskId: Long?,
    preselectedDate: LocalDate?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TaskRepository(database) }
    
    val viewModel: TaskFormViewModel = viewModel(
        factory = TaskFormViewModelFactory(taskId, repository)
    )

    val task by viewModel.task.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableIntStateOf(30) }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var assignedDate by remember { mutableStateOf(preselectedDate) }
    var recurrenceType by remember { mutableStateOf(RecurrenceType.NONE) }
    var selectedDays by remember { mutableStateOf(setOf<DayOfWeek>()) }
    var endDateTime by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(task) {
        task?.let {
            title = it.title
            description = it.description
            durationMinutes = it.durationMinutes
            selectedGroupId = it.groupId
            assignedDate = it.assignedDate?.let { millis ->
                Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
            }
            recurrenceType = it.recurrenceType
            selectedDays = if (it.recurrenceDays.isNotEmpty()) {
                it.recurrenceDays.split(",")
                    .filter { s -> s.isNotEmpty() }
                    .map { s -> DayOfWeek.of(s.toInt()) }
                    .toSet()
            } else {
                setOf()
            }
            endDateTime = it.endDateTime
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null) "New Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Duration
            Column {
                Text("Duration: $durationMinutes minutes", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = durationMinutes.toFloat(),
                    onValueChange = { durationMinutes = it.toInt() },
                    valueRange = 5f..240f,
                    steps = 46
                )
            }

            // Group
            GroupSelector(
                groups = groups,
                selectedGroupId = selectedGroupId,
                onGroupSelected = { selectedGroupId = it }
            )

            // Date assignment
            OutlinedButton(
                onClick = { /* Date picker would go here */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(assignedDate?.toString() ?: "No date assigned")
            }

            // End Date & Time
            Column {
                Text("End Date & Time (Disables Recurrence)", style = MaterialTheme.typography.titleSmall)
                OutlinedButton(
                    onClick = { 
                        showDateTimePicker(context, endDateTime ?: System.currentTimeMillis()) {
                            endDateTime = it
                            recurrenceType = RecurrenceType.NONE // Auto-disable recurrence
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(endDateTime?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDateTime().toString() } ?: "Set End Time")
                }
                if (endDateTime != null) {
                    TextButton(onClick = { endDateTime = null }) {
                        Text("Clear End Date", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Recurrence
            Column {
                Text("Recurrence", style = MaterialTheme.typography.titleMedium)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = recurrenceType == RecurrenceType.NONE,
                        onClick = { recurrenceType = RecurrenceType.NONE },
                        label = { Text("None") }
                    )
                    FilterChip(
                        selected = recurrenceType == RecurrenceType.DAILY,
                        onClick = { 
                            if (endDateTime == null) {
                                recurrenceType = RecurrenceType.DAILY 
                            }
                        },
                        label = { Text("Daily") },
                        enabled = endDateTime == null
                    )
                    FilterChip(
                        selected = recurrenceType == RecurrenceType.WEEKLY,
                        onClick = { 
                            if (endDateTime == null) {
                                recurrenceType = RecurrenceType.WEEKLY 
                            }
                        },
                        label = { Text("Weekly") },
                        enabled = endDateTime == null
                    )
                }

                if (endDateTime != null) {
                    Text(
                        "Recurrence is disabled when an End Date is set.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (recurrenceType == RecurrenceType.WEEKLY) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Select days:", style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        DayOfWeek.entries.forEach { day ->
                            FilterChip(
                                selected = selectedDays.contains(day),
                                onClick = {
                                    selectedDays = if (selectedDays.contains(day)) {
                                        selectedDays - day
                                    } else {
                                        selectedDays + day
                                    }
                                },
                                label = { Text(day.name.take(3)) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        scope.launch {
                            val newTask = Task(
                                id = taskId ?: 0,
                                title = title,
                                description = description,
                                durationMinutes = durationMinutes,
                                groupId = selectedGroupId,
                                assignedDate = assignedDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
                                recurrenceType = recurrenceType,
                                recurrenceDays = if (recurrenceType == RecurrenceType.WEEKLY) {
                                    selectedDays.joinToString(",") { it.value.toString() }
                                } else "",
                                status = task?.status ?: TaskStatus.PENDING,
                                endDateTime = endDateTime
                            )
                            
                            if (taskId == null) {
                                repository.insertTask(newTask)
                            } else {
                                repository.updateTask(newTask)
                            }
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text(if (taskId == null) "Create Task" else "Update Task")
            }
        }
    }
}

private fun showDateTimePicker(
    context: android.content.Context,
    initialMillis: Long,
    onDateTimeSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = initialMillis

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    onDateTimeSelected(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
