package com.example.truekita.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProductCard(
    title: String,
    price: String,
    seller: String,
    condition: String,
    status: String,
    imageRes: Int,
    onMessageClick: () -> Unit,
    onDetailsClick: () -> Unit // <--- Parámetro para navegar a detalles
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = price, color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
                    Text(text = "Vendedor: $seller", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "Condición: $condition", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // BOTÓN DETALLES
                OutlinedButton(
                    onClick = onDetailsClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3AA9B5))
                ) {
                    Text("Detalles", color = Color(0xFF3AA9B5))
                }

                // BOTÓN MENSAJE
                Button(
                    onClick = onMessageClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3AA9B5)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Mensaje", color = Color.White)
                }
            }
        }
    }
}