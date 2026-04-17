package com.example.truekita.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.ProductUi
import com.example.truekita.components.*
import com.example.truekita.navigation.Screen

@Composable
fun HomeScreen(
    navController: NavController,
    onOpenNotifications: () -> Unit // Recibe la función para el panel
) {
    val filterAll = stringResource(id = R.string.all_caps)
    val filterChat = stringResource(id = R.string.chat)
    val filterTransport = stringResource(id = R.string.transport)
    var selectedFilter by remember { mutableStateOf("TODO") }

    val products = listOf(
        ProductUi(stringResource(id = R.string.calculator), stringResource(id = R.string.price_200), stringResource(id = R.string.seller_alexis), stringResource(id = R.string.condition_new), stringResource(id = R.string.available), R.drawable.calculadora, "VENTA"),
        ProductUi(stringResource(id = R.string.arduino), stringResource(id = R.string.price_150), stringResource(id = R.string.seller_nestor), stringResource(id = R.string.condition_semi_new), stringResource(id = R.string.available), R.drawable.arduino, "INTERCAMBIO")
    )

    val chats = listOf(
        Pair(stringResource(id = R.string.omar_andrade), stringResource(id = R.string.new_message)),
        Pair(stringResource(id = R.string.seller_leonardo), stringResource(id = R.string.sample_message_2))
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.app_name),
                showBack = false,
                onNotificationClick = onOpenNotifications // Abre el panel
            )
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item { SearchBarPlaceholder(stringResource(id = R.string.search_hint), { navController.navigate(Screen.Search.route) }) }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChipButton(filterAll, selectedFilter == "TODO", { selectedFilter = "TODO" }, Modifier.weight(1f))
                    FilterChipButton(filterChat, selectedFilter == "Chat", { selectedFilter = "Chat" }, Modifier.weight(1f))
                    FilterChipButton(filterTransport, selectedFilter == "Transporte", { selectedFilter = "Transporte" }, Modifier.weight(1f))
                }
            }
            when (selectedFilter) {
                "TODO" -> {
                    items(products) { item ->
                        ProductCard(item.title, item.price, item.seller, item.condition, item.status, item.imageRes,
                            onMessageClick = { navController.navigate(Screen.ChatList.route) },
                            onDetailsClick = { navController.navigate("${Screen.ProductDetail.route}/${item.title}") }
                        )
                    }
                }
                "Chat" -> {
                    items(chats) { chat -> ChatListItem(chat.first, chat.second) { navController.navigate(Screen.ChatDetail.route) } }
                }
                "Transporte" -> {
                    item { TransportItem(stringResource(id = R.string.route_zone_ita), stringResource(id = R.string.departure_720)) }
                }
            }
        }
    }
}

// Componentes ChatListItem y TransportItem se mantienen igual abajo...
@Composable
fun ChatListItem(name: String, lastMsg: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = lastMsg, color = Color.Gray, fontSize = 14.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun TransportItem(origin: String, destiny: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DirectionsCar, null, modifier = Modifier.size(32.dp), tint = Color(0xFF2E7D32))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = origin, fontWeight = FontWeight.Bold)
                Text(text = destiny, color = Color.DarkGray, fontSize = 13.sp)
            }
        }
    }
}