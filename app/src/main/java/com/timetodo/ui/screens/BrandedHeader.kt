package com.timetodo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timetodo.theme.GradientEnd
import com.timetodo.theme.GradientStart
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun BrandedHeader(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val dateFormatted = today.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Title
            Text(
                text = "Time To Do",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Date Info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayOfWeek,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = " • ",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = dateFormatted,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        // Theme Toggle Button
        IconButton(
            onClick = onThemeToggle,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle theme",
                tint = Color.White
            )
        }
    }
}
