package com.timetodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timetodo.data.AppDatabase
import com.timetodo.data.TaskRepository
import com.timetodo.data.entity.Group
import com.timetodo.theme.GroupColors
import com.timetodo.ui.viewmodels.GroupManagementViewModel
import com.timetodo.ui.viewmodels.GroupManagementViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupManagementScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TaskRepository(database) }
    
    val viewModel: GroupManagementViewModel = viewModel(
        factory = GroupManagementViewModelFactory(repository)
    )

    val groups by viewModel.groups.collectAsState()
    val scope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<Group?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Groups") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add group")
            }
        }
    ) { padding ->
        if (groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No groups yet.\nTap + to create one.",
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
                items(groups, key = { it.id }) { group ->
                    GroupCard(
                        group = group,
                        onEdit = { editingGroup = group },
                        onDelete = {
                            scope.launch {
                                viewModel.deleteGroup(group.id)
                            }
                        }
                    )
                }
            }
        }
    }

    // Create/Edit dialog
    if (showCreateDialog || editingGroup != null) {
        GroupDialog(
            group = editingGroup,
            onDismiss = {
                showCreateDialog = false
                editingGroup = null
            },
            onSave = { name, colorIndex ->
                scope.launch {
                    if (editingGroup != null) {
                        viewModel.updateGroup(editingGroup!!.copy(name = name, color = colorIndex))
                    } else {
                        viewModel.createGroup(name, colorIndex)
                    }
                    showCreateDialog = false
                    editingGroup = null
                }
            }
        )
    }
}

@Composable
private fun GroupCard(
    group: Group,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val groupColor = GroupColors.getOrElse(group.color % GroupColors.size) { GroupColors[0] }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEdit)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(groupColor)
                )
                Text(group.name, style = MaterialTheme.typography.titleMedium)
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun GroupDialog(
    group: Group?,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(group?.name ?: "") }
    var selectedColorIndex by remember { mutableIntStateOf(group?.color ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (group == null) "Create Group" else "Edit Group") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Color:", style = MaterialTheme.typography.bodyMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(GroupColors.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GroupColors[index])
                                .clickable { selectedColorIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColorIndex == index) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Selected",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, selectedColorIndex) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
