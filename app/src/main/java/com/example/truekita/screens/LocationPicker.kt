package com.example.truekita.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun LocationPickerScreen(
    navController: NavController,
    tipoUbicacion: String = "ruta",
    tipoUsuario: String = "usuario"
) {
    val context = LocalContext.current

    val initialPosition = LatLng(21.8833, -102.2916)

    var selectedLatLng by remember {
        mutableStateOf(initialPosition)
    }

    var selectedName by remember {
        mutableStateOf("Ubicación seleccionada")
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 14f)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Seleccionar ubicación",
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
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        selectedLatLng = latLng
                        selectedName = "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}"
                    },
                    properties = MapProperties(
                        isMyLocationEnabled = false
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false
                    )
                ) {
                    Marker(
                        state = MarkerState(position = selectedLatLng),
                        title = selectedName,
                        snippet = "Punto elegido"
                    )
                }

                FloatingActionButton(
                    onClick = {
                        selectedLatLng = initialPosition
                        selectedName = "Ubicación central"

                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(initialPosition, 14f)
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    containerColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Centrar mapa",
                        tint = Color(0xFF0D47A1)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Punto seleccionado",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = selectedName,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Latitud: ${selectedLatLng.latitude}",
                        color = Color.Gray
                    )

                    Text(
                        text = "Longitud: ${selectedLatLng.longitude}",
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("ubicacionNombre", selectedName)

                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("latitud", selectedLatLng.latitude)

                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("longitud", selectedLatLng.longitude)

                            Toast.makeText(
                                context,
                                "Ubicación seleccionada",
                                Toast.LENGTH_SHORT
                            ).show()

                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFA5F0B5)
                        )
                    ) {
                        Text(
                            text = "Guardar ubicación",
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}