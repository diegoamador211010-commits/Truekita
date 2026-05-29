package com.example.truekita.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AdminHomeScreen(
    navController: NavController
) {
    val backgroundColor = Color(0xFFE3F2FD)
    val darkBlue = Color(0xFF0D47A1)
    val green = Color(0xFF81C784)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = darkBlue,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Panel de administrador",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Gestiona chats, tickets, publicaciones y viajes",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        AdminOptionCard(
            title = "Tickets de soporte",
            subtitle = "Responder dudas o reportes de usuarios",
            iconType = "tickets",
            onClick = {
                navController.navigate(Screen.AdminTickets.route)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        AdminOptionCard(
            title = "Chats de usuarios",
            subtitle = "Ver conversaciones enviadas al administrador",
            iconType = "chats",
            onClick = {
                navController.navigate(Screen.AdminChats.route)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        AdminOptionCard(
            title = "Publicaciones pendientes",
            subtitle = "Aceptar o rechazar productos publicados",
            iconType = "products",
            onClick = {
                navController.navigate(Screen.AdminProducts.route)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        AdminOptionCard(
            title = "Viajes publicados",
            subtitle = "Eliminar rutas o viajes no permitidos",
            iconType = "routes",
            onClick = {
                navController.navigate(Screen.AdminRoutes.route)
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()

                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.AdminHome.route) {
                        inclusive = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = green
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(
                text = "Cerrar sesión",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AdminOptionCard(
    title: String,
    subtitle: String,
    iconType: String,
    onClick: () -> Unit
) {
    val darkBlue = Color(0xFF0D47A1)

    val icon = when (iconType) {
        "tickets" -> Icons.Default.SupportAgent
        "chats" -> Icons.Default.Chat
        "products" -> Icons.Default.Article
        else -> Icons.Default.DirectionsCar
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = darkBlue,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}