package com.example.truekita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource // IMPORTANTE
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen

@Composable
fun ProductDetailScreen(navController: NavController, productTitle: String) {
    // Obtenemos las versiones traducidas de los nombres para la comparación
    val calcName = stringResource(id = R.string.calculator)
    val arduinoName = stringResource(id = R.string.arduino)
    val chargerName = stringResource(id = R.string.iphone_charger)

    // SIMULACIÓN DE DATOS (Filtramos según el título traducido)
    val (price, seller, imageRes, type) = when (productTitle) {
        calcName -> listOf(stringResource(R.string.price_200), stringResource(R.string.seller_alexis), R.drawable.calculadora, stringResource(R.string.sale_caps))
        arduinoName -> listOf(stringResource(R.string.price_150), stringResource(R.string.seller_nestor), R.drawable.arduino, stringResource(R.string.exchange_caps))
        chargerName -> listOf(stringResource(R.string.price_120), stringResource(R.string.seller_abraham), R.drawable.cargador_iphone, stringResource(R.string.rent_caps))
        else -> listOf("---", "Usuario ITA", R.drawable.placeholder, "---")
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFE3F2FD)).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Internacionalizado
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                }
                Text(
                    text = stringResource(id = R.string.more_options), // O una etiqueta de "Detalle"
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.width(48.dp))
            }

            // Imagen
            Card(Modifier.fillMaxWidth().height(180.dp).padding(vertical = 10.dp), shape = RoundedCornerShape(16.dp)) {
                Image(painter = painterResource(id = imageRes as Int), contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }

            // Nombre del Producto (Ya viene dinámico)
            DetailBox(text = "${stringResource(id = R.string.product_calculator).split(":")[0]}: $productTitle")

            // Estado y Tipo (VENTA/RENTA/INTERCAMBIO)
            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(id = R.string.available), fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.width(6.dp)); Box(Modifier.size(10.dp).background(Color(0xFF81C784), CircleShape))
                    }
                    Surface(Modifier.align(Alignment.CenterHorizontally), color = Color(0xFFE0E0E0), shape = RoundedCornerShape(4.dp)) {
                        Text(type as String, Modifier.padding(horizontal = 20.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("${stringResource(id = R.string.name_label)}: $seller", fontWeight = FontWeight.Bold)
            Text("${stringResource(id = R.string.rating)}: $price", color = Color(0xFF1976D2), fontSize = 18.sp)

            // Estrellas
            Row(Modifier.padding(vertical = 8.dp)) {
                repeat(5) { Icon(Icons.Default.Star, null, tint = Color(0xFFFFD600), modifier = Modifier.size(28.dp)) }
            }

            Spacer(Modifier.weight(1f))

            // Botón Mandar Mensaje Internacionalizado
            Button(
                onClick = { navController.navigate(Screen.ChatList.route) },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5F0B5)),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(stringResource(id = R.string.send_message), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DetailBox(text: String) {
    Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp), color = Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp)) {
        Text(text, Modifier.padding(12.dp), textAlign = TextAlign.Center)
    }
}