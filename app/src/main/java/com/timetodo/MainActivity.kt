package com.timetodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.work.*
import com.timetodo.data.ThemeMode
import com.timetodo.data.ThemePreferences
import com.timetodo.navigation.TaskManagerNavigation
import com.timetodo.theme.TaskManagerTheme
import com.timetodo.worker.ReminderWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import java.util.Calendar
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, initialize reminders
            initializeReminders()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check and request notification permission
        checkNotificationPermission()
        
        // Initialize task reminders if enabled
        initializeReminders()
        
        scheduleDailyReminder()
        
        enableEdgeToEdge()
        setContent {
            val themePreferences = remember { ThemePreferences(applicationContext) }
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val scope = rememberCoroutineScope()
            
            var showPermissionDialog by remember { mutableStateOf(false) }
            
            // Check if we should show permission dialog
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    
                    if (!hasPermission) {
                        showPermissionDialog = true
                    }
                }
            }
            
            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            
            val onThemeToggle: () -> Unit = {
                scope.launch {
                    val newMode = when (themeMode) {
                        ThemeMode.LIGHT -> ThemeMode.DARK
                        ThemeMode.DARK -> ThemeMode.SYSTEM
                        ThemeMode.SYSTEM -> ThemeMode.LIGHT
                    }
                    themePreferences.setThemeMode(newMode)
                }
            }
            
            TaskManagerTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskManagerNavigation(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle
                    )
                }
            }
            
            // Notification Permission Dialog
            if (showPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { showPermissionDialog = false },
                    title = { Text("Enable Notifications") },
                    text = { 
                        Text("Time ToDo uses notifications to:\n\n" +
                             "• Remind you about pending tasks\n" +
                             "• Help you stay focused on active tasks\n" +
                             "• Keep you productive throughout the day\n\n" +
                             "Would you like to enable notifications?")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showPermissionDialog = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        ) {
                            Text("Enable")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showPermissionDialog = false }
                        ) {
                            Text("Not Now")
                        }
                    }
                )
            }
        }
    }
    
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            // Dialog will be shown in Compose UI if permission not granted
        }
    }
    
    private fun initializeReminders() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            val prefs = com.timetodo.data.NotificationPreferences(applicationContext)
            val enabled = prefs.taskRemindersEnabled.first()
            if (enabled) {
                com.timetodo.notification.ReminderScheduler.scheduleAllReminders(applicationContext)
            }
        }
    }
    
    override fun onStop() {
        super.onStop()
        // Show focus reminder if user leaves app with active task (if enabled in settings)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            val prefs = com.timetodo.data.NotificationPreferences(applicationContext)
            val enabled = prefs.focusReminderEnabled.first()
            if (enabled) {
                com.timetodo.notification.FocusReminderHelper.showFocusReminder(this@MainActivity)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Cancel focus reminder when user returns to app
        com.timetodo.notification.FocusReminderHelper.cancelFocusReminder(this)
    }

    private fun scheduleDailyReminder() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun calculateInitialDelay(): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        // Schedule for 9:00 AM
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return calendar.timeInMillis - now
    }
}
