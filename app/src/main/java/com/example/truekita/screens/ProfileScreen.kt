package com.example.truekita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp // Ajuste de icono
import androidx.compose.material.icons.automirrored.filled.Help // Ajuste de icono
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource // IMPORTANTE
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen

@Composable
fun ProfileScreen(navController: NavController) {
    val backgroundColor = Color(0xFFE3F2FD) // Azul claro ITA

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // --- FOTO DE PERFIL ---
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- INFORMACIÓN DEL USUARIO ---
            Text(
                text = "Diego", // Puedes dejarlo fijo o usar un string si gustas
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Estudiante ITA - TICs", // Este podrías traducirlo si fuera necesario
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- OPCIONES DE MENÚ TRADUCIDAS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Mis Publicaciones
                    ProfileOptionUi(
                        icon = Icons.Default.Inventory,
                        text = stringResource(id = R.string.my_posts),
                        onClick = { navController.navigate(Screen.MyPosts.route) }
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    // Mis Rutas
                    ProfileOptionUi(
                        icon = Icons.Default.DirectionsCar,
                        text = stringResource(id = R.string.my_routes),
                        onClick = { navController.navigate(Screen.MyRoutes.route) }
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    // Configuración
                    ProfileOptionUi(
                        icon = Icons.Default.Settings,
                        text = stringResource(id = R.string.settings),
                        onClick = { navController.navigate(Screen.Settings.route) }
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    // Ayuda
                    ProfileOptionUi(
                        icon = Icons.Default.Help,
                        text = stringResource(id = R.string.help),
                        onClick = { navController.navigate(Screen.Help.route) }
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    // Cerrar Sesión
                    ProfileOptionUi(
                        icon = Icons.Default.ExitToApp,
                        text = stringResource(id = R.string.logout),
                        isLogout = true,
                        onClick = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileOptionUi(
    icon: ImageVector,
    text: String,
    isLogout: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isLogout) Color.Red else Color(0xFF0D47A1),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = if (isLogout) Color.Red else Color.Black,
            modifier = Modifier.weight(1f)
        )
        // Icono de flechita a la derecha
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}