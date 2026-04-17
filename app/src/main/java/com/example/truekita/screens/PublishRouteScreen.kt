package com.example.truekita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishRouteScreen(navController: NavController) {
    // Estados para los inputs
    var driverName by remember { mutableStateOf("") }
    var routePath by remember { mutableStateOf("") }
    var departureTime by remember { mutableStateOf("") }
    var availableSpots by remember { mutableStateOf("") }

    val backgroundColor = Color(0xFFE3F2FD) // Azul claro ITA
    val buttonGreen = Color(0xFF98EE99)

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Publicar Ruta",
                showBack = true,
                onBackClick = { navController.popBackStack() }
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
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text(
                        text = "Completa la información de tu ruta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Campos de texto personalizados
                item {
                    RouteInputField(
                        value = driverName,
                        onValueChange = { driverName = it },
                        label = "Nombre del conductor"
                    )
                }

                item {
                    RouteInputField(
                        value = routePath,
                        onValueChange = { routePath = it },
                        label = "Trayecto (Ej: Fracc. Ojocaliente -> ITA)"
                    )
                }

                item {
                    RouteInputField(
                        value = departureTime,
                        onValueChange = { departureTime = it },
                        label = "Hora de salida"
                    )
                }

                item {
                    RouteInputField(
                        value = availableSpots,
                        onValueChange = { availableSpots = it },
                        label = "Lugares disponibles"
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // BOTÓN FINAL DE PUBLICAR
                item {
                    Button(
                        onClick = {
                            // Aquí iría la lógica para guardar en la base de datos

                            // Navegamos de regreso a la lista de rutas
                            navController.navigate(Screen.ViewRoute.route) {
                                popUpTo(Screen.Home.route) // Limpia el historial hasta el inicio
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonGreen)
                    ) {
                        Text(
                            text = "Publicar ruta",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                item {
                    Text(
                        text = "Recuerda ser puntual con tus compañeros.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteInputField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF0D47A1),
            unfocusedBorderColor = Color.LightGray
        ),
        singleLine = true
    )
}