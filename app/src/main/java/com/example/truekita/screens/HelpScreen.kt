package com.example.truekita.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun HelpScreen(navController: NavController) {
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val lightBlue = Color(0xFFE3F2FD)
    val darkBlue = Color(0xFF0D47A1)

    var showFaqDialog by remember { mutableStateOf(false) }
    var showTicketDialog by remember { mutableStateOf(false) }
    var loadingSupport by remember { mutableStateOf(false) }

    fun abrirChatConAdmin() {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                context,
                "Debes iniciar sesión para contactar soporte",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        loadingSupport = true

        db.collection("usuarios")
            .whereEqualTo("rol", "admin")
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    loadingSupport = false

                    Toast.makeText(
                        context,
                        "No hay administrador disponible. Genera un ticket.",
                        Toast.LENGTH_LONG
                    ).show()

                    showTicketDialog = true
                    return@addOnSuccessListener
                }

                val adminDoc = result.documents.first()
                val adminUid = adminDoc.getString("uid") ?: adminDoc.id
                val adminName = adminDoc.getString("nombre") ?: "Administrador"

                val userName = user.displayName ?: user.email ?: "Usuario"
                val chatId = listOf(user.uid, adminUid).sorted().joinToString("_")

                val chatData = hashMapOf(
                    "participantes" to listOf(user.uid, adminUid),
                    "participantesNombres" to mapOf(
                        user.uid to userName,
                        adminUid to adminName
                    ),
                    "tipo" to "soporte",
                    "ultimoMensaje" to "",
                    "fechaUltimoMensaje" to Timestamp.now()
                )

                db.collection("chats")
                    .document(chatId)
                    .set(chatData, SetOptions.merge())
                    .addOnSuccessListener {
                        loadingSupport = false

                        navController.navigate(
                            "${Screen.ChatDetail.route}/$chatId/${Uri.encode(adminName)}"
                        )
                    }
                    .addOnFailureListener { error ->
                        loadingSupport = false

                        Toast.makeText(
                            context,
                            "Error al abrir chat: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { error ->
                loadingSupport = false

                Toast.makeText(
                    context,
                    "Error buscando administrador: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    if (showFaqDialog) {
        FaqDialog(
            onDismiss = {
                showFaqDialog = false
            }
        )
    }

    if (showTicketDialog) {
        TicketDialog(
            onDismiss = {
                showTicketDialog = false
            },
            onSave = { asunto, mensaje, prioridad ->
                val user = auth.currentUser

                if (user == null) {
                    Toast.makeText(
                        context,
                        "Debes iniciar sesión para generar un ticket",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@TicketDialog
                }

                val ticketData = hashMapOf(
                    "usuarioUid" to user.uid,
                    "usuarioCorreo" to (user.email ?: ""),
                    "asunto" to asunto,
                    "mensaje" to mensaje,
                    "prioridad" to prioridad,
                    "estado" to "pendiente",
                    "fechaCreacion" to Timestamp.now()
                )

                db.collection("ticketsSoporte")
                    .add(ticketData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Ticket generado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()

                        showTicketDialog = false
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(
                            context,
                            "Error al generar ticket: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.help_center),
                showBack = true,
                onBackClick = {
                    navController.popBackStack()
                }
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
                .background(lightBlue)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = darkBlue
                )

                Text(
                    text = stringResource(id = R.string.find_answers),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }

            HelpCard(
                title = stringResource(id = R.string.live_support),
                subtitle = if (loadingSupport) {
                    "Abriendo chat..."
                } else {
                    stringResource(id = R.string.real_time_chat)
                },
                icon = Icons.Default.SupportAgent,
                iconColor = Color(0xFF4CAF50)
            ) {
                if (!loadingSupport) {
                    abrirChatConAdmin()
                }
            }

            HelpCard(
                title = stringResource(id = R.string.faq),
                subtitle = stringResource(id = R.string.solve_common_questions),
                icon = Icons.Default.QuestionAnswer,
                iconColor = Color(0xFF2196F3)
            ) {
                showFaqDialog = true
            }

            HelpCard(
                title = stringResource(id = R.string.generate_ticket),
                subtitle = stringResource(id = R.string.administrator),
                icon = Icons.Default.ConfirmationNumber,
                iconColor = Color(0xFFFF9800)
            ) {
                showTicketDialog = true
            }
        }
    }
}

@Composable
fun HelpCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = iconColor
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun FaqDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Preguntas frecuentes")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FaqItem(
                    question = "¿Cómo publico un producto?",
                    answer = "Entra al apartado de publicar producto, selecciona una imagen, llena los datos y presiona Publicar."
                )

                FaqItem(
                    question = "¿Cómo veo mis publicaciones?",
                    answer = "Desde Perfil puedes entrar a Mis publicaciones. Ahí puedes ver, editar o eliminar tus productos."
                )

                FaqItem(
                    question = "¿Cómo solicito una ruta?",
                    answer = "Entra a Transporte, revisa las rutas disponibles y presiona Solicitar viaje."
                )

                FaqItem(
                    question = "¿Qué hago si olvidé mi contraseña?",
                    answer = "En la pantalla de inicio de sesión puedes usar la opción de recuperar contraseña."
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "Cerrar")
            }
        }
    )
}

@Composable
fun FaqItem(
    question: String,
    answer: String
) {
    Column {
        Text(
            text = question,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = answer,
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDialog(
    onDismiss: () -> Unit,
    onSave: (
        asunto: String,
        mensaje: String,
        prioridad: String
    ) -> Unit
) {
    var asunto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var prioridad by remember { mutableStateOf("Normal") }
    var expanded by remember { mutableStateOf(false) }

    val prioridades = listOf("Normal", "Urgente")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Generar ticket")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = asunto,
                    onValueChange = {
                        asunto = it
                    },
                    label = {
                        Text("Asunto")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = mensaje,
                    onValueChange = {
                        mensaje = it
                    },
                    label = {
                        Text("Describe tu problema")
                    },
                    minLines = 3
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    }
                ) {
                    OutlinedTextField(
                        value = prioridad,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Prioridad")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        prioridades.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text(text = item)
                                },
                                onClick = {
                                    prioridad = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (asunto.isBlank() || mensaje.isBlank()) {
                        return@TextButton
                    }

                    onSave(asunto, mensaje, prioridad)
                }
            ) {
                Text(text = "Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "Cancelar")
            }
        }
    )
}