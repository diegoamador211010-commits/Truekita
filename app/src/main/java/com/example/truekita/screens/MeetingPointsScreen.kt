package com.example.truekita.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // IMPORTANTE
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.BottomNavBar
import java.util.*

@Composable
fun MeetingPointsScreen(navController: NavController, title: String) {
    val context = LocalContext.current
    val lightBlue = Color(0xFFE3F2FD)
    val darkBlue = Color(0xFF0D47A1)
    val grayButton = Color(0xFFD9D9D9)
    val greenConfirm = Color(0xFFA5F0B5)

    // --- ESTADOS TRADUCIDOS ---
    val defaultDateText = stringResource(id = R.string.select_date)
    var selectedPlace by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(defaultDateText) }
    var selectedHour by remember { mutableStateOf("") }

    // Calendario Nativo
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

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(lightBlue)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HEADER
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                }
                Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // MAPA
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(id = R.string.map_placeholder_title), fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text(stringResource(id = R.string.map_placeholder_subtitle), fontSize = 12.sp, color = Color.LightGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // BOTÓN CALENDARIO
            Button(
                onClick = { datePickerDialog.show() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(selectedDate != defaultDateText) darkBlue else grayButton
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(selectedDate, color = if(selectedDate != defaultDateText) Color.White else Color.Black)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // LUGAR SELECCIONADO
            Surface(
                color = grayButton,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(0.8f).height(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if(selectedPlace.isEmpty()) stringResource(id = R.string.meeting_points) else selectedPlace,
                        fontSize = 14.sp, color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // SELECCIÓN DE LUGAR (Usando las etiquetas del XML)
            val lugares = listOf(
                stringResource(id = R.string.cafeteria),
                stringResource(id = R.string.auditorium),
                stringResource(id = R.string.language_center)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                lugares.forEach { lugar ->
                    FilterChip(
                        selected = selectedPlace == lugar,
                        onClick = { selectedPlace = lugar },
                        label = { Text(lugar) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = darkBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // --- HORARIOS ---
            if (selectedPlace.isNotEmpty()) {
                Text(
                    text = stringResource(id = R.string.select_time) + " ($selectedPlace):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val horas = (7..18).map { hour ->
                    val h = if (hour > 12) hour - 12 else hour
                    val ampm = if (hour >= 12) "PM" else "AM"
                    "$h:00 $ampm"
                }

                Column {
                    horas.chunked(3).forEach { fila ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            fila.forEach { hora ->
                                Button(
                                    onClick = { selectedHour = hora },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if(selectedHour == hora) darkBlue else Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.width(100.dp).padding(2.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                                ) {
                                    Text(hora, fontSize = 10.sp, color = if(selectedHour == hora) Color.White else Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BOTÓN CONFIRMAR
            Button(
                onClick = {
                    if(selectedPlace.isNotEmpty() && selectedHour.isNotEmpty() && selectedDate != defaultDateText) {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(selectedPlace.isNotEmpty() && selectedHour.isNotEmpty()) greenConfirm else Color.LightGray
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(stringResource(id = R.string.confirm), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}