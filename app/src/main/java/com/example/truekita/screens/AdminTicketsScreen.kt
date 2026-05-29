package com.example.truekita.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AdminTicketsScreen(
    navController: NavController
) {
    AdminPlaceholderScreen(
        title = "Tickets de soporte",
        subtitle = "Aquí el admin responderá tickets de usuarios.",
        iconType = "tickets",
        onBack = {
            navController.popBackStack()
        }
    )
}