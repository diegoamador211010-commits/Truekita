package com.example.truekita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.truekita.navigation.NavGraph
import com.example.truekita.screens.NotificationsBottomSheet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            // Estado para el panel de notificaciones
            var showNotifications by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxSize()) {
                // Tu navegación original intacta
                NavGraph(
                    navController = navController,
                    onOpenNotifications = { showNotifications = true }
                )

                // El panel desplegable (solo aparece si showNotifications es true)
                if (showNotifications) {
                    NotificationsBottomSheet(
                        onDismiss = { showNotifications = false }
                    )
                }
            }
        }
    }
}