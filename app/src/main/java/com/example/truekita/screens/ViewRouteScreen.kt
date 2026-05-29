package com.example.truekita.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.google.firebase.firestore.Query

data class ViewRouteData(
    val id: String = "",
    val nombreConductor: String = "",
    val trayecto: String = "",
    val horaSalida: String = "",
    val lugaresDisponibles: String = "",
    val tipo: String = "",
    val ubicacionNombre: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val estado: String = "",
    val conductorUid: String = "",
    val conductorCorreo: String = ""
)

@Composable
fun ViewRouteScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var routes by remember {
        mutableStateOf<List<ViewRouteData>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    DisposableEffect(Unit) {
        val listener = db.collection("rutas")
            .orderBy("fechaPublicacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    cargando = false

                    Toast.makeText(
                        context,
                        "Error al cargar rutas: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addSnapshotListener
                }

                routes = snapshot?.documents?.map { document ->
                    ViewRouteData(
                        id = document.id,
                        nombreConductor = document.getString("nombreConductor") ?: "",
                        trayecto = document.getString("trayecto") ?: "",
                        horaSalida = document.getString("horaSalida") ?: "",
                        lugaresDisponibles = document.getString("lugaresDisponibles") ?: "",
                        tipo = document.getString("tipo") ?: "",
                        ubicacionNombre = document.getString("ubicacionNombre") ?: "",
                        latitud = document.getDouble("latitud") ?: 0.0,
                        longitud = document.getDouble("longitud") ?: 0.0,
                        estado = document.getString("estado") ?: "disponible",
                        conductorUid = document.getString("conductorUid") ?: "",
                        conductorCorreo = document.getString("conductorCorreo") ?: ""
                    )
                } ?: emptyList()

                cargando = false
            }

        onDispose {
            listener.remove()
        }
    }

    fun solicitarViaje(route: ViewRouteData) {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(
                context,
                "Debes iniciar sesión para solicitar viaje",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (route.conductorUid.isBlank()) {
            Toast.makeText(
                context,
                "Esta ruta no tiene conductor asignado. Revisa conductorUid en Firebase.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (route.conductorUid == user.uid) {
            Toast.makeText(
                context,
                "No puedes solicitar tu propia ruta",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val solicitudesRef = db.collection("solicitudesRutas")

        solicitudesRef
            .whereEqualTo("rutaId", route.id)
            .whereEqualTo("solicitanteUid", user.uid)
            .get()
            .addOnSuccessListener { existingRequests ->

                if (!existingRequests.isEmpty) {
                    Toast.makeText(
                        context,
                        "Ya solicitaste esta ruta",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }

                val rutaRef = db.collection("rutas").document(route.id)

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(rutaRef)

                    val lugaresTexto = snapshot.getString("lugaresDisponibles") ?: "0"
                    val lugaresActuales = lugaresTexto.toIntOrNull() ?: 0

                    if (lugaresActuales <= 0) {
                        throw Exception("Ruta llena")
                    }

                    val nuevosLugares = lugaresActuales - 1

                    transaction.update(
                        rutaRef,
                        mapOf(
                            "lugaresDisponibles" to nuevosLugares.toString(),
                            "estado" to if (nuevosLugares == 0) "llena" else "disponible",
                            "fechaActualizacion" to Timestamp.now()
                        )
                    )

                    nuevosLugares
                }.addOnSuccessListener { nuevosLugares ->

                    val solicitudData = hashMapOf(
                        "rutaId" to route.id,
                        "trayecto" to route.trayecto,
                        "horaSalida" to route.horaSalida,
                        "nombreConductor" to route.nombreConductor,
                        "conductorUid" to route.conductorUid,
                        "conductorCorreo" to route.conductorCorreo,
                        "solicitanteUid" to user.uid,
                        "solicitanteCorreo" to (user.email ?: ""),
                        "estado" to "pendiente",
                        "fechaSolicitud" to Timestamp.now()
                    )

                    db.collection("solicitudesRutas")
                        .add(solicitudData)
                        .addOnSuccessListener {

                            val notificacionData = hashMapOf(
                                "usuarioUid" to route.conductorUid,
                                "titulo" to "Nueva solicitud de viaje",
                                "subtitulo" to "${user.email ?: "Un usuario"} solicitó tu ruta: ${route.trayecto}",
                                "estado" to "Pendiente",
                                "fecha" to Timestamp.now(),
                                "leida" to false,
                                "tipo" to "ruta",
                                "rutaId" to route.id,
                                "solicitanteUid" to user.uid,
                                "solicitanteCorreo" to (user.email ?: "")
                            )

                            db.collection("notificaciones")
                                .add(notificacionData)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Solicitud enviada. Lugares restantes: $nuevosLugares",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { error ->
                                    Toast.makeText(
                                        context,
                                        "Se apartó el lugar, pero falló la notificación: ${error.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(
                                context,
                                "Se bajó el lugar, pero falló la solicitud: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                }.addOnFailureListener { error ->
                    val mensaje = if (error.message == "Ruta llena") {
                        "Esta ruta ya está llena"
                    } else {
                        "Error al solicitar viaje: ${error.message}"
                    }

                    Toast.makeText(
                        context,
                        mensaje,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    context,
                    "Error revisando solicitud: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.routes),
                showBack = true,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { padding ->

        when {
            cargando -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            routes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFE3F2FD))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no hay rutas publicadas",
                        fontSize = 17.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFE3F2FD))
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = 24.dp
                    )
                ) {
                    item {
                        RouteHeaderCard(
                            totalRoutes = routes.size
                        )
                    }

                    items(routes, key = { it.id }) { route ->
                        ViewRouteItemCard(
                            route = route,
                            onVerRuta = {
                                navController.navigate("${Screen.MapasRuta.route}/${route.id}")
                            },
                            onSolicitar = {
                                solicitarViaje(route)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RouteHeaderCard(
    totalRoutes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = Color(0xFFE3F2FD)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color(0xFF0D47A1),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Rutas disponibles",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )

                Text(
                    text = "$totalRoutes rutas publicadas por usuarios",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ViewRouteItemCard(
    route: ViewRouteData,
    onVerRuta: () -> Unit,
    onSolicitar: () -> Unit
) {
    val lugares = route.lugaresDisponibles.toIntOrNull() ?: 0
    val isAvailable = lugares > 0
    val isEnabled = isAvailable && route.estado.lowercase() != "llena"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
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
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = Color(0xFFEAF4FF)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            tint = Color(0xFF0D47A1),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = route.trayecto.ifBlank { "Ruta sin trayecto" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )

                    Text(
                        text = route.tipo.ifBlank { "Tipo de ruta no especificado" },
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                RouteStatusChip(
                    isAvailable = isEnabled,
                    lugares = lugares
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            RouteInfoRow(
                icon = Icons.Default.Person,
                label = "Conductor",
                value = route.nombreConductor.ifBlank { "Sin conductor" }
            )

            RouteInfoRow(
                icon = Icons.Default.AccessTime,
                label = "Hora de salida",
                value = route.horaSalida.ifBlank { "Sin hora registrada" }
            )

            RouteInfoRow(
                icon = Icons.Default.EventSeat,
                label = "Lugares disponibles",
                value = if (isAvailable) "$lugares lugares" else "Ruta llena"
            )

            RouteInfoRow(
                icon = Icons.Default.LocationOn,
                label = "Punto de salida",
                value = route.ubicacionNombre.ifBlank { "Ubicación no registrada" }
            )

            if (route.latitud != 0.0 || route.longitud != 0.0) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Coordenadas: ${route.latitud}, ${route.longitud}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onVerRuta,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D47A1)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.view_route),
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onSolicitar,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnabled) Color(0xFF81C784) else Color.LightGray
                    ),
                    shape = RoundedCornerShape(14.dp),
                    enabled = isEnabled
                ) {
                    Text(
                        text = if (isEnabled) {
                            stringResource(id = R.string.request_trip)
                        } else {
                            "Llena"
                        },
                        fontSize = 13.sp,
                        color = if (isEnabled) Color.Black else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RouteInfoRow(
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
                fontSize = 12.sp,
                color = Color.Gray
            )

            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun RouteStatusChip(
    isAvailable: Boolean,
    lugares: Int
) {
    Surface(
        color = if (isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(
                        if (isAvailable) Color(0xFF2E7D32) else Color.Red,
                        CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = if (isAvailable) "$lugares disp." else "Llena",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAvailable) Color(0xFF2E7D32) else Color.Red
            )
        }
    }
}