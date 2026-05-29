package com.example.truekita.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class RouteRequestData(
    val id: String = "",
    val rutaId: String = "",
    val trayecto: String = "",
    val horaSalida: String = "",
    val solicitanteUid: String = "",
    val solicitanteCorreo: String = "",
    val estado: String = "",
    val fechaSolicitud: Timestamp? = null
)

@Composable
fun RouteRequestsScreen(
    navController: NavController,
    routeId: String
) {
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var requests by remember {
        mutableStateOf<List<RouteRequestData>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var processingRequestId by remember {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(currentUser?.uid, routeId) {
        if (currentUser == null) {
            loading = false
            onDispose { }
        } else {
            val listener = db.collection("solicitudesRutas")
                .whereEqualTo("conductorUid", currentUser.uid)
                .whereEqualTo("rutaId", routeId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        loading = false

                        Toast.makeText(
                            context,
                            "Error al cargar solicitudes: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()

                        return@addSnapshotListener
                    }

                    requests = snapshot?.documents
                        ?.map { document ->
                            RouteRequestData(
                                id = document.id,
                                rutaId = document.getString("rutaId") ?: "",
                                trayecto = document.getString("trayecto") ?: "",
                                horaSalida = document.getString("horaSalida") ?: "",
                                solicitanteUid = document.getString("solicitanteUid") ?: "",
                                solicitanteCorreo = document.getString("solicitanteCorreo") ?: "",
                                estado = document.getString("estado") ?: "pendiente",
                                fechaSolicitud = document.getTimestamp("fechaSolicitud")
                            )
                        }
                        ?.sortedByDescending { item ->
                            item.fechaSolicitud?.toDate()?.time ?: 0L
                        }
                        ?: emptyList()

                    loading = false
                }

            onDispose {
                listener.remove()
            }
        }
    }

    fun aceptarSolicitud(request: RouteRequestData) {
        if (request.estado.lowercase() != "pendiente") {
            Toast.makeText(
                context,
                "Esta solicitud ya fue respondida",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        processingRequestId = request.id

        val solicitudRef = db.collection("solicitudesRutas").document(request.id)

        solicitudRef.update(
            mapOf(
                "estado" to "aceptada",
                "fechaRespuesta" to Timestamp.now()
            )
        ).addOnSuccessListener {
            val notificacionData = hashMapOf(
                "usuarioUid" to request.solicitanteUid,
                "titulo" to "Solicitud aceptada",
                "subtitulo" to "Tu solicitud para la ruta ${request.trayecto} fue aceptada",
                "estado" to "Aceptada",
                "fecha" to Timestamp.now(),
                "leida" to false,
                "tipo" to "ruta",
                "rutaId" to request.rutaId
            )

            db.collection("notificaciones")
                .add(notificacionData)

            processingRequestId = null

            Toast.makeText(
                context,
                "Solicitud aceptada",
                Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener { error ->
            processingRequestId = null

            Toast.makeText(
                context,
                "Error al aceptar: ${error.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun rechazarSolicitud(request: RouteRequestData) {
        if (request.estado.lowercase() != "pendiente") {
            Toast.makeText(
                context,
                "Esta solicitud ya fue respondida",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        processingRequestId = request.id

        val solicitudRef = db.collection("solicitudesRutas").document(request.id)
        val rutaRef = db.collection("rutas").document(request.rutaId)

        db.runTransaction { transaction ->
            val solicitudSnapshot = transaction.get(solicitudRef)

            val estadoActual = solicitudSnapshot.getString("estado") ?: "pendiente"

            if (estadoActual.lowercase() != "pendiente") {
                throw Exception("Solicitud ya respondida")
            }

            val rutaSnapshot = transaction.get(rutaRef)

            val lugaresTexto = rutaSnapshot.getString("lugaresDisponibles") ?: "0"
            val lugaresActuales = lugaresTexto.toIntOrNull() ?: 0
            val nuevosLugares = lugaresActuales + 1

            transaction.update(
                solicitudRef,
                mapOf(
                    "estado" to "rechazada",
                    "fechaRespuesta" to Timestamp.now()
                )
            )

            transaction.update(
                rutaRef,
                mapOf(
                    "lugaresDisponibles" to nuevosLugares.toString(),
                    "estado" to "disponible",
                    "fechaActualizacion" to Timestamp.now()
                )
            )

            nuevosLugares
        }.addOnSuccessListener { nuevosLugares ->
            val notificacionData = hashMapOf(
                "usuarioUid" to request.solicitanteUid,
                "titulo" to "Solicitud rechazada",
                "subtitulo" to "Tu solicitud para la ruta ${request.trayecto} fue rechazada",
                "estado" to "Rechazada",
                "fecha" to Timestamp.now(),
                "leida" to false,
                "tipo" to "ruta",
                "rutaId" to request.rutaId
            )

            db.collection("notificaciones")
                .add(notificacionData)

            processingRequestId = null

            Toast.makeText(
                context,
                "Solicitud rechazada. Lugares disponibles: $nuevosLugares",
                Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener { error ->
            processingRequestId = null

            val mensaje = if (error.message == "Solicitud ya respondida") {
                "Esta solicitud ya fue respondida"
            } else {
                "Error al rechazar: ${error.message}"
            }

            Toast.makeText(
                context,
                mensaje,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Solicitudes de ruta",
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
                .background(Color(0xFFE3F2FD))
                .padding(16.dp)
        ) {
            when {
                currentUser == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Inicia sesión para ver solicitudes",
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

                requests.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún nadie ha solicitado esta ruta",
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
                        items(requests, key = { it.id }) { request ->
                            RouteRequestCard(
                                request = request,
                                isProcessing = processingRequestId == request.id,
                                onAcceptClick = {
                                    aceptarSolicitud(request)
                                },
                                onRejectClick = {
                                    rechazarSolicitud(request)
                                },
                                onMessageClick = {
                                    val chatId = listOf(
                                        currentUser.uid,
                                        request.solicitanteUid
                                    ).sorted().joinToString("_")

                                    val otherName = request.solicitanteCorreo.ifBlank {
                                        "Usuario"
                                    }

                                    navController.navigate(
                                        "${Screen.ChatDetail.route}/$chatId/${Uri.encode(otherName)}"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RouteRequestCard(
    request: RouteRequestData,
    isProcessing: Boolean,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    val isPending = request.estado.lowercase() == "pendiente"
    val statusColor = when (request.estado.lowercase()) {
        "aceptada" -> Color(0xFF2E7D32)
        "rechazada" -> Color.Red
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFFEAF4FF)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF0D47A1),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Solicitud de viaje",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.Black
                    )

                    Text(
                        text = request.estado.replaceFirstChar { it.uppercase() },
                        fontSize = 13.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            RequestInfoRow(
                icon = Icons.Default.Email,
                label = "Usuario que solicitó",
                value = request.solicitanteCorreo.ifBlank { "Correo no disponible" }
            )

            RequestInfoRow(
                icon = Icons.Default.Route,
                label = "Ruta",
                value = request.trayecto.ifBlank { "Sin trayecto" }
            )

            RequestInfoRow(
                icon = Icons.Default.AccessTime,
                label = "Hora de salida",
                value = request.horaSalida.ifBlank { "Sin hora" }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (isPending) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAcceptClick,
                        enabled = !isProcessing,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF81C784)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = if (isProcessing) "..." else "Aceptar",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onRejectClick,
                        enabled = !isProcessing,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = if (isProcessing) "..." else "Rechazar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = onMessageClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D47A1)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Enviar mensaje",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RequestInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF0D47A1),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp
            )

            Text(
                text = value,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}