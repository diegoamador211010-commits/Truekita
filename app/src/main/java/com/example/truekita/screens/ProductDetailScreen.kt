package com.example.truekita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.truekita.R
import com.example.truekita.components.BottomNavBar
import com.google.firebase.firestore.FirebaseFirestore

data class ProductoDetalleDb(
    val id: String = "",
    val titulo: String = "",
    val condicion: String = "",
    val tipo: String = "",
    val precio: String = "",
    val zonaEntrega: String = "",
    val imagenUrl: String = "",
    val vendedorCorreo: String = "",
    val estado: String = "",
    val estadoRevision: String = "",
    val motivoRechazo: String = ""
)

@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: String
) {
    val db = FirebaseFirestore.getInstance()

    var producto by remember { mutableStateOf<ProductoDetalleDb?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(productId) {
        if (productId.isBlank()) {
            cargando = false
            error = "Producto no encontrado"
            return@LaunchedEffect
        }

        db.collection("productos")
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val estadoRevision = document.getString("estadoRevision") ?: ""
                    val estadoProducto = document.getString("estado") ?: "disponible"

                    if (estadoRevision != "aceptada") {
                        error = when (estadoRevision) {
                            "rechazada" -> {
                                val motivo = document.getString("motivoRechazo") ?: "No cumple con las reglas de Truekita."
                                "Este producto fue rechazado por el bot.\n\nMotivo: $motivo"
                            }

                            "pendiente" -> {
                                "Este producto todavía está pendiente de revisión."
                            }

                            else -> {
                                "Este producto no está aprobado para mostrarse."
                            }
                        }

                        producto = null
                    } else if (estadoProducto != "disponible") {
                        error = "Este producto ya no está disponible."
                        producto = null
                    } else {
                        producto = ProductoDetalleDb(
                            id = document.getString("id") ?: document.id,
                            titulo = document.getString("titulo") ?: "",
                            condicion = document.getString("condicion") ?: "",
                            tipo = document.getString("tipo") ?: "",
                            precio = document.getString("precio") ?: "",
                            zonaEntrega = document.getString("zonaEntrega") ?: "",
                            imagenUrl = document.getString("imagenUrl") ?: "",
                            vendedorCorreo = document.getString("vendedorCorreo") ?: "",
                            estado = estadoProducto,
                            estadoRevision = estadoRevision,
                            motivoRechazo = document.getString("motivoRechazo") ?: ""
                        )
                    }
                } else {
                    error = "El producto ya no existe"
                }

                cargando = false
            }
            .addOnFailureListener { e ->
                error = "Error al cargar producto: ${e.message}"
                cargando = false
            }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFE3F2FD))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }

                Text(
                    text = "Detalle del producto",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            when {
                cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = error ?: "Error desconocido",
                                    color = Color.Red,
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        navController.popBackStack()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF81C784)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        text = "Regresar",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                producto != null -> {
                    val item = producto!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.imagenUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = item.imagenUrl,
                                        contentDescription = item.titulo,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(70.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.estado.ifBlank { "disponible" },
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color(0xFF81C784), CircleShape)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = item.titulo.ifBlank { "Sin título" },
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                ProductDetailRow(
                                    icon = Icons.Default.AttachMoney,
                                    label = "Precio",
                                    value = item.precio.ifBlank { "Sin precio" }
                                )

                                ProductDetailRow(
                                    icon = Icons.Default.Info,
                                    label = "Condición",
                                    value = item.condicion.ifBlank { "Sin condición" }
                                )

                                ProductDetailRow(
                                    icon = Icons.Default.Category,
                                    label = "Tipo de producto",
                                    value = item.tipo.ifBlank { "Sin tipo" }
                                )

                                ProductDetailRow(
                                    icon = Icons.Default.Place,
                                    label = "Zona de entrega",
                                    value = item.zonaEntrega.ifBlank { "Sin zona de entrega" }
                                )

                                ProductDetailRow(
                                    icon = Icons.Default.Person,
                                    label = "Publicado por",
                                    value = item.vendedorCorreo.ifBlank { "Usuario TRUEKITA" }
                                )

                                ProductDetailRow(
                                    icon = Icons.Default.CheckCircle,
                                    label = "Estado",
                                    value = item.estado.ifBlank { "disponible" }
                                )

                                ProductDetailRow(
                                    icon = Icons.Default.CheckCircle,
                                    label = "Revisión",
                                    value = "Aprobado por el bot"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF0D47A1),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}