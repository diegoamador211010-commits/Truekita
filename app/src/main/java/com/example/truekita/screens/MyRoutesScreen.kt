package com.example.truekita.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource // IMPORTANTE
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen
import com.example.truekita.R

@Composable
fun MyRoutesScreen(navController: NavController) {
    // Usamos las etiquetas del XML para los nombres de las rutas
    val myRoutes = listOf(
        stringResource(id = R.string.route_zone_ita), // "Ruta: Zona Centro - ITA"
        stringResource(id = R.string.view_route)      // "Ver ruta" (o cualquier otra etiqueta de ruta)
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.my_routes), // "Mis rutas" en el XML
                showBack = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(myRoutes) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = item, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                navController.navigate(Screen.PublishRoute.route)
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            // Cambié "Editar" por la etiqueta del XML para que se traduzca
                            Text(text = stringResource(id = R.string.edit))
                        }
                    }
                }
            }
        }
    }
}