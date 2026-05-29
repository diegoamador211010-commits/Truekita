package com.example.truekita.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truekita.R
import com.example.truekita.components.NotificationCard
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

data class NotificationUi(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val status: String = "",
    val fecha: Timestamp? = null,
    val leida: Boolean = false
)

fun notificationIsToday(timestamp: Timestamp?): Boolean {
    if (timestamp == null) return false

    val notificationCalendar = Calendar.getInstance().apply {
        time = timestamp.toDate()
    }

    val todayCalendar = Calendar.getInstance()

    return notificationCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
            notificationCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    val darkBlue = Color(0xFF0D47A1)
    val lightBlue = Color(0xFFE3F2FD)

    var notifications by remember {
        mutableStateOf<List<NotificationUi>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var deleting by remember {
        mutableStateOf(false)
    }

    DisposableEffect(currentUser?.uid) {
        if (currentUser == null) {
            loading = false
            notifications = emptyList()

            onDispose { }
        } else {
            val listener = db.collection("notificaciones")
                .whereEqualTo("usuarioUid", currentUser.uid)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        loading = false

                        Toast.makeText(
                            context,
                            "Error al cargar notificaciones: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addSnapshotListener
                    }

                    val documentos = snapshot?.documents ?: emptyList()

                    notifications = documentos
                        .map { document ->
                            NotificationUi(
                                id = document.id,
                                title = document.getString("titulo") ?: "",
                                subtitle = document.getString("subtitulo") ?: "",
                                status = document.getString("estado") ?: "",
                                fecha = document.getTimestamp("fecha"),
                                leida = document.getBoolean("leida") ?: false
                            )
                        }
                        .sortedByDescending { item ->
                            item.fecha?.toDate()?.time ?: 0L
                        }

                    documentos
                        .filter { document ->
                            document.getBoolean("leida") == false
                        }
                        .forEach { document ->
                            document.reference.update("leida", true)
                        }

                    loading = false
                }

            onDispose {
                listener.remove()
            }
        }
    }

    fun eliminarTodasLasNotificaciones() {
        if (currentUser == null) return

        deleting = true

        db.collection("notificaciones")
            .whereEqualTo("usuarioUid", currentUser.uid)
            .get()
            .addOnSuccessListener { result ->
                val batch = db.batch()

                result.documents.forEach { document ->
                    batch.delete(document.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        deleting = false

                        Toast.makeText(
                            context,
                            "Notificaciones eliminadas",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { error ->
                        deleting = false

                        Toast.makeText(
                            context,
                            "Error al eliminar: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { error ->
                deleting = false

                Toast.makeText(
                    context,
                    "Error al cargar notificaciones: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    val todayNotifications = notifications.filter { item ->
        notificationIsToday(item.fecha)
    }

    val olderNotifications = notifications.filter { item ->
        !notificationIsToday(item.fecha)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
                .background(lightBlue)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = darkBlue,
                                modifier = Modifier.size(30.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = stringResource(id = R.string.notifications),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Text(
                                    text = "${notifications.size} notificaciones",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            eliminarTodasLasNotificaciones()
                        },
                        enabled = notifications.isNotEmpty() && !deleting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        if (deleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = Color.White
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Eliminar notificaciones",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                currentUser == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Inicia sesión para ver tus notificaciones",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }

                loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                notifications.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tienes notificaciones",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 40.dp)
                    ) {
                        if (todayNotifications.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(id = R.string.today),
                                    fontWeight = FontWeight.Bold,
                                    color = darkBlue,
                                    fontSize = 15.sp
                                )
                            }

                            items(todayNotifications, key = { item -> item.id }) { item ->
                                NotificationCard(
                                    item.title,
                                    item.subtitle,
                                    item.status
                                )
                            }
                        }

                        if (olderNotifications.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Anteriores",
                                    fontWeight = FontWeight.Bold,
                                    color = darkBlue,
                                    fontSize = 15.sp
                                )
                            }

                            items(olderNotifications, key = { item -> item.id }) { item ->
                                NotificationCard(
                                    item.title,
                                    item.subtitle,
                                    item.status
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}