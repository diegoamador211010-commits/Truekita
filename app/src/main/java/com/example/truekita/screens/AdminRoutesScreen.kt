package com.example.truekita.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class AdminRouteUi(
    val id: String = "",
    val nombreConductor: String = "",
    val trayecto: String = "",
    val horaSalida: String = "",
    val lugaresDisponibles: String = "",
    val tipo: String = "",
    val conductorCorreo: String = ""
)

@Composable
fun AdminRoutesScreen(
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()

    var rutas by remember {
        mutableStateOf<List<AdminRouteUi>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(true)
    }

    var eliminando by remember {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        val listener = db.collection("rutas")
            .orderBy("fechaPublicacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    cargando = false
                    return@addSnapshotListener
                }

                rutas = snapshot?.documents?.map { document ->
                    AdminRouteUi(
                        id = document.id,
                        nombreConductor = document.getString("nombreConductor") ?: "",
                        trayecto = document.getString("trayecto") ?: "",
                        horaSalida = document.getString("horaSalida") ?: "",
                        lugaresDisponibles = document.getString("lugaresDisponibles") ?: "",
                        tipo = document.getString("tipo") ?: "",
                        conductorCorreo = document.getString("conductorCorreo") ?: ""
                    )
                } ?: emptyList()

                cargando = false
            }

        onDispose {
            listener.remove()
        }
    }

    fun eliminarRuta(routeId: String) {
        eliminando = true

        db.collection("rutas")
            .document(routeId)
            .delete()
            .addOnCompleteListener {
                eliminando = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD))
            .padding(18.dp)
    ) {
        Row(
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
                text = "Viajes publicados",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

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
                        text = "No hay viajes publicados",
                        color = Color.Gray,
                        fontSize = 16.sp
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
                            eliminando = eliminando,
                            onEliminar = {
                                eliminarRuta(ruta.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRouteCard(
    ruta: AdminRouteUi,
    eliminando: Boolean,
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
            Text(
                text = ruta.trayecto.ifBlank { "Ruta sin trayecto" },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Conductor: ${
                    ruta.nombreConductor.ifBlank {
                        ruta.conductorCorreo.ifBlank { "Sin conductor" }
                    }
                }",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Correo: ${ruta.conductorCorreo.ifBlank { "Sin correo" }}",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Text(
                text = "Salida: ${ruta.horaSalida.ifBlank { "Sin hora" }}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Lugares: ${ruta.lugaresDisponibles.ifBlank { "0" }}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Tipo: ${ruta.tipo.ifBlank { "Sin tipo" }}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onEliminar,
                enabled = !eliminando,
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

                Text(
                    text = if (eliminando) {
                        "Eliminando..."
                    } else {
                        "Eliminar viaje"
                    }
                )
            }
        }
    }
}