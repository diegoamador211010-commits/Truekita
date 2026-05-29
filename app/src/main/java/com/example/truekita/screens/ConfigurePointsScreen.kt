package com.example.truekita.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ConfigurePointsScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser

    var placeName by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf("") }
    var estimatedTime by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    val lightBlue = Color(0xFFE3F2FD)
    val darkBlue = Color(0xFF0D47A1)

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.configure_meeting_points_title),
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

        val context = androidx.compose.ui.platform.LocalContext.current

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(lightBlue)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = darkBlue
                            )

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
            }

            item {
                OutlinedTextField(
                    value = placeName,
                    onValueChange = {
                        placeName = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(stringResource(id = R.string.place_name_hint))
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = darkBlue,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = seats,
                    onValueChange = { value ->
                        seats = value.filter { it.isDigit() }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(stringResource(id = R.string.seats_available_hint))
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = darkBlue,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = estimatedTime,
                    onValueChange = {
                        estimatedTime = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(stringResource(id = R.string.estimated_arrival_hint))
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = darkBlue,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEAF4FF)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.points_added_progress),
                            fontWeight = FontWeight.Medium
                        )

                        LinearProgressIndicator(
                            progress = {
                                val filled = listOf(placeName, seats, estimatedTime)
                                    .count { it.isNotBlank() }

                                filled / 3f
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            color = darkBlue
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (currentUser == null) {
                            Toast.makeText(
                                context,
                                "Debes iniciar sesión",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (placeName.isBlank() || seats.isBlank() || estimatedTime.isBlank()) {
                            Toast.makeText(
                                context,
                                "Completa todos los campos",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        val cuposNumero = seats.toIntOrNull()

                        if (cuposNumero == null || cuposNumero <= 0) {
                            Toast.makeText(
                                context,
                                "Los cupos deben ser un número mayor a 0",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        saving = true

                        val data = hashMapOf(
                            "nombre" to placeName.trim(),
                            "cupos" to cuposNumero,
                            "horaLlegada" to estimatedTime.trim(),
                            "activo" to true,
                            "creadoEn" to Timestamp.now(),
                            "creadoPorUid" to currentUser.uid,
                            "creadoPorCorreo" to (currentUser.email ?: "")
                        )

                        db.collection("puntosEncuentro")
                            .add(data)
                            .addOnSuccessListener {
                                saving = false

                                Toast.makeText(
                                    context,
                                    "Punto guardado correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()

                                navController.navigate(Screen.MeetingPoints.route) {
                                    popUpTo(Screen.ConfigurePoints.route) {
                                        inclusive = true
                                    }
                                }
                            }
                            .addOnFailureListener { error ->
                                saving = false

                                Toast.makeText(
                                    context,
                                    "Error al guardar: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    },
                    enabled = !saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF98EE99)
                    )
                ) {
                    Text(
                        text = if (saving) {
                            "Guardando..."
                        } else {
                            stringResource(id = R.string.save_points)
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}