package com.example.truekita.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen

@Composable
fun ConfigurePointsScreen(navController: NavController) {
    val placeName = remember { mutableStateOf("") }
    val seats = remember { mutableStateOf("") }
    val estimatedTime = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.configure_meeting_points_title),
                showBack = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Card del Mapa con textos del XML
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(id = R.string.map_preview),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(id = R.string.route_visual_reference),
                            color = Color.Gray
                        )
                    }
                }
            }

            // Input: Nombre del lugar
            item {
                OutlinedTextField(
                    value = placeName.value,
                    onValueChange = { placeName.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(id = R.string.place_name_hint)) },
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Input: Cupos
            item {
                OutlinedTextField(
                    value = seats.value,
                    onValueChange = { seats.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(id = R.string.seats_available_hint)) },
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Input: Hora llegada
            item {
                OutlinedTextField(
                    value = estimatedTime.value,
                    onValueChange = { estimatedTime.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(id = R.string.estimated_arrival_hint)) },
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Barra de progreso
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF4FF))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(id = R.string.points_added_progress),
                            fontWeight = FontWeight.Medium
                        )
                        LinearProgressIndicator(
                            progress = { 0.66f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            color = Color(0xFF0D47A1)
                        )
                    }
                }
            }

            // Botón Guardar
            item {
                Button(
                    onClick = { navController.navigate(Screen.ViewRoute.route) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.save_points),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}