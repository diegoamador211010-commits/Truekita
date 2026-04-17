package com.example.truekita.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource // IMPORTANTE
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.ProductUi
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.components.FilterChipButton
import com.example.truekita.components.ProductCard
import com.example.truekita.navigation.Screen

@Composable
fun SearchScreen(navController: NavController) {
    // Obtenemos los textos de los filtros desde el XML
    val saleFilter = stringResource(id = R.string.sale_caps)
    val rentFilter = stringResource(id = R.string.rent_caps)
    val exchangeFilter = stringResource(id = R.string.exchange_caps)

    var searchQuery by remember { mutableStateOf("") }
    var selectedSearchFilter by remember { mutableStateOf("VENTA") } // Mantenemos el ID interno para la lógica

    val allProducts = listOf(
        ProductUi(stringResource(R.string.calculator) + " Casio", "$200", stringResource(R.string.seller_omar), stringResource(R.string.condition_new), stringResource(R.string.available), R.drawable.calculadora, "VENTA"),
        ProductUi(stringResource(R.string.calculator) + " Grafica", "Intercambio", stringResource(R.string.seller_leonardo), stringResource(R.string.condition_semi_new), stringResource(R.string.in_wait), R.drawable.calculadora, "INTERCAMBIO"),
        ProductUi(stringResource(R.string.arduino) + " Uno", "$150", stringResource(R.string.seller_nestor), stringResource(R.string.condition_new), stringResource(R.string.available), R.drawable.arduino, "VENTA"),
        ProductUi(stringResource(R.string.iphone_charger), "$200", stringResource(R.string.seller_abraham), stringResource(R.string.condition_new), stringResource(R.string.available), R.drawable.cargador_iphone, "VENTA")
    )

    val filteredProducts = allProducts.filter { product ->
        val matchesName = product.title.contains(searchQuery, ignoreCase = true)
        val matchesFilter = product.type == selectedSearchFilter

        if (searchQuery.isEmpty()) false else matchesName && matchesFilter
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.search),
                showBack = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(12.dp))

            // Buscador con placeholder del XML
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(id = R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(25.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF81C784),
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Chips de filtro traducidos
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChipButton(saleFilter, selectedSearchFilter == "VENTA", { selectedSearchFilter = "VENTA" }, Modifier.weight(1f))
                FilterChipButton(rentFilter, selectedSearchFilter == "RENTA", { selectedSearchFilter = "RENTA" }, Modifier.weight(1f))
                FilterChipButton(exchangeFilter, selectedSearchFilter == "INTERCAMBIO", { selectedSearchFilter = "INTERCAMBIO" }, Modifier.weight(1f))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(id = R.string.search_user).replace("Usuario", "producto"),
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray
                        )
                    }
                } else if (filteredProducts.isEmpty()) {
                    item {
                        Text(
                            text = "No hay resultados.", // Podrías agregar esta al XML si gustas
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray
                        )
                    }
                } else {
                    items(filteredProducts) { item ->
                        ProductCard(
                            title = item.title,
                            price = item.price,
                            seller = item.seller,
                            condition = item.condition,
                            status = item.status,
                            imageRes = item.imageRes,
                            onMessageClick = { navController.navigate(Screen.ChatDetail.route) },
                            onDetailsClick = {
                                // Navegación dinámica para que no marque error
                                navController.navigate("product_detail_screen/${item.title}")
                            }
                        )
                    }
                }
            }
        }
    }
}