package com.example.truekita.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class RutaTransporte(
    val id: String = "",
    val nombreConductor: String = "",
    val trayecto: String = "",
    val horaSalida: String = "",
    val lugaresDisponibles: String = "",
    val tipo: String = "",
    val conductorUid: String = "",
    val conductorCorreo: String = ""
)

@Composable
fun RoutesScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val backgroundColor = Color(0xFFE3F2FD)
    val buttonGreen = Color(0xFFA5F0B5)

    var rutas by remember {
        mutableStateOf<List<RutaTransporte>>(emptyList())
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

                rutas = snapshot?.documents?.map { document ->
                    RutaTransporte(
                        id = document.id,
                        nombreConductor = document.getString("nombreConductor") ?: "",
                        trayecto = document.getString("trayecto") ?: "",
                        horaSalida = document.getString("horaSalida") ?: "",
                        lugaresDisponibles = document.getString("lugaresDisponibles") ?: "",
                        tipo = document.getString("tipo") ?: "",
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

    fun solicitarViaje(ruta: RutaTransporte) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(
                context,
                "Debes iniciar sesión para solicitar viaje",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (ruta.conductorUid == currentUser.uid) {
            Toast.makeText(
                context,
                "No puedes solicitar tu propia ruta",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val rutaRef = db.collection("rutas").document(ruta.id)

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
                    "fechaActualizacion" to Timestamp.now(),
                    "estado" to if (nuevosLugares == 0) {
                        "llena"
                    } else {
                        "disponible"
                    }
                )
            )

            nuevosLugares
        }.addOnSuccessListener { nuevosLugares ->

            val solicitudData = hashMapOf(
                "rutaId" to ruta.id,
                "trayecto" to ruta.trayecto,
                "nombreConductor" to ruta.nombreConductor,
                "conductorUid" to ruta.conductorUid,
                "conductorCorreo" to ruta.conductorCorreo,
                "solicitanteUid" to currentUser.uid,
                "solicitanteCorreo" to (currentUser.email ?: ""),
                "estado" to "aceptada",
                "fechaSolicitud" to Timestamp.now()
            )

            db.collection("solicitudesRutas")
                .add(solicitudData)

            if (ruta.conductorUid.isNotBlank()) {
                val notificacionData = hashMapOf(
                    "usuarioUid" to ruta.conductorUid,
                    "titulo" to "Nueva solicitud de viaje",
                    "subtitulo" to "${currentUser.email ?: "Un usuario"} solicitó tu ruta: ${ruta.trayecto}",
                    "estado" to "Pendiente",
                    "fecha" to Timestamp.now(),
                    "leida" to false
                )

                db.collection("notificaciones")
                    .add(notificacionData)
            }

            Toast.makeText(
                context,
                "Solicitud enviada. Lugares restantes: $nuevosLugares",
                Toast.LENGTH_SHORT
            ).show()

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

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(id = R.string.routes),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            when {
                cargando -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                rutas.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no hay rutas publicadas",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(rutas, key = { ruta -> ruta.id }) { ruta ->
                            RouteItemCard(
                                navController = navController,
                                ruta = ruta,
                                context = context,
                                onSolicitarViaje = {
                                    solicitarViaje(ruta)
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    navController.navigate(Screen.PublishRoute.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonGreen
                ),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.add_route),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun RouteItemCard(
    navController: NavController,
    ruta: RutaTransporte,
    context: Context,
    onSolicitarViaje: () -> Unit
) {
    val darkBlue = Color(0xFF0D47A1)

    val lugares = ruta.lugaresDisponibles.toIntOrNull() ?: 0
    val isFull = lugares <= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (ruta.nombreConductor.isNotBlank()) {
                        ruta.nombreConductor
                    } else {
                        "Sin conductor"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = "★★★★★",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Text(
                    text = if (ruta.trayecto.isNotBlank()) {
                        ruta.trayecto
                    } else {
                        "Sin trayecto"
                    },
                    fontSize = 14.sp
                )

                Text(
                    text = if (ruta.horaSalida.isNotBlank()) {
                        "Salida: ${ruta.horaSalida}"
                    } else {
                        "Sin hora de salida"
                    },
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = Color(0xFFEEEEEE),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = if (isFull) {
                                        Color.Red
                                    } else {
                                        Color.Green
                                    },
                                    shape = CircleShape
                                )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (isFull) {
                                "Ruta llena"
                            } else {
                                "$lugares lugares disponibles"
                            },
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Button(
                    onClick = {
                        navController.navigate("${Screen.MapasRuta.route}/${ruta.id}")
                    },
                    modifier = Modifier
                        .width(120.dp)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = darkBlue
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.view_route),
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSolicitarViaje,
                    enabled = !isFull,
                    modifier = Modifier
                        .width(120.dp)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFull) {
                            Color.LightGray
                        } else {
                            darkBlue
                        }
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isFull) {
                            "Llena"
                        } else {
                            stringResource(id = R.string.request_trip)
                        },
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}