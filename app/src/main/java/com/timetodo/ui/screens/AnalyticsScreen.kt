package com.timetodo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timetodo.data.AppDatabase
import com.timetodo.data.TaskRepository
import com.timetodo.domain.AnalyticsEngine
import com.timetodo.theme.GroupColors
import com.timetodo.ui.viewmodels.AnalyticsViewModel
import com.timetodo.ui.viewmodels.AnalyticsViewModelFactory
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { TaskRepository(database) }
    val analyticsEngine = remember { AnalyticsEngine(repository) }
    
    val viewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModelFactory(analyticsEngine)
    )

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Day", "Week", "Groups")

    val dayStats by viewModel.getDayStats(LocalDate.now()).collectAsState(initial = null)
    val weekStats by viewModel.getWeekStats(LocalDate.now()).collectAsState(initial = null)
    val groupStats by viewModel.getGroupStats().collectAsState(initial = emptyList())
    val groups by viewModel.groups.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Day stats
                        dayStats?.let { stats ->
                            StatCard(
                                title = "Today",
                                plannedMinutes = stats.plannedMinutes,
                                actualMinutes = stats.actualMinutes,
                                tasksCompleted = stats.completedTasks,
                                totalTasks = stats.totalTasks
                            )
                        } ?: Text("No data for today")
                    }
                    1 -> {
                        // Week stats
                        weekStats?.let { stats ->
                            StatCard(
                                title = "This Week",
                                plannedMinutes = stats.plannedMinutes,
                                actualMinutes = stats.actualMinutes,
                                tasksCompleted = stats.completedTasks,
                                totalTasks = stats.totalTasks
                            )
                        } ?: Text("No data for this week")
                    }
                    2 -> {
                        // Group stats
                        if (groupStats.isEmpty()) {
                            Text("No group data available")
                        } else {
                            for (stat in groupStats) {
                                val group = groups.find { it.id == stat.groupId }
                                GroupStatCard(
                                    groupName = group?.name ?: "Unknown",
                                    groupColor = group?.let { 
                                        GroupColors.getOrElse(it.color % GroupColors.size) { GroupColors[0] }
                                    },
                                    totalMinutes = stat.totalMinutes,
                                    tasksCompleted = stat.completedTasks
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    plannedMinutes: Int,
    actualMinutes: Int,
    tasksCompleted: Int,
    totalTasks: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            
            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Planned Time:")
                Text("${plannedMinutes / 60}h ${plannedMinutes % 60}m")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Actual Time:")
                Text("${actualMinutes / 60}h ${actualMinutes % 60}m")
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tasks Completed:")
                Text("$tasksCompleted / $totalTasks")
            }
            
            if (plannedMinutes > 0) {
                val percentage = (actualMinutes.toFloat() / plannedMinutes * 100).toInt()
                val progressValue = (actualMinutes.toFloat() / plannedMinutes).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progressValue },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${percentage}% of planned time", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun GroupStatCard(
    groupName: String,
    groupColor: androidx.compose.ui.graphics.Color?,
    totalMinutes: Int,
    tasksCompleted: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(groupName, style = MaterialTheme.typography.titleMedium)
                Text("$tasksCompleted tasks completed", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "${totalMinutes / 60}h ${totalMinutes % 60}m",
                style = MaterialTheme.typography.titleMedium,
                color = groupColor ?: MaterialTheme.colorScheme.primary
            )
        }
    }
}
