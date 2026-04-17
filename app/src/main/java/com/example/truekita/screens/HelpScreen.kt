package com.example.truekita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar

@Composable
fun HelpScreen(navController: NavController) {
    val lightBlue = Color(0xFFE3F2FD)
    val darkBlue = Color(0xFF0D47A1)

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.help_center),
                showBack = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(lightBlue)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // Sección de Bienvenida
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.HelpOutline, null, modifier = Modifier.size(60.dp), tint = darkBlue)
                    Text(
                        text = stringResource(id = R.string.find_answers),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }

            // Opción: Soporte en Vivo
            item {
                HelpCard(
                    title = stringResource(id = R.string.live_support),
                    subtitle = stringResource(id = R.string.real_time_chat),
                    icon = Icons.Default.SupportAgent,
                    iconColor = Color(0xFF4CAF50)
                ) { /* Acción para chat */ }
            }

            // Opción: Preguntas Frecuentes
            item {
                HelpCard(
                    title = stringResource(id = R.string.faq),
                    subtitle = stringResource(id = R.string.solve_common_questions),
                    icon = Icons.Default.QuestionAnswer,
                    iconColor = Color(0xFF2196F3)
                ) { /* Acción para FAQ */ }
            }

            // Opción: Generar Ticket
            item {
                HelpCard(
                    title = stringResource(id = R.string.generate_ticket),
                    subtitle = stringResource(id = R.string.administrator),
                    icon = Icons.Default.ConfirmationNumber,
                    iconColor = Color(0xFFFF9800)
                ) { /* Acción para Ticket */ }
            }
        }
    }
}

@Composable
fun HelpCard(title: String, subtitle: String, icon: ImageVector, iconColor: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(32.dp), tint = iconColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}