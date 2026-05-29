package com.example.truekita.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

data class AdminTicketUi(
    val id: String = "",
    val asunto: String = "",
    val mensaje: String = "",
    val usuarioCorreo: String = "",
    val usuarioUid: String = "",
    val estado: String = "",
    val prioridad: String = "",
    val respuestaAdmin: String = ""
)

@Composable
fun AdminTicketsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var tickets by remember {
        mutableStateOf<List<AdminTicketUi>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var ticketResponder by remember {
        mutableStateOf<AdminTicketUi?>(null)
    }

    var respuesta by remember {
        mutableStateOf("")
    }

    var ticketEliminar by remember {
        mutableStateOf<AdminTicketUi?>(null)
    }

    var eliminando by remember {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        val listener = db.collection("ticketsSoporte")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    cargando = false

                    Toast.makeText(
                        context,
                        "Error al cargar tickets: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addSnapshotListener
                }

                tickets = snapshot?.documents
                    ?.sortedByDescending { document ->
                        document.getTimestamp("fechaCreacion")?.toDate()?.time ?: 0L
                    }
                    ?.map { document ->
                        AdminTicketUi(
                            id = document.id,
                            asunto = document.getString("asunto") ?: "Sin asunto",
                            mensaje = document.getString("mensaje") ?: "",
                            usuarioCorreo = document.getString("usuarioCorreo") ?: "Sin correo",
                            usuarioUid = document.getString("usuarioUid") ?: "",
                            estado = document.getString("estado") ?: "pendiente",
                            prioridad = document.getString("prioridad") ?: "Normal",
                            respuestaAdmin = document.getString("respuestaAdmin") ?: ""
                        )
                    } ?: emptyList()

                cargando = false
            }

        onDispose {
            listener.remove()
        }
    }

    fun responderTicket(ticket: AdminTicketUi, textoRespuesta: String) {
        db.collection("ticketsSoporte")
            .document(ticket.id)
            .update(
                mapOf(
                    "respuestaAdmin" to textoRespuesta,
                    "estado" to "respondido",
                    "fechaRespuesta" to Timestamp.now()
                )
            )
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Respuesta enviada correctamente",
                    Toast.LENGTH_SHORT
                ).show()

                ticketResponder = null
                respuesta = ""
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    context,
                    "Error al responder: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun cerrarTicket(ticket: AdminTicketUi) {
        db.collection("ticketsSoporte")
            .document(ticket.id)
            .update(
                mapOf(
                    "estado" to "cerrado",
                    "fechaCierre" to Timestamp.now()
                )
            )
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Ticket cerrado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    context,
                    "Error al cerrar ticket: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun eliminarTicket(ticket: AdminTicketUi) {
        eliminando = true

        db.collection("ticketsSoporte")
            .document(ticket.id)
            .delete()
            .addOnSuccessListener {
                eliminando = false
                ticketEliminar = null

                Toast.makeText(
                    context,
                    "Ticket eliminado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                eliminando = false

                Toast.makeText(
                    context,
                    "Error al eliminar ticket: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    ticketResponder?.let { ticket ->
        AlertDialog(
            onDismissRequest = {
                ticketResponder = null
                respuesta = ""
            },
            title = {
                Text(text = "Responder ticket")
            },
            text = {
                Column {
                    Text(
                        text = ticket.asunto,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = ticket.mensaje,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = respuesta,
                        onValueChange = {
                            respuesta = it
                        },
                        label = {
                            Text("Respuesta del administrador")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (respuesta.isBlank()) {
                            Toast.makeText(
                                context,
                                "Escribe una respuesta",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@TextButton
                        }

                        responderTicket(ticket, respuesta.trim())
                    }
                ) {
                    Text(text = "Enviar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        ticketResponder = null
                        respuesta = ""
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    ticketEliminar?.let { ticket ->
        AlertDialog(
            onDismissRequest = {
                if (!eliminando) {
                    ticketEliminar = null
                }
            },
            title = {
                Text(text = "Eliminar ticket")
            },
            text = {
                Text(
                    text = "¿Seguro que quieres eliminar este ticket?\n\n${ticket.asunto}"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        eliminarTicket(ticket)
                    },
                    enabled = !eliminando
                ) {
                    Text(
                        text = if (eliminando) {
                            "Eliminando..."
                        } else {
                            "Eliminar"
                        },
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        ticketEliminar = null
                    },
                    enabled = !eliminando
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD))
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver"
                )
            }

            Text(
                text = "Tickets de soporte",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        when {
            cargando -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            tickets.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay tickets enviados por usuarios",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(tickets, key = { ticket -> ticket.id }) { ticket ->
                        AdminTicketCard(
                            ticket = ticket,
                            onResponder = {
                                ticketResponder = ticket
                                respuesta = ticket.respuestaAdmin
                            },
                            onCerrar = {
                                cerrarTicket(ticket)
                            },
                            onEliminar = {
                                ticketEliminar = ticket
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTicketCard(
    ticket: AdminTicketUi,
    onResponder: () -> Unit,
    onCerrar: () -> Unit,
    onEliminar: () -> Unit
) {
    val estadoColor = when (ticket.estado.lowercase()) {
        "pendiente" -> Color(0xFFFF9800)
        "abierto" -> Color(0xFFFF9800)
        "respondido" -> Color(0xFF2E7D32)
        "cerrado" -> Color.Gray
        else -> Color(0xFF0D47A1)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = Color(0xFF0D47A1)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = ticket.asunto.ifBlank { "Sin asunto" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Usuario: ${ticket.usuarioCorreo}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Prioridad: ${ticket.prioridad}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Estado: ${ticket.estado}",
                fontSize = 14.sp,
                color = estadoColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mensaje:",
                fontSize = 13.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = ticket.mensaje.ifBlank { "Sin mensaje" },
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            if (ticket.respuestaAdmin.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    )
                ) {
                    Text(
                        text = "Respuesta: ${ticket.respuestaAdmin}",
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onResponder,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D47A1)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Responder")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCerrar,
                    modifier = Modifier.weight(1f),
                    enabled = ticket.estado.lowercase() != "cerrado",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF81C784),
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Cerrar",
                        color = Color.Black
                    )
                }

                Button(
                    onClick = onEliminar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(text = "Eliminar")
                }
            }
        }
    }
}