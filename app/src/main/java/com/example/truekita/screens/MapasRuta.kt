package com.example.truekita.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.BottomNavBar
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

data class RouteDetailData(
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
    val conductorCorreo: String = ""
)

@Composable
fun MapasRuta(
    navController: NavController,
    routeId: String = ""
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    val lightBlue = Color(0xFFE3F2FD)
    val darkBlue = Color(0xFF0D47A1)

    var route by remember {
        mutableStateOf<RouteDetailData?>(null)
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    LaunchedEffect(routeId) {
        if (routeId.isBlank()) {
            loading = false
            errorMessage = "No se encontró la ruta seleccionada"
            return@LaunchedEffect
        }

        db.collection("rutas")
            .document(routeId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    route = RouteDetailData(
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
                        conductorCorreo = document.getString("conductorCorreo") ?: ""
                    )
                } else {
                    errorMessage = "La ruta ya no existe"
                }

                loading = false
            }
            .addOnFailureListener { error ->
                loading = false
                errorMessage = "Error al cargar ruta: ${error.message}"

                Toast.makeText(
                    context,
                    errorMessage,
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
                .background(lightBlue)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.back),
                        tint = Color.DarkGray
                    )
                }

                Text(
                    text = stringResource(id = R.string.view_route),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage.isNotBlank() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                route != null -> {
                    val item = route!!

                    val routePosition = if (item.latitud != 0.0 || item.longitud != 0.0) {
                        LatLng(item.latitud, item.longitud)
                    } else {
                        LatLng(20.6597, -103.3496)
                    }

                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(routePosition, 15f)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(3.dp)
                        ) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = MapProperties(
                                    isMyLocationEnabled = false
                                ),
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = false,
                                    scrollGesturesEnabled = false,
                                    zoomGesturesEnabled = false,
                                    tiltGesturesEnabled = false,
                                    rotationGesturesEnabled = false,
                                    myLocationButtonEnabled = false,
                                    mapToolbarEnabled = false
                                )
                            ) {
                                Marker(
                                    state = MarkerState(position = routePosition),
                                    title = item.trayecto.ifBlank { "Ruta" },
                                    snippet = item.ubicacionNombre.ifBlank { "Punto de salida" }
                                )
                            }
                        }

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
                                        modifier = Modifier.size(52.dp),
                                        shape = CircleShape,
                                        color = Color(0xFFEAF4FF)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Route,
                                                contentDescription = null,
                                                tint = darkBlue,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = item.trayecto.ifBlank { "Ruta sin trayecto" },
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )

                                        Text(
                                            text = item.tipo.ifBlank { "Tipo de ruta no especificado" },
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                RouteDetailInfoRow(
                                    icon = Icons.Default.Person,
                                    label = "Conductor",
                                    value = item.nombreConductor.ifBlank { "Sin conductor" }
                                )

                                RouteDetailInfoRow(
                                    icon = Icons.Default.AccessTime,
                                    label = "Hora de salida",
                                    value = item.horaSalida.ifBlank { "Sin hora registrada" }
                                )

                                RouteDetailInfoRow(
                                    icon = Icons.Default.EventSeat,
                                    label = "Lugares disponibles",
                                    value = item.lugaresDisponibles.ifBlank { "Sin lugares registrados" }
                                )

                                RouteDetailInfoRow(
                                    icon = Icons.Default.LocationOn,
                                    label = "Punto de salida",
                                    value = item.ubicacionNombre.ifBlank { "Ubicación no registrada" }
                                )

                                RouteDetailInfoRow(
                                    icon = Icons.Default.DirectionsCar,
                                    label = "Estado",
                                    value = item.estado.ifBlank { "disponible" }
                                )

                                if (item.conductorCorreo.isNotBlank()) {
                                    RouteDetailInfoRow(
                                        icon = Icons.Default.Person,
                                        label = "Correo del conductor",
                                        value = item.conductorCorreo
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {
                                Text(
                                    text = "Coordenadas de la ruta",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Latitud: ${item.latitud}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )

                                Text(
                                    text = "Longitud: ${item.longitud}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RouteDetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
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
                fontSize = 15.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}