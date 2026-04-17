package com.example.truekita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource // IMPORTANTE PARA EL IDIOMA
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen

// Modelo de datos para las rutas
data class RouteData(
    val name: String,
    val path: String,
    val time: String,
    val spots: Int,
    val isAvailable: Boolean
)

@Composable
fun ViewRouteScreen(navController: NavController) {
    // Datos de ejemplo usando strings del XML
    val routes = listOf(
        RouteData(stringResource(id = R.string.seller_omar), stringResource(id = R.string.route_zone_ita), "7:20 Am", 3, true),
        RouteData(stringResource(id = R.string.seller_omar), stringResource(id = R.string.route_zone_ita), "7:20 Am", 0, false)
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.routes), // "Rutas" o "Rutas Disponibles"
                showBack = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFE3F2FD)) // Azul claro ITA
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(routes) { item ->
                    RouteItemCard(
                        route = item,
                        // Navega a la pantalla del mapa
                        onVerRuta = { navController.navigate(Screen.MapasRuta.route) },
                        // Navega al chat para ponerse de acuerdo
                        onSolicitar = { navController.navigate(Screen.ChatList.route) }
                    )
                }
            }

            // Botón Agregar Ruta
            Button(
                onClick = { navController.navigate(Screen.PublishRoute.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF98EE99)), // Verde característico
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.add_route),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RouteItemCard(route: RouteData, onVerRuta: () -> Unit, onSolicitar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = route.name, fontWeight = FontWeight.Bold)
                Text(text = "${stringResource(id = R.string.routes)}: ${route.path}", fontSize = 14.sp)
                Text(text = "${stringResource(id = R.string.departure_720).split(":")[0]}: ${route.time}", fontSize = 14.sp)
            }

            // Columna de botones (La que arreglaste con Alignment)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = onVerRuta,
                    modifier = Modifier.width(110.dp).height(35.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = stringResource(id = R.string.view_route), fontSize = 12.sp, color = Color.White)
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onSolicitar,
                    modifier = Modifier.width(110.dp).height(35.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = stringResource(id = R.string.request_trip), fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}