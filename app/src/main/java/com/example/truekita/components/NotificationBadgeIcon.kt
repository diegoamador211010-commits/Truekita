package com.example.truekita.components

import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NotificationBadgeIcon(
    pendingCount: Int,
    contentDescription: String = "Notificaciones"
) {
    BadgedBox(
        badge = {
            if (pendingCount > 0) {
                Badge(
                    containerColor = Color.Red,
                    contentColor = Color.White,
                    modifier = Modifier.offset(x = (-2).dp, y = 2.dp)
                ) {
                    Text(
                        text = if (pendingCount > 99) {
                            "99+"
                        } else {
                            pendingCount.toString()
                        }
                    )
                }
            }
        }
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = contentDescription
        )
    }
}