package com.example.truekita.screens.admin

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

data class AdminChatPreview(
    val chatId: String = "",
    val userName: String = "",
    val lastMessage: String = "",
    val fechaUltimoMensaje: Timestamp? = null
)

@Composable
fun AdminChatsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser
    val adminUid = currentUser?.uid

    var chats by remember {
        mutableStateOf<List<AdminChatPreview>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    DisposableEffect(adminUid) {
        if (adminUid == null) {
            loading = false
            onDispose { }
        } else {
            val listener = db.collection("chats")
                .whereArrayContains("participantes", adminUid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        loading = false

                        Toast.makeText(
                            context,
                            "Error al cargar chats: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addSnapshotListener
                    }

                    chats = snapshot?.documents
                        ?.map { document ->
                            val nombres = document.get("participantesNombres") as? Map<*, *>

                            val nombreUsuario = nombres
                                ?.filterKeys { key -> key != adminUid }
                                ?.values
                                ?.firstOrNull()
                                ?.toString()
                                ?: "Usuario"

                            AdminChatPreview(
                                chatId = document.id,
                                userName = nombreUsuario,
                                lastMessage = document.getString("ultimoMensaje") ?: "",
                                fechaUltimoMensaje = document.getTimestamp("fechaUltimoMensaje")
                            )
                        }
                        ?.sortedByDescending {
                            it.fechaUltimoMensaje?.toDate()?.time ?: 0L
                        } ?: emptyList()

                    loading = false
                }

            onDispose {
                listener.remove()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Regresar"
                )
            }

            Text(
                text = "Chats de usuarios",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = Color(0xFF0D47A1),
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Bandeja de soporte",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.Black
                    )

                    Text(
                        text = "Aquí aparecen los chats donde participa el administrador.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        when {
            adminUid == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Debes iniciar sesión como administrador",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }

            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            chats.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Todavía no hay chats para el administrador",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(chats, key = { it.chatId }) { chat ->
                        AdminChatItemRow(
                            chat = chat,
                            onClick = {
                                navController.navigate(
                                    "${Screen.ChatDetail.route}/${chat.chatId}/${Uri.encode(chat.userName)}"
                                )
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminChatItemRow(
    chat: AdminChatPreview,
    onClick: () -> Unit
) {
    val hora = chat.fechaUltimoMensaje?.toDate()?.let { fecha ->
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(fecha)
    } ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xFF0D47A1)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = chat.userName.ifBlank { "Usuario" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Text(
                    text = hora,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (chat.lastMessage.isNotBlank()) {
                    chat.lastMessage
                } else {
                    "Sin mensajes todavía"
                },
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}