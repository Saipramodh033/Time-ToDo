package com.example.time_todo.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.time_todo.services.TimerService
import com.example.time_todo.ui.TimerState

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val elapsed by TimerState.elapsedSeconds.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Elapsed: ${elapsed}s",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(32.dp))

        Row {
            Button(onClick = {
                context.startForegroundService(
                    Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_START
                    }
                )

            }) {
                Text("Start")
            }

            Spacer(Modifier.width(16.dp))

            Button(onClick = {
                context.startService(
                    Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_PAUSE
                    }
                )
            }) {
                Text("Pause")
            }

            Spacer(Modifier.width(16.dp))

            Button(onClick = {
                context.startService(
                    Intent(context, TimerService::class.java).apply {
                        action = TimerService.ACTION_RESUME
                    }
                )
            }) {
                Text("Resume")
            }
        }
    }
}
