package com.example.truekita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource // IMPORTANTE
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.ProductUi
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.components.ProductCard
import com.example.truekita.navigation.Screen

@Composable
fun MyPostsScreen(navController: NavController) {
    // Lista de productos usando las etiquetas del XML para que cambien de idioma
    val myProducts = listOf(
        ProductUi(
            stringResource(id = R.string.product_calculator),
            stringResource(id = R.string.price_150),
            "Diego (Me)",
            stringResource(id = R.string.condition_semi_new),
            stringResource(id = R.string.available),
            R.drawable.calculadora,
            "VENTA"
        ),
        ProductUi(
            stringResource(id = R.string.arduino),
            stringResource(id = R.string.price_120),
            "Diego (Me)",
            stringResource(id = R.string.condition_new),
            stringResource(id = R.string.in_wait),
            R.drawable.arduino,
            "VENTA"
        )
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.my_posts), // "Mis publicaciones"
                showBack = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Subtítulo usando una etiqueta de revisión o aviso si prefieres
            Text(
                text = stringResource(id = R.string.active_posts), // "Publicaciones Activas"
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(myProducts) { item ->
                    ProductCard(
                        title = item.title,
                        price = item.price,
                        seller = item.seller,
                        condition = item.condition,
                        status = item.status,
                        imageRes = item.imageRes,
                        onMessageClick = {
                            // Lógica para ver quién te escribió
                            navController.navigate(Screen.ChatList.route)
                        },
                        onDetailsClick = {
                            // Navegación al detalle
                            navController.navigate("product_detail_screen/${item.title}")
                        }
                    )
                }
            }
        }
    }
}