package com.timetodo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_preferences")

class NotificationPreferences(private val context: Context) {
    companion object {
        val ENABLE_TASK_REMINDERS = booleanPreferencesKey("enable_task_reminders")
        val ENABLE_FOCUS_REMINDER = booleanPreferencesKey("enable_focus_reminder")
    }
    
    val taskRemindersEnabled: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences ->
            preferences[ENABLE_TASK_REMINDERS] ?: true // Enabled by default
        }
    
    val focusReminderEnabled: Flow<Boolean> = context.notificationDataStore.data
        .map { preferences ->
            preferences[ENABLE_FOCUS_REMINDER] ?: true // Enabled by default
        }
    
    suspend fun setTaskRemindersEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { preferences ->
            preferences[ENABLE_TASK_REMINDERS] = enabled
        }
    }
    
    suspend fun setFocusReminderEnabled(enabled: Boolean) {
        context.notificationDataStore.edit { preferences ->
            preferences[ENABLE_FOCUS_REMINDER] = enabled
        }
    }
}
