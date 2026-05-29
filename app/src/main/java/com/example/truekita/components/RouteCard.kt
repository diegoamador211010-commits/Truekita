package com.example.truekita.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truekita.screens.ViewRouteData

@Composable
fun RouteCard(
    route: ViewRouteData,
    onVerRuta: () -> Unit,
    onSolicitar: () -> Unit
) {
    val lugares = route.lugaresDisponibles.toIntOrNull() ?: 0
    val isAvailable = lugares > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = if (route.nombreConductor.isNotBlank()) {
                        route.nombreConductor
                    } else {
                        "Sin conductor"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = "Calificación: ★★★★★",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (route.trayecto.isNotBlank()) {
                        "Ruta: ${route.trayecto}"
                    } else {
                        "Ruta: Sin trayecto"
                    },
                    fontSize = 13.sp
                )

                Text(
                    text = if (route.horaSalida.isNotBlank()) {
                        "Salida: ${route.horaSalida}"
                    } else {
                        "Salida: Sin hora"
                    },
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = Color(0xFFEEEEEE),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    if (isAvailable) Color.Green else Color.Red,
                                    CircleShape
                                )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (isAvailable) {
                                "$lugares lugares disponibles"
                            } else {
                                "Ruta llena"
                            },
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onVerRuta,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Ver Ruta",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onSolicitar,
                    enabled = isAvailable,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Solicitar Viaje",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}