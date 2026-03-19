package com.example.time_todo.ui

import kotlinx.coroutines.flow.MutableStateFlow

object TimerState {
    val elapsedSeconds = MutableStateFlow(0)
}
