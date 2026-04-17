package com.example.truekita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource // IMPORTANTE
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen

@Composable
fun MapasRuta(navController: NavController) {
    val lightBlue = Color(0xFFE3F2FD)
    val darkBlue = Color(0xFF0D47A1)
    val buttonGreen = Color(0xFF98EE99)

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).background(lightBlue).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER ---
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back), tint = Color.Gray)
                }
                Text(
                    text = stringResource(id = R.string.view_route), // "Ver ruta"
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- MAPA ---
            Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().height(250.dp)) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.mapa_ruta),
                        contentDescription = stringResource(id = R.string.map_preview),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTÓN PUNTOS DE ENCUENTRO ---
            Button(
                onClick = { /* Acción puntos */ },
                colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                modifier = Modifier.width(200.dp).height(40.dp)
            ) {
                Text(text = stringResource(id = R.string.meeting_points), color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- TARJETA PARADA INTERMEDIA ---
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)), modifier = Modifier.width(220.dp)) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Walmart", fontWeight = FontWeight.Medium) // Este es nombre propio
                    Text(text = "7:40 AM", fontSize = 14.sp)
                    Text(text = stringResource(id = R.string.three_seats), fontSize = 12.sp) // "3 lugares disponibles"
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- INFO CONDUCTOR ---
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().height(160.dp)) {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = stringResource(id = R.string.driver_omar), fontWeight = FontWeight.Bold)
                        Text(text = stringResource(id = R.string.rating_five_stars), fontSize = 14.sp, color = Color.DarkGray)
                        Text(text = stringResource(id = R.string.departure_720), fontSize = 14.sp, color = Color.DarkGray)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        // Botón Regresar
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1E9FF)),
                            modifier = Modifier.height(38.dp).width(120.dp)
                        ) {
                            Text(text = stringResource(id = R.string.back), color = Color.Black)
                        }

                        // Botón Solicitar
                        Button(
                            onClick = { navController.navigate(Screen.ViewRoute.route) },
                            colors = ButtonDefaults.buttonColors(containerColor = buttonGreen),
                            modifier = Modifier.height(38.dp).width(120.dp)
                        ) {
                            Text(text = stringResource(id = R.string.request_trip), color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}