package com.example.truekita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R

@Composable
fun SavePasswordScreen(navController: NavController) {
    val lightBlue = Color(0xFFE3F2FD)
    val successGreen = Color(0xFF81C784)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBlue)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- ICONO DE ÉXITO VISUAL ---
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = successGreen.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = successGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- TEXTO TRADUCIDO ---
        Text(
            text = stringResource(id = R.string.password_saved_success),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- BOTÓN IR AL LOGIN (Ruta corregida) ---
        Button(
            onClick = {
                // Usamos la ruta como String para evitar errores de referencia
                navController.navigate("login_screen") {
                    popUpTo("login_screen") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
        ) {
            Text(
                text = stringResource(id = R.string.go_to_login),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}