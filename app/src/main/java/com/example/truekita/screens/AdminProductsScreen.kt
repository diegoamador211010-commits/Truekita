package com.example.truekita.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun AdminProductsScreen(
    navController: NavController
) {
    AdminPlaceholderScreen(
        title = "Publicaciones pendientes",
        subtitle = "Aquí el admin aceptará o rechazará productos publicados.",
        iconType = "products",
        onBack = {
            navController.popBackStack()
        }
    )
}