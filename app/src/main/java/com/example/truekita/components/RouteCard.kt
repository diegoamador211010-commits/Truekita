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
import com.example.truekita.screens.RouteData // Asegúrate de que este import sea correcto

@Composable
fun RouteCard(
    route: RouteData,
    onVerRuta: () -> Unit,
    onSolicitar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = route.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Calificacion(Estrellas)", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Ruta: ${route.path}", fontSize = 13.sp)
                Text(text = "Salida: ${route.time}", fontSize = 13.sp)

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
                                .background(if (route.isAvailable) Color.Green else Color.Red, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (route.isAvailable) "${route.spots} lugares Disponibles" else "Ruta llena",
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
                    Text("Ver Ruta", fontSize = 11.sp)
                }
                Button(
                    onClick = onSolicitar,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Solicitar Viaje", fontSize = 11.sp)
                }
            }
        }
    }
}