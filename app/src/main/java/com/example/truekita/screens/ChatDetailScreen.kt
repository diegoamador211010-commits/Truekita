package com.example.truekita.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Locale

data class ChatMessageDb(
    val id: String = "",
    val texto: String = "",
    val enviadoPorUid: String = "",
    val enviadoPorNombre: String = "",
    val fecha: Timestamp? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    chatId: String,
    otherUserName: String
) {
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser
    val currentUid = currentUser?.uid
    val currentName = currentUser?.displayName
        ?: currentUser?.email
        ?: "Usuario"

    var message by remember { mutableStateOf("") }
    var mensajes by remember { mutableStateOf<List<ChatMessageDb>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    DisposableEffect(chatId) {
        val listener = db.collection("chats")
            .document(chatId)
            .collection("mensajes")
            .orderBy("fecha", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    loading = false

                    Toast.makeText(
                        context,
                        "Error al cargar mensajes: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addSnapshotListener
                }

                mensajes = snapshot?.documents?.map { document ->
                    ChatMessageDb(
                        id = document.id,
                        texto = document.getString("texto") ?: "",
                        enviadoPorUid = document.getString("enviadoPorUid") ?: "",
                        enviadoPorNombre = document.getString("enviadoPorNombre") ?: "",
                        fecha = document.getTimestamp("fecha")
                    )
                } ?: emptyList()

                loading = false
            }

        onDispose {
            listener.remove()
        }
    }

    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.lastIndex)
        }
    }

    fun enviarMensaje() {
        if (currentUid == null) {
            Toast.makeText(
                context,
                "Inicia sesión para enviar mensajes",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val textoEnviar = message.trim()

        if (textoEnviar.isBlank()) {
            return
        }

        sending = true
        message = ""

        val now = Timestamp.now()

        val mensajeNuevo = hashMapOf(
            "texto" to textoEnviar,
            "enviadoPorUid" to currentUid,
            "enviadoPorNombre" to currentName,
            "fecha" to now
        )

        val chatRef = db.collection("chats").document(chatId)

        chatRef.collection("mensajes")
            .add(mensajeNuevo)
            .addOnSuccessListener {
                chatRef.set(
                    mapOf(
                        "chatId" to chatId,
                        "ultimoMensaje" to textoEnviar,
                        "fechaUltimoMensaje" to now,
                        "ultimoMensajePorUid" to currentUid,
                        "participantes" to chatId.split("_")
                    ),
                    SetOptions.merge()
                )

                sending = false
            }
            .addOnFailureListener { error ->
                sending = false
                message = textoEnviar

                Toast.makeText(
                    context,
                    "Error al enviar: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEAF4FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF0D47A1),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = otherUserName.ifBlank { "Chat" },
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Text(
                                text = "Mensajes",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                value = message,
                onValueChange = {
                    message = it
                },
                sending = sending,
                onSendClick = {
                    enviarMensaje()
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        },
        containerColor = Color(0xFFE3F2FD)
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE3F2FD))
        ) {
            when {
                currentUid == null -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Inicia sesión para enviar mensajes",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }

                loading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                mensajes.isEmpty() -> {
                    EmptyChatState(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        )
                    ) {
                        items(mensajes, key = { it.id }) { item ->
                            ChatBubble(
                                message = item,
                                isMine = item.enviadoPorUid == currentUid
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAF4FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF0D47A1),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "No hay mensajes todavía",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Envía el primer mensaje para iniciar la conversación.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    sending: Boolean,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Escribe un mensaje...")
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = false,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF7F7F7),
                    unfocusedContainerColor = Color(0xFFF7F7F7),
                    focusedBorderColor = Color(0xFF0D47A1),
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = onSendClick,
                modifier = Modifier.size(52.dp),
                containerColor = if (value.isBlank() || sending) {
                    Color.LightGray
                } else {
                    Color(0xFF0D47A1)
                },
                contentColor = Color.White
            ) {
                if (sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar"
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessageDb,
    isMine: Boolean
) {
    val bubbleColor = if (isMine) {
        Color(0xFFDCF8C6)
    } else {
        Color.White
    }

    val alignment = if (isMine) {
        Arrangement.End
    } else {
        Arrangement.Start
    }

    val timeText = message.fecha?.toDate()?.let { date ->
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
    } ?: ""

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isMine) 18.dp else 4.dp,
                    bottomEnd = if (isMine) 4.dp else 18.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = bubbleColor
                ),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 14.dp,
                        end = 14.dp,
                        top = 10.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text(
                        text = message.texto,
                        fontSize = 15.sp,
                        color = Color.Black,
                        lineHeight = 20.sp
                    )

                    if (timeText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = timeText,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}