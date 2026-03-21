package com.timetodo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.timetodo.data.entity.Group
import com.timetodo.theme.GroupColors
import com.timetodo.theme.SurfaceDark

@Composable
fun GroupSelector(
    groups: List<Group>,
    selectedGroupId: Long?,
    onGroupSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedGroup = groups.find { it.id == selectedGroupId }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedGroup != null) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(getGroupColor(selectedGroup.color))
                        )
                        Text(
                            text = selectedGroup.name,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = "No Group",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select group",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            // No group option
            DropdownMenuItem(
                text = {
                    Text(
                        text = "No Group",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = {
                    onGroupSelected(null)
                    expanded = false
                }
            )

            HorizontalDivider()

            // Group options
            groups.forEach { group ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(getGroupColor(group.color))
                            )
                            Text(
                                text = group.name,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = {
                        onGroupSelected(group.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun getGroupColor(colorIndex: Int): Color {
    return GroupColors.getOrElse(colorIndex % GroupColors.size) { GroupColors[0] }
}
