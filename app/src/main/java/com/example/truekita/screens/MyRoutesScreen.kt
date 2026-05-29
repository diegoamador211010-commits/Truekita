package com.example.truekita.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class MyRouteData(
    val id: String = "",
    val nombreConductor: String = "",
    val trayecto: String = "",
    val horaSalida: String = "",
    val lugaresDisponibles: String = "",
    val tipo: String = "",
    val conductorUid: String = ""
)

@Composable
fun MyRoutesScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var routes by remember { mutableStateOf<List<MyRouteData>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var routeToEdit by remember { mutableStateOf<MyRouteData?>(null) }
    var routeToDelete by remember { mutableStateOf<MyRouteData?>(null) }

    DisposableEffect(currentUser?.uid) {
        if (currentUser == null) {
            loading = false
            onDispose { }
        } else {
            val listener = db.collection("rutas")
                .whereEqualTo("conductorUid", currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        loading = false
                        Toast.makeText(
                            context,
                            "Error al cargar rutas: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addSnapshotListener
                    }

                    routes = snapshot?.documents
                        ?.sortedByDescending { document ->
                            document.getTimestamp("fechaPublicacion")?.toDate()?.time ?: 0L
                        }
                        ?.map { document ->
                            MyRouteData(
                                id = document.id,
                                nombreConductor = document.getString("nombreConductor") ?: "",
                                trayecto = document.getString("trayecto") ?: "",
                                horaSalida = document.getString("horaSalida") ?: "",
                                lugaresDisponibles = document.getString("lugaresDisponibles") ?: "",
                                tipo = document.getString("tipo") ?: "",
                                conductorUid = document.getString("conductorUid") ?: ""
                            )
                        } ?: emptyList()

                    loading = false
                }

            onDispose {
                listener.remove()
            }
        }
    }

    routeToEdit?.let { route ->
        EditRouteDialog(
            route = route,
            onDismiss = {
                routeToEdit = null
            },
            onSave = { nombreConductor, trayecto, horaSalida, lugaresDisponibles ->
                val lugaresNumero = lugaresDisponibles.toIntOrNull()

                if (lugaresNumero == null || lugaresNumero < 0) {
                    Toast.makeText(
                        context,
                        "Los lugares disponibles deben ser un número válido",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@EditRouteDialog
                }

                db.collection("rutas")
                    .document(route.id)
                    .update(
                        mapOf(
                            "nombreConductor" to nombreConductor,
                            "trayecto" to trayecto,
                            "horaSalida" to horaSalida,
                            "lugaresDisponibles" to lugaresDisponibles,
                            "fechaActualizacion" to Timestamp.now()
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Ruta actualizada",
                            Toast.LENGTH_SHORT
                        ).show()

                        routeToEdit = null
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(
                            context,
                            "Error al editar ruta: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        )
    }

    routeToDelete?.let { route ->
        AlertDialog(
            onDismissRequest = {
                routeToDelete = null
            },
            title = {
                Text(text = "Eliminar ruta")
            },
            text = {
                Text(text = "¿Seguro que quieres eliminar esta ruta?\n\n${route.trayecto}")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        db.collection("rutas")
                            .document(route.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Ruta eliminada",
                                    Toast.LENGTH_SHORT
                                ).show()

                                routeToDelete = null
                            }
                            .addOnFailureListener { error ->
                                Toast.makeText(
                                    context,
                                    "Error al eliminar ruta: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        routeToDelete = null
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.my_routes),
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
                .padding(16.dp)
        ) {
            when {
                currentUser == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Inicia sesión para ver tus rutas",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }

                loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                routes.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no tienes rutas publicadas",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(routes, key = { it.id }) { route ->
                            MyRouteCard(
                                route = route,
                                navController = navController,
                                onEditClick = {
                                    routeToEdit = route
                                },
                                onDeleteClick = {
                                    routeToDelete = route
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    navController.navigate(Screen.PublishRoute.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF98EE99)
                )
            ) {
                Text(
                    text = stringResource(id = R.string.add_route),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MyRouteCard(
    route: MyRouteData,
    navController: NavController,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val lugares = route.lugaresDisponibles.toIntOrNull() ?: 0
    val isAvailable = lugares > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (route.nombreConductor.isNotBlank()) {
                    route.nombreConductor
                } else {
                    "Sin conductor"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (route.trayecto.isNotBlank()) {
                    "Ruta: ${route.trayecto}"
                } else {
                    "Ruta: Sin trayecto"
                },
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = if (route.horaSalida.isNotBlank()) {
                    "Salida: ${route.horaSalida}"
                } else {
                    "Salida: Sin hora"
                },
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = Color(0xFFEEEEEE),
                shape = RoundedCornerShape(12.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    navController.navigate("${Screen.MapasRuta.route}/${route.id}")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D47A1)
                )
            ) {
                Text(
                    text = stringResource(id = R.string.view_route),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    navController.navigate(Screen.RouteRequests.createRoute(route.id))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF81C784)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Ver solicitudes",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D47A1)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Editar",
                        color = Color.White
                    )
                }

                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Eliminar",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EditRouteDialog(
    route: MyRouteData,
    onDismiss: () -> Unit,
    onSave: (
        nombreConductor: String,
        trayecto: String,
        horaSalida: String,
        lugaresDisponibles: String
    ) -> Unit
) {
    var nombreConductor by remember { mutableStateOf(route.nombreConductor) }
    var trayecto by remember { mutableStateOf(route.trayecto) }
    var horaSalida by remember { mutableStateOf(route.horaSalida) }
    var lugaresDisponibles by remember { mutableStateOf(route.lugaresDisponibles) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Editar ruta")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nombreConductor,
                    onValueChange = {
                        nombreConductor = it
                    },
                    label = {
                        Text("Nombre del conductor")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = trayecto,
                    onValueChange = {
                        trayecto = it
                    },
                    label = {
                        Text("Trayecto")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = horaSalida,
                    onValueChange = {
                        horaSalida = it
                    },
                    label = {
                        Text("Hora de salida")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = lugaresDisponibles,
                    onValueChange = {
                        lugaresDisponibles = it.filter { char -> char.isDigit() }
                    },
                    label = {
                        Text("Lugares disponibles")
                    },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (
                        nombreConductor.isBlank() ||
                        trayecto.isBlank() ||
                        horaSalida.isBlank() ||
                        lugaresDisponibles.isBlank()
                    ) {
                        return@TextButton
                    }

                    onSave(
                        nombreConductor,
                        trayecto,
                        horaSalida,
                        lugaresDisponibles
                    )
                }
            ) {
                Text(text = "Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "Cancelar")
            }
        }
    )
}