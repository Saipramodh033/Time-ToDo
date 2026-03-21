package com.timetodo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.timetodo.theme.*
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    today: LocalDate,
    taskCountByDate: Map<LocalDate, Int>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday
    val daysInMonth = yearMonth.lengthOfMonth()

    Column(modifier = modifier) {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Empty cells before first day
            items(firstDayOfWeek) {
                Box(modifier = Modifier.aspectRatio(1f))
            }

            // Days of month
            items(daysInMonth) { dayIndex ->
                val day = dayIndex + 1
                val date = yearMonth.atDay(day)
                val isToday = date == today
                val isSelected = date == selectedDate
                val taskCount = taskCountByDate[date] ?: 0

                DayCell(
                    day = day,
                    isToday = isToday,
                    isSelected = isSelected,
                    taskCount = taskCount,
                    onClick = { onDateClick(date) }
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    taskCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> PrimaryDark.copy(alpha = 0.3f)
                    isToday -> StatusInProgress.copy(alpha = 0.2f)
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isToday -> StatusInProgress
                    isSelected -> PrimaryDark
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            if (taskCount > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(StatusInProgress)
                )
            }
        }
    }
}
