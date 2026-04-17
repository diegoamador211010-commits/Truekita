package com.example.truekita.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.truekita.navigation.Screen

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        Triple("Inicio", Icons.Default.Home, Screen.Home.route),
        // CORRECCIÓN AQUÍ: Cambiamos Routes por ViewRoute
        Triple("Rutas", Icons.Default.DirectionsCar, Screen.ViewRoute.route),
        Triple("Publicar", Icons.Default.Add, Screen.PublishProduct.route),
        Triple("Chats", Icons.Default.Email, Screen.ChatList.route),
        Triple("Perfil", Icons.Default.Person, Screen.Profile.route)
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    NavigationBar {
        items.forEach { (label, icon, route) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentDestination?.route == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}