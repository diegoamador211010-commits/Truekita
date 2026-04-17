package com.example.truekita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource // IMPORTANTE
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen

@Composable
fun PublishProductScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.publish_product), // "Publicar Producto"
                showBack = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE3F2FD))
                .padding(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // RECUADRO DE IMAGEN
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = stringResource(id = R.string.tap_to_add_image), // "Pulsa para agregar imagen"
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // INPUTS CON LABELS DEL XML
                item { CustomInputField(value = title, onValueChange = { title = it }, label = stringResource(id = R.string.title_hint)) }
                item { CustomInputField(value = condition, onValueChange = { condition = it }, label = stringResource(id = R.string.product_condition_hint)) }
                item { CustomInputField(value = type, onValueChange = { type = it }, label = stringResource(id = R.string.product_type_hint)) }
                item { CustomInputField(value = price, onValueChange = { price = it }, label = stringResource(id = R.string.product_price_hint)) }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // BOTÓN ELEGIR ZONA (VERDE)
                item {
                    Button(
                        onClick = { navController.navigate(Screen.MeetingPoints.route) },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5F0B5)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.choose_delivery_zone), // "Elegir zona de entrega"
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // BOTÓN PUBLICAR
                item {
                    Button(
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5F0B5)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.publish_action), // "Publicar"
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // AVISO LEGAL/SISTEMA
                item {
                    Text(
                        text = stringResource(id = R.string.review_notice), // Aviso de revisión automática
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomInputField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, fontSize = 14.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF1F8E9),
            unfocusedContainerColor = Color(0xFFF1F8E9),
            focusedBorderColor = Color(0xFFC8E6C9),
            unfocusedBorderColor = Color(0xFFC8E6C9)
        )
    )
}