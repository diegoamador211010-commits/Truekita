package com.example.truekita.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // IMPORTANTE: Corrige el error de items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truekita.R
import com.example.truekita.components.NotificationCard

// Definición del modelo para evitar el error "Unresolved reference"
data class NotificationUi(
    val title: String,
    val subtitle: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    onDismiss: () -> Unit
) {
    val todayNotifications = listOf(
        NotificationUi(stringResource(id = R.string.alex_perez), stringResource(id = R.string.product_calculator), stringResource(id = R.string.accepted)),
        NotificationUi(stringResource(id = R.string.natalia_puentes), stringResource(id = R.string.transport_720), stringResource(id = R.string.accepted))
    )

    val yesterdayNotifications = listOf(
        NotificationUi(stringResource(id = R.string.omar_andrade), stringResource(id = R.string.transport_650), stringResource(id = R.string.full)),
        NotificationUi(stringResource(id = R.string.natalia_puentes), stringResource(id = R.string.new_message), ""),
        NotificationUi(stringResource(id = R.string.alex_perez), stringResource(id = R.string.new_message), "")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.notifications),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Text(stringResource(id = R.string.today), fontWeight = FontWeight.Bold, color = Color.Gray)
                }
                items(todayNotifications) { item ->
                    NotificationCard(item.title, item.subtitle, item.status)
                }
                item {
                    Text(stringResource(id = R.string.yesterday), fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(top = 10.dp))
                }
                items(yesterdayNotifications) { item ->
                    NotificationCard(item.title, item.subtitle, item.status)
                }
            }
        }
    }
}