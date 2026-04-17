package com.example.truekita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.BottomNavBar

@Composable
fun RoutesScreen(navController: NavController) {
    val backgroundColor = Color(0xFFE3F2FD)
    val buttonGreen = Color(0xFFA5F0B5)

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
                .padding(horizontal = 16.dp)
        ) {
            // --- HEADER IGUAL AL TUYO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                }

                Text(
                    text = stringResource(id = R.string.routes),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // USAMOS RUTA DIRECTA PARA EVITAR EL ERROR ROJO
                IconButton(onClick = { navController.navigate("notifications_screen") }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(id = R.string.notifications)
                    )
                }
            }

            // --- LISTA DE RUTAS ---
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(3) { index ->
                    RouteItemCard(
                        navController = navController,
                        isFull = index == 1
                    )
                }
            }

            // --- BOTÓN AGREGAR RUTA ---
            Button(
                onClick = { navController.navigate("publish_route_screen") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonGreen),
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
fun RouteItemCard(navController: NavController, isFull: Boolean) {
    val darkBlue = Color(0xFF0D47A1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(id = R.string.driver_omar), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = stringResource(id = R.string.rating_five_stars), fontSize = 12.sp, color = Color.Gray)
                Text(text = stringResource(id = R.string.route_zone_ita), fontSize = 14.sp)
                Text(text = stringResource(id = R.string.departure_720), fontSize = 14.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = Color(0xFFEEEEEE),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (isFull) Color.Red else Color.Green, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFull) stringResource(id = R.string.full_route) else stringResource(id = R.string.three_seats),
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Button(
                    onClick = { navController.navigate("mapas_ruta_screen") },
                    modifier = Modifier.width(120.dp).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = stringResource(id = R.string.view_route), fontSize = 12.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { /* Lógica para solicitar */ },
                    modifier = Modifier.width(120.dp).height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = darkBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = stringResource(id = R.string.request_trip), fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}