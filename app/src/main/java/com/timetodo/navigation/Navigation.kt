package com.timetodo.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.timetodo.ui.screens.*
import java.time.LocalDate

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    object Today : Screen("today", "Today", Icons.Default.Home)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.DateRange)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object TaskForm : Screen("task_form?taskId={taskId}&date={date}", "Task Form")
    object TaskExecution : Screen("task_execution/{taskId}", "Task Execution")
    object Day : Screen("day/{date}", "Day")
    object GroupManagement : Screen("group_management", "Groups")
}

val bottomNavItems = listOf(
    Screen.Today,
    Screen.Calendar,
    Screen.Analytics,
    Screen.Settings
)

@Composable
fun TaskManagerNavigation(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            // Show bottom bar only on main screens
            val showBottomBar = bottomNavItems.any { screen ->
                currentDestination?.hierarchy?.any { it.route == screen.route } == true
            }

            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Today.route) {
                TodayScreen(
                    onNavigateToTaskForm = { taskId, date ->
                        navController.navigate("task_form?taskId=${taskId ?: ""}&date=${date ?: ""}")
                    },
                    onNavigateToExecution = { taskId ->
                        navController.navigate("task_execution/$taskId")
                    },
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen(
                    onNavigateToDay = { date ->
                        navController.navigate("day/$date")
                    }
                )
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToGroups = {
                        navController.navigate(Screen.GroupManagement.route)
                    }
                )
            }

            composable(
                route = "task_form?taskId={taskId}&date={date}",
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument("date") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val taskIdStr = backStackEntry.arguments?.getString("taskId")
                val taskId = taskIdStr?.toLongOrNull()
                val dateStr = backStackEntry.arguments?.getString("date")
                val date = dateStr?.let { if (it.isNotEmpty()) LocalDate.parse(it) else null }

                TaskFormScreen(
                    taskId = taskId,
                    preselectedDate = date,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "task_execution/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.LongType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getLong("taskId") ?: return@composable
                TaskExecutionScreen(
                    taskId = taskId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToFocus = { id -> navController.navigate("focus_mode/$id") }
                )
            }

            composable(
                route = "day/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val dateStr = backStackEntry.arguments?.getString("date") ?: return@composable
                val date = LocalDate.parse(dateStr)
                DayScreen(
                    date = date,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTaskForm = { taskId ->
                        navController.navigate("task_form?taskId=${taskId ?: ""}&date=$date")
                    },
                    onNavigateToExecution = { taskId ->
                        navController.navigate("task_execution/$taskId")
                    }
                )
            }

            composable(Screen.GroupManagement.route) {
                GroupManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "focus_mode/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.LongType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getLong("taskId") ?: return@composable
                FocusModeScreen(
                    taskId = taskId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
