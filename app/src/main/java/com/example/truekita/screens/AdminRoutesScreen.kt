package com.example.truekita.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

data class AdminRouteUi(
    val id: String = "",
    val nombreConductor: String = "",
    val trayecto: String = "",
    val origen: String = "",
    val destino: String = "",
    val horaSalida: String = "",
    val fechaSalida: String = "",
    val lugaresDisponibles: String = "",
    val precio: String = "",
    val tipo: String = "",
    val conductorCorreo: String = ""
)

@Composable
fun AdminRoutesScreen(
    navController: NavController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var rutas by remember {
        mutableStateOf<List<AdminRouteUi>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var rutaAEliminar by remember {
        mutableStateOf<AdminRouteUi?>(null)
    }

    var eliminando by remember {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        val listener = db.collection("rutas")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    cargando = false

                    Toast.makeText(
                        context,
                        "Error al cargar rutas: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addSnapshotListener
                }

                rutas = snapshot?.documents
                    ?.sortedByDescending { document ->
                        document.getTimestamp("fechaPublicacion")?.toDate()?.time
                            ?: document.getTimestamp("fechaCreacion")?.toDate()?.time
                            ?: document.getTimestamp("fecha")?.toDate()?.time
                            ?: 0L
                    }
                    ?.map { document ->
                        val origen = document.getString("origen")
                            ?: document.getString("puntoSalida")
                            ?: document.getString("salida")
                            ?: ""

                        val destino = document.getString("destino")
                            ?: document.getString("puntoLlegada")
                            ?: document.getString("llegada")
                            ?: ""

                        val trayectoFirestore = document.getString("trayecto")
                            ?: document.getString("ruta")
                            ?: document.getString("nombreRuta")
                            ?: ""

                        val trayectoFinal = when {
                            trayectoFirestore.isNotBlank() -> trayectoFirestore
                            origen.isNotBlank() && destino.isNotBlank() -> "$origen → $destino"
                            origen.isNotBlank() -> origen
                            destino.isNotBlank() -> destino
                            else -> "Ruta sin trayecto"
                        }

                        AdminRouteUi(
                            id = document.id,
                            nombreConductor = document.getString("nombreConductor")
                                ?: document.getString("conductorNombre")
                                ?: document.getString("nombre")
                                ?: "",
                            trayecto = trayectoFinal,
                            origen = origen,
                            destino = destino,
                            horaSalida = document.getString("horaSalida")
                                ?: document.getString("hora")
                                ?: "",
                            fechaSalida = document.getString("fechaSalida")
                                ?: document.getString("fecha")
                                ?: "",
                            lugaresDisponibles = document.get("lugaresDisponibles")?.toString()
                                ?: document.get("lugares")?.toString()
                                ?: document.get("asientosDisponibles")?.toString()
                                ?: document.get("asientos")?.toString()
                                ?: "",
                            precio = document.get("precio")?.toString()
                                ?: document.get("costo")?.toString()
                                ?: "",
                            tipo = document.getString("tipo")
                                ?: document.getString("tipoRuta")
                                ?: "",
                            conductorCorreo = document.getString("conductorCorreo")
                                ?: document.getString("correoConductor")
                                ?: document.getString("email")
                                ?: document.getString("usuarioCorreo")
                                ?: ""
                        )
                    } ?: emptyList()

                cargando = false
            }

        onDispose {
            listener.remove()
        }
    }

    fun eliminarRuta(ruta: AdminRouteUi) {
        eliminando = true

        db.collection("rutas")
            .document(ruta.id)
            .delete()
            .addOnSuccessListener {
                eliminando = false
                rutaAEliminar = null

                Toast.makeText(
                    context,
                    "Ruta eliminada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                eliminando = false

                Toast.makeText(
                    context,
                    "Error al eliminar ruta: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    rutaAEliminar?.let { ruta ->
        AlertDialog(
            onDismissRequest = {
                if (!eliminando) {
                    rutaAEliminar = null
                }
            },
            title = {
                Text(text = "Eliminar ruta")
            },
            text = {
                Text(
                    text = "¿Seguro que quieres eliminar esta ruta?\n\n${ruta.trayecto}"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        eliminarRuta(ruta)
                    },
                    enabled = !eliminando
                ) {
                    Text(
                        text = if (eliminando) {
                            "Eliminando..."
                        } else {
                            "Eliminar"
                        },
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        rutaAEliminar = null
                    },
                    enabled = !eliminando
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver"
                )
            }

            Text(
                text = "Rutas publicadas",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            Text(
                text = "Aquí puedes ver y eliminar las rutas registradas por los usuarios.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(18.dp))

            when {
                cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                rutas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay rutas publicadas en Firebase",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(rutas, key = { ruta -> ruta.id }) { ruta ->
                            AdminRouteCard(
                                ruta = ruta,
                                onEliminar = {
                                    rutaAEliminar = ruta
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRouteCard(
    ruta: AdminRouteUi,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color(0xFF0D47A1)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = ruta.trayecto.ifBlank { "Ruta sin trayecto" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Conductor: ${
                    ruta.nombreConductor.ifBlank {
                        ruta.conductorCorreo.ifBlank { "Sin conductor" }
                    }
                }",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            if (ruta.conductorCorreo.isNotBlank()) {
                Text(
                    text = "Correo: ${ruta.conductorCorreo}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            if (ruta.fechaSalida.isNotBlank()) {
                Text(
                    text = "Fecha: ${ruta.fechaSalida}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            Text(
                text = "Salida: ${ruta.horaSalida.ifBlank { "Sin hora" }}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Lugares disponibles: ${ruta.lugaresDisponibles.ifBlank { "No especificado" }}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            if (ruta.precio.isNotBlank()) {
                Text(
                    text = "Precio: ${ruta.precio}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            Text(
                text = "Tipo: ${ruta.tipo.ifBlank { "Sin tipo" }}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onEliminar,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = "Eliminar ruta")
            }
        }
    }
}