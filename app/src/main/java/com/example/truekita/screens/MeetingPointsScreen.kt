package com.example.truekita.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.truekita.components.BottomNavBar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

data class PuntoEncuentroDb(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val cupos: Int = 0,
    val horaLlegada: String = "",
    val activo: Boolean = true
)

@Composable
fun MeetingPointsScreen(
    navController: NavController,
    title: String
) {
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    val lightBlue = Color(0xFFE3F2FD)
    val darkBlue = Color(0xFF0D47A1)
    val grayButton = Color(0xFFD9D9D9)
    val greenConfirm = Color(0xFFA5F0B5)

    val defaultDateText = stringResource(id = R.string.select_date)

    val puntosRespaldo = listOf(
        PuntoEncuentroDb(
            id = "estacionamiento",
            nombre = "Estacionamiento",
            descripcion = "Punto de encuentro en el estacionamiento principal.",
            cupos = 10,
            horaLlegada = "7:00 AM - 9:00 PM",
            activo = true
        ),
        PuntoEncuentroDb(
            id = "auditorio",
            nombre = "Auditorio",
            descripcion = "Punto de encuentro frente al auditorio.",
            cupos = 10,
            horaLlegada = "7:00 AM - 9:00 PM",
            activo = true
        ),
        PuntoEncuentroDb(
            id = "cafeteria",
            nombre = "Cafetería",
            descripcion = "Punto de encuentro en el área de cafetería.",
            cupos = 10,
            horaLlegada = "7:00 AM - 9:00 PM",
            activo = true
        )
    )

    var puntosEncuentro by remember { mutableStateOf(puntosRespaldo) }
    var loading by remember { mutableStateOf(true) }

    var selectedPlace by remember { mutableStateOf<PuntoEncuentroDb?>(null) }
    var selectedDate by remember { mutableStateOf(defaultDateText) }
    var selectedHour by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = db.collection("puntosEncuentro")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    loading = false

                    // Si Firebase falla, se quedan los puntos fijos.
                    puntosEncuentro = puntosRespaldo

                    Toast.makeText(
                        context,
                        "Usando puntos de encuentro locales",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addSnapshotListener
                }

                val puntosFirebase = snapshot?.documents
                    ?.map { document ->
                        PuntoEncuentroDb(
                            id = document.id,
                            nombre = document.getString("nombre") ?: "",
                            descripcion = document.getString("descripcion") ?: "",
                            cupos = document.getLong("cupos")?.toInt() ?: 10,
                            horaLlegada = document.getString("horaLlegada") ?: "7:00 AM - 9:00 PM",
                            activo = document.getBoolean("activo") ?: true
                        )
                    }
                    ?.filter { it.activo && it.nombre.isNotBlank() }
                    ?.sortedBy { it.nombre }
                    ?: emptyList()

                puntosEncuentro = if (puntosFirebase.isNotEmpty()) {
                    puntosFirebase
                } else {
                    puntosRespaldo
                }

                loading = false
            }

        onDispose {
            listener.remove()
        }
    }

    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val horas = (7..21).map { hour ->
        val h = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        val ampm = if (hour >= 12) "PM" else "AM"
        "$h:00 $ampm"
    }

    val formComplete =
        selectedPlace != null &&
                selectedHour.isNotBlank() &&
                selectedDate != defaultDateText

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(lightBlue)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }

                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = darkBlue,
                            modifier = Modifier.size(42.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(id = R.string.map_placeholder_title),
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        Text(
                            text = "Estacionamiento, Auditorio y Cafetería",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Button(
                onClick = {
                    datePickerDialog.show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedDate != defaultDateText) darkBlue else grayButton
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = selectedDate,
                    color = if (selectedDate != defaultDateText) Color.White else Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = grayButton,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(44.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedPlace?.nombre ?: stringResource(id = R.string.meeting_points),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            if (loading) {
                CircularProgressIndicator()
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(puntosEncuentro, key = { it.id }) { punto ->
                        FilterChip(
                            selected = selectedPlace?.id == punto.id,
                            onClick = {
                                selectedPlace = punto
                                selectedHour = ""
                            },
                            label = {
                                Text(punto.nombre)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = darkBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (selectedPlace != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = selectedPlace!!.descripcion,
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )

                Text(
                    text = "Horario disponible: 7:00 AM - 9:00 PM",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            if (selectedPlace != null) {
                Text(
                    text = stringResource(id = R.string.select_time) + " (${selectedPlace!!.nombre}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    horas.chunked(3).forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            fila.forEach { hora ->
                                Button(
                                    onClick = {
                                        selectedHour = hora
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedHour == hora) darkBlue else Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .width(100.dp)
                                        .padding(2.dp),
                                    border = BorderStroke(1.dp, Color.LightGray)
                                ) {
                                    Text(
                                        text = hora,
                                        fontSize = 10.sp,
                                        color = if (selectedHour == hora) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (selectedPlace == null) {
                        Toast.makeText(
                            context,
                            "Selecciona un punto de encuentro",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (selectedDate == defaultDateText) {
                        Toast.makeText(
                            context,
                            "Selecciona una fecha",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (selectedHour.isBlank()) {
                        Toast.makeText(
                            context,
                            "Selecciona una hora",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    val punto = selectedPlace!!

                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("zonaEntrega", punto.nombre)

                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("fechaEntrega", selectedDate)

                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("horaEntrega", selectedHour)

                    if (currentUser == null) {
                        Toast.makeText(
                            context,
                            "Punto seleccionado",
                            Toast.LENGTH_SHORT
                        ).show()

                        navController.popBackStack()
                        return@Button
                    }

                    saving = true

                    val data = hashMapOf(
                        "usuarioUid" to currentUser.uid,
                        "usuarioCorreo" to (currentUser.email ?: ""),
                        "puntoId" to punto.id,
                        "puntoNombre" to punto.nombre,
                        "fechaSeleccionada" to selectedDate,
                        "horaSeleccionada" to selectedHour,
                        "creadoEn" to Timestamp.now()
                    )

                    db.collection("seleccionesEntrega")
                        .add(data)
                        .addOnSuccessListener {
                            saving = false

                            Toast.makeText(
                                context,
                                "Punto de encuentro guardado",
                                Toast.LENGTH_SHORT
                            ).show()

                            navController.popBackStack()
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
                    .fillMaxWidth(0.85f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (formComplete) greenConfirm else Color.LightGray
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = if (saving) "Guardando..." else stringResource(id = R.string.confirm),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}