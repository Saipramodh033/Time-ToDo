package com.timetodo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timetodo.theme.StatusInProgress
import com.timetodo.theme.SurfaceVariantDark


@Composable
fun TimerDisplay(
    elapsedSeconds: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
    isLarge: Boolean = true
) {
    val progress = if (totalSeconds > 0) {
        (elapsedSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circular progress indicator
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(if (isLarge) 280.dp else 180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = if (isLarge) 16.dp.toPx() else 12.dp.toPx()
                val diameter = size.minDimension - strokeWidth

                // Background circle
                drawArc(
                    color = SurfaceVariantDark,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Progress arc
                drawArc(
                    color = StatusInProgress,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Time text
            Text(
                text = formatTime(elapsedSeconds),
                fontSize = if (isLarge) 56.sp else 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (isLarge) {
            Spacer(modifier = Modifier.height(16.dp))

            // Total time
            Text(
                text = "of ${formatTime(totalSeconds)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}
