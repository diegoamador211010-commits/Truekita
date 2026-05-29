package com.example.truekita.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PublishRouteScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var nombreConductor by rememberSaveable { mutableStateOf("") }
    var trayecto by rememberSaveable { mutableStateOf("") }
    var horaSalida by rememberSaveable { mutableStateOf("") }
    var lugaresDisponibles by rememberSaveable { mutableStateOf("") }
    var tipo by rememberSaveable { mutableStateOf("") }
    var saving by rememberSaveable { mutableStateOf(false) }

    var ubicacionNombre by rememberSaveable { mutableStateOf("") }
    var latitud by rememberSaveable { mutableDoubleStateOf(0.0) }
    var longitud by rememberSaveable { mutableDoubleStateOf(0.0) }

    var expandedTipo by remember { mutableStateOf(false) }

    val tipoOptions = listOf(
        "Ruta escolar",
        "Transporte compartido",
        "Viaje especial"
    )

    val backStackEntry = navController.currentBackStackEntry
    val savedStateHandle = backStackEntry?.savedStateHandle

    LaunchedEffect(Unit) {
        val nombre = savedStateHandle?.get<String>("ubicacionNombre") ?: ""
        val lat = savedStateHandle?.get<Double>("latitud") ?: 0.0
        val lng = savedStateHandle?.get<Double>("longitud") ?: 0.0

        if (nombre.isNotBlank()) {
            ubicacionNombre = nombre
            latitud = lat
            longitud = lng
        }
    }

    DisposableEffect(backStackEntry) {
        val lifecycle = backStackEntry?.lifecycle

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val nombre = savedStateHandle?.get<String>("ubicacionNombre") ?: ""
                val lat = savedStateHandle?.get<Double>("latitud") ?: 0.0
                val lng = savedStateHandle?.get<Double>("longitud") ?: 0.0

                if (nombre.isNotBlank()) {
                    ubicacionNombre = nombre
                    latitud = lat
                    longitud = lng
                }
            }
        }

        lifecycle?.addObserver(observer)

        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.publish_route),
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
                .background(Color(0xFFE3F2FD))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TruekitaRouteInputField(
                value = nombreConductor,
                onValueChange = { nombreConductor = it },
                label = "Nombre del conductor"
            )

            TruekitaRouteInputField(
                value = trayecto,
                onValueChange = { trayecto = it },
                label = "Trayecto / Ruta"
            )

            TruekitaRouteInputField(
                value = horaSalida,
                onValueChange = { horaSalida = it },
                label = "Hora de salida"
            )

            TruekitaRouteInputField(
                value = lugaresDisponibles,
                onValueChange = { value ->
                    lugaresDisponibles = value.filter { char -> char.isDigit() }
                },
                label = "Lugares disponibles",
                keyboardType = KeyboardType.Number
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        expandedTipo = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = if (tipo.isBlank()) "Seleccionar tipo de ruta" else tipo,
                        color = if (tipo.isBlank()) Color.Gray else Color.Black
                    )
                }

                DropdownMenu(
                    expanded = expandedTipo,
                    onDismissRequest = {
                        expandedTipo = false
                    }
                ) {
                    tipoOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(text = option)
                            },
                            onClick = {
                                tipo = option
                                expandedTipo = false
                            }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Ubicación seleccionada",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (ubicacionNombre.isNotBlank()) {
                            ubicacionNombre
                        } else {
                            "Aún no seleccionas ubicación"
                        },
                        color = Color.Gray
                    )

                    if (latitud != 0.0 || longitud != 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Lat: $latitud, Lng: $longitud",
                            color = Color.Gray
                        )
                    }
                }
            }

            Button(
                onClick = {
                    navController.navigate(Screen.LocationPicker.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D47A1)
                )
            ) {
                Text(
                    text = "Seleccionar ubicación",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    if (currentUser == null) {
                        Toast.makeText(
                            context,
                            "Debes iniciar sesión para publicar una ruta",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (
                        nombreConductor.isBlank() ||
                        trayecto.isBlank() ||
                        horaSalida.isBlank() ||
                        lugaresDisponibles.isBlank() ||
                        tipo.isBlank()
                    ) {
                        Toast.makeText(
                            context,
                            "Completa todos los campos",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (ubicacionNombre.isBlank()) {
                        Toast.makeText(
                            context,
                            "Selecciona una ubicación",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    val lugaresNumero = lugaresDisponibles.toIntOrNull()

                    if (lugaresNumero == null || lugaresNumero <= 0) {
                        Toast.makeText(
                            context,
                            "Los lugares deben ser mayor a 0",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    saving = true

                    val routeData = hashMapOf(
                        "nombreConductor" to nombreConductor.trim(),
                        "trayecto" to trayecto.trim(),
                        "horaSalida" to horaSalida.trim(),
                        "lugaresDisponibles" to lugaresDisponibles.trim(),
                        "tipo" to tipo,
                        "ubicacionNombre" to ubicacionNombre,
                        "latitud" to latitud,
                        "longitud" to longitud,
                        "conductorUid" to currentUser.uid,
                        "conductorCorreo" to (currentUser.email ?: ""),
                        "fechaPublicacion" to Timestamp.now(),
                        "estado" to "disponible"
                    )

                    db.collection("rutas")
                        .add(routeData)
                        .addOnSuccessListener {
                            saving = false

                            Toast.makeText(
                                context,
                                "Ruta publicada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()

                            navController.navigate(Screen.MyRoutes.route) {
                                popUpTo(Screen.PublishRoute.route) {
                                    inclusive = true
                                }
                            }
                        }
                        .addOnFailureListener { error ->
                            saving = false

                            Toast.makeText(
                                context,
                                "Error al guardar ruta: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                },
                enabled = !saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA5F0B5)
                )
            ) {
                Text(
                    text = if (saving) "Publicando..." else "Publicar ruta",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TruekitaRouteInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(label)
        },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF0D47A1),
            unfocusedBorderColor = Color.LightGray
        )
    )
}