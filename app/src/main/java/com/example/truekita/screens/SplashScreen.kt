package com.example.truekita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController
) {
    // Temporizador para saltar al Login
    LaunchedEffect(Unit) {
        delay(1800) // 1.8 segundos de gloria para tu logo
        navController.navigate(Screen.Login.route) {
            // Limpiamos el Splash del historial para que no se pueda regresar
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF7ADFF2),
                            Color(0xFFEAFBF0),
                            Color(0xFFA9F0D1)
                        )
                    )
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contenedor del Logo con efecto traslúcido
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.4f),
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_truequita_logo),
                    contentDescription = stringResource(id = R.string.app_logo_placeholder),
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.size(24.dp))

            // Nombre de la App desde el strings.xml
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF3AA9B5),
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}