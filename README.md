# TimeToDo - Turning Intentions into Actions

TimeToDo is a modern, offline-first Android application designed with a single goal: **to help you move from intention to execution instead of letting ideas sit idle.**

Unlike many complex task managers, TimeToDo doesn't want you to spend your day planning. It focuses on **time-boxed execution**—setting a constraint and sticking to it. If you have 45 minutes for a task, you focus on those 45 minutes and nothing else.

## ✨ Features

- **🎯 Constraint-Driven Focus**: Set a strict time limit for your tasks to stay within the flow.
- **⏱️ Real-Time Execution**: Track progress with a dedicated foreground service that keeps you accountable.
- **⏸️ Pause & Resume**: Handle unavoidable interruptions without losing track of your actual work time.
- **📱 Persistent Tracking**: A persistent notification ensures you are always aware of your current focus.
- **📅 Calendar View**: Browse and manage tasks day-by-day with a full monthly calendar.
- **📊 Analytics Engine**: Track productivity trends, completion rates, and time spent per task group.
- **🔁 Recurring Tasks**: Schedule tasks that repeat on a daily, weekly, or custom cadence.
- **🗂️ Group Management**: Organize tasks into custom groups (Coding, Assignments, etc.) for clarity.
- **🔔 Smart Notifications**: Focus reminders, daily nudges, and scheduled alarm notifications.
- **🌙 Light / Dark / System Theme**: Switch between themes from Settings — your eyes, your choice.
- **📡 100% Offline**: Your data stays on your device, private and always accessible.

## 🛠️ Built With

- **Kotlin**: Modern programming language for Android.
- **Jetpack Compose**: Declarative UI toolkit for building native interfaces.
- **Material 3**: The latest evolution of Material Design.
- **Room Database**: Robust local data persistence.
- **Coroutines & Flow**: For seamless asynchronous operations.
- **WorkManager**: Reliable background scheduling for reminders.
- **Foreground Services**: Ensuring accurate time tracking in the background.

## 🏗️ Architecture

TimeToDo is built with a decoupled, modern Android architecture (MVVM) to ensure reliability even when you're not looking at the app.

- **Presentation Layer**: Built 100% in **Jetpack Compose** using **MVVM**. `StateFlow` drives real-time UI updates.
- **Task Engine (`TimerService`)**: A **Foreground Service** handles time-tracking logic, immune to system kills.
- **Persistence Layer**: **Room Database** with DAOs for Tasks, Groups, and Execution sessions.
- **Analytics**: `AnalyticsEngine` aggregates execution history for productivity reporting.
- **Recurrence**: `RecurrenceCalculator` computes future occurrences for repeating tasks.
- **Notifications**: `ReminderScheduler` + `WorkManager` power daily nudges and focus reminders.

### 📁 Project Structure

```text
app/src/main/java/com/timetodo/
├── data/
│   ├── dao/           # Room DAOs (TaskDao, GroupDao, TaskExecutionDao)
│   ├── entity/        # Room entities (Task, Group, TaskExecution)
│   ├── AppDatabase.kt
│   ├── TaskRepository.kt
│   ├── NotificationPreferences.kt
│   └── ThemePreferences.kt
├── domain/
│   ├── AnalyticsEngine.kt        # Productivity analytics logic
│   ├── RecurrenceCalculator.kt   # Recurring task scheduling
│   └── TimerManager.kt           # Central timer state manager
├── navigation/
│   └── Navigation.kt             # Full nav graph (8 routes)
├── notification/
│   ├── FocusReminderHelper.kt
│   ├── NotificationChannels.kt
│   ├── ReminderReceiver.kt
│   └── ReminderScheduler.kt
├── service/
│   ├── AlarmReceiver.kt
│   ├── TimerService.kt           # Foreground timer service
│   └── TimerWorker.kt
├── theme/                        # Material 3 colors, typography
├── ui/
│   ├── components/               # Reusable UI (TaskCard, TimerDisplay, CalendarGrid, etc.)
│   ├── screens/                  # 10 screens: Today, Calendar, Day, Analytics, Settings,
│   │                             #   TaskForm, TaskExecution, FocusMode, GroupManagement, BrandedHeader
│   └── viewmodels/               # 7 ViewModels (one per major screen)
├── util/
│   └── NotificationHelper.kt
├── worker/
│   └── ReminderWorker.kt         # WorkManager daily reminder
├── MainActivity.kt
└── TaskManagerApplication.kt
```

## 📖 How to Use

1. **Intention**: Tap `+` to create a task. Give it a title, group, and **Duration Constraint**.
2. **Commitment**: Hit **Start** on your task card — the app shifts into execution mode.
3. **Execution**: A persistent notification keeps you anchored. Leave the app and focus on your work.
4. **Completion**: Tap **Complete** when done. Your actual time is recorded for analytics.
5. **Review**: Head to **Analytics** to see your completion rates and focus patterns over time.

## 📸 Screenshots

| Home Screen | New Task | Timer |
|:---:|:---:|:---:|
| ![Home Screen](screenshots/home.jpg) | ![New Task](screenshots/new_task.jpg) | ![Timer](screenshots/timer.jpg) |

| Calendar | Analytics |
|:---:|:---:|
| ![Calendar](screenshots/calendar.jpg) | ![Analytics](screenshots/analytics.jpg) |

## 🚀 Getting Started

1. **Clone the repo**
   ```sh
   git clone https://github.com/Saipramodh033/Time-ToDo.git
   ```
2. **Open in Android Studio** (Ladybug or later recommended).
3. **Build & Run** — connect your device or emulator and hit Run.

