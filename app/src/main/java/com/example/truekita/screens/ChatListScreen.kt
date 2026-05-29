package com.example.truekita.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.components.SearchBarPlaceholder
import com.example.truekita.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

data class ChatPreview(
    val chatId: String = "",
    val otherUserName: String = "",
    val lastMessage: String = "",
    val fechaUltimoMensaje: Timestamp? = null,
    val unreadCount: Int = 0
)

@Composable
fun ChatListScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser
    val currentUid = currentUser?.uid

    var chats by remember { mutableStateOf<List<ChatPreview>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    DisposableEffect(currentUid) {
        if (currentUid == null) {
            loading = false
            onDispose { }
        } else {
            val listener = db.collection("chats")
                .whereArrayContains("participantes", currentUid)
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

                    val lista = snapshot?.documents?.map { document ->
                        val nombres = document.get("participantesNombres") as? Map<*, *>
                        val nombreOtro = nombres
                            ?.filterKeys { it != currentUid }
                            ?.values
                            ?.firstOrNull()
                            ?.toString()
                            ?: "Usuario"

                        ChatPreview(
                            chatId = document.id,
                            otherUserName = nombreOtro,
                            lastMessage = document.getString("ultimoMensaje") ?: "",
                            fechaUltimoMensaje = document.getTimestamp("fechaUltimoMensaje"),
                            unreadCount = 0
                        )
                    }?.sortedByDescending {
                        it.fechaUltimoMensaje?.toDate()?.time ?: 0L
                    } ?: emptyList()

                    chats = lista
                    loading = false
                }

            onDispose {
                listener.remove()
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.chat),
                showBack = false
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                SearchBarPlaceholder(
                    text = stringResource(id = R.string.search_user),
                    onSearchClick = { }
                )
            }

            when {
                currentUid == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Inicia sesión para ver tus chats",
                            color = Color.Gray
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
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no tienes chats",
                            color = Color.Gray
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(chats, key = { it.chatId }) { chat ->
                            ChatItemRow(chat) {
                                navController.navigate(
                                    "${Screen.ChatDetail.route}/${chat.chatId}/${Uri.encode(chat.otherUserName)}"
                                )
                            }

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
}

@Composable
fun ChatItemRow(
    chat: ChatPreview,
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color(0xFF0D47A1)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.otherUserName.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = chat.otherUserName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = hora,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Text(
                text = if (chat.lastMessage.isNotBlank()) chat.lastMessage else "Sin mensajes",
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}