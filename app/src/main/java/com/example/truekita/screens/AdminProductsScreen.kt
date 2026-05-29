package com.example.truekita.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

data class AdminProductDb(
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
    val motivoRechazo: String = "",
    val revisadoPor: String = ""
)

@Composable
fun AdminProductsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var productos by remember {
        mutableStateOf<List<AdminProductDb>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var selectedFilter by remember {
        mutableStateOf("pendiente")
    }

    var productToReject by remember {
        mutableStateOf<AdminProductDb?>(null)
    }

    var rejectReason by remember {
        mutableStateOf("")
    }

    DisposableEffect(Unit) {
        val listener = db.collection("productos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    loading = false

                    Toast.makeText(
                        context,
                        "Error al cargar publicaciones: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addSnapshotListener
                }

                productos = snapshot?.documents
                    ?.sortedByDescending { document ->
                        document.getTimestamp("fechaPublicacion")?.toDate()?.time ?: 0L
                    }
                    ?.map { document ->
                        AdminProductDb(
                            id = document.getString("id") ?: document.id,
                            titulo = document.getString("titulo") ?: "",
                            condicion = document.getString("condicion") ?: "",
                            tipo = document.getString("tipo") ?: "",
                            precio = document.getString("precio") ?: "",
                            zonaEntrega = document.getString("zonaEntrega") ?: "",
                            imagenUrl = document.getString("imagenUrl") ?: "",
                            vendedorCorreo = document.getString("vendedorCorreo") ?: "",
                            estado = document.getString("estado") ?: "disponible",
                            estadoRevision = document.getString("estadoRevision") ?: "pendiente",
                            motivoRechazo = document.getString("motivoRechazo") ?: "",
                            revisadoPor = document.getString("revisadoPor") ?: ""
                        )
                    } ?: emptyList()

                loading = false
            }

        onDispose {
            listener.remove()
        }
    }

    val productosFiltrados = remember(productos, selectedFilter) {
        productos.filter { producto ->
            when (selectedFilter) {
                "pendiente" -> producto.estadoRevision == "pendiente"
                "aceptada" -> producto.estadoRevision == "aceptada"
                "rechazada" -> producto.estadoRevision == "rechazada"
                else -> true
            }
        }
    }

    fun aceptarProducto(product: AdminProductDb) {
        db.collection("productos")
            .document(product.id)
            .update(
                mapOf(
                    "estadoRevision" to "aceptada",
                    "motivoRechazo" to "Aprobada manualmente por el administrador.",
                    "fechaRevision" to Timestamp.now(),
                    "revisadoPor" to "Administrador"
                )
            )
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Publicación aceptada",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    context,
                    "Error al aceptar: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun rechazarProducto(product: AdminProductDb, motivo: String) {
        db.collection("productos")
            .document(product.id)
            .update(
                mapOf(
                    "estadoRevision" to "rechazada",
                    "motivoRechazo" to motivo,
                    "fechaRevision" to Timestamp.now(),
                    "revisadoPor" to "Administrador"
                )
            )
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Publicación rechazada",
                    Toast.LENGTH_SHORT
                ).show()

                productToReject = null
                rejectReason = ""
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    context,
                    "Error al rechazar: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    productToReject?.let { product ->
        AlertDialog(
            onDismissRequest = {
                productToReject = null
                rejectReason = ""
            },
            title = {
                Text(text = "Rechazar publicación")
            },
            text = {
                Column {
                    Text(
                        text = "Escribe el motivo por el que se rechazará:",
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = {
                            rejectReason = it
                        },
                        label = {
                            Text("Motivo de rechazo")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (rejectReason.isBlank()) {
                            Toast.makeText(
                                context,
                                "Escribe un motivo",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@TextButton
                        }

                        rechazarProducto(product, rejectReason.trim())
                    }
                ) {
                    Text(
                        text = "Rechazar",
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        productToReject = null
                        rejectReason = ""
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Regresar"
                )
            }

            Text(
                text = "Revisión de publicaciones",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Panel del administrador",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminFilterButton(
                    text = "Pendientes",
                    selected = selectedFilter == "pendiente",
                    onClick = {
                        selectedFilter = "pendiente"
                    },
                    modifier = Modifier.weight(1f)
                )

                AdminFilterButton(
                    text = "Aceptadas",
                    selected = selectedFilter == "aceptada",
                    onClick = {
                        selectedFilter = "aceptada"
                    },
                    modifier = Modifier.weight(1f)
                )

                AdminFilterButton(
                    text = "Rechazadas",
                    selected = selectedFilter == "rechazada",
                    onClick = {
                        selectedFilter = "rechazada"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                productosFiltrados.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay publicaciones en esta sección",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(productosFiltrados, key = { it.id }) { product ->
                            AdminProductCard(
                                product = product,
                                onAcceptClick = {
                                    aceptarProducto(product)
                                },
                                onRejectClick = {
                                    productToReject = product
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(45.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) {
                Color(0xFF0D47A1)
            } else {
                Color.White
            }
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = text,
            color = if (selected) {
                Color.White
            } else {
                Color.Black
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AdminProductCard(
    product: AdminProductDb,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    val revisionColor = when (product.estadoRevision) {
        "aceptada" -> Color(0xFF2E7D32)
        "rechazada" -> Color.Red
        "pendiente" -> Color(0xFFFF9800)
        else -> Color.Gray
    }

    val revisionText = when (product.estadoRevision) {
        "aceptada" -> "Aceptada"
        "rechazada" -> "Rechazada"
        "pendiente" -> "Pendiente"
        else -> "Sin revisión"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imagenUrl.isNotBlank()) {
                    AsyncImage(
                        model = product.imagenUrl,
                        contentDescription = product.titulo,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = product.titulo.ifBlank { "Sin título" },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Precio: ${product.precio.ifBlank { "Sin precio" }}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Condición: ${product.condicion.ifBlank { "Sin condición" }}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Tipo: ${product.tipo.ifBlank { "Sin tipo" }}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            if (product.zonaEntrega.isNotBlank()) {
                Text(
                    text = "Zona: ${product.zonaEntrega}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            Text(
                text = "Vendedor: ${product.vendedorCorreo.ifBlank { "Sin correo" }}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Estado revisión: $revisionText",
                fontSize = 14.sp,
                color = revisionColor,
                fontWeight = FontWeight.Bold
            )

            if (product.revisadoPor.isNotBlank()) {
                Text(
                    text = "Revisado por: ${product.revisadoPor}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            if (product.motivoRechazo.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (product.estadoRevision == "rechazada") {
                            Color(0xFFFFEBEE)
                        } else {
                            Color(0xFFE8F5E9)
                        }
                    )
                ) {
                    Text(
                        text = "Motivo: ${product.motivoRechazo}",
                        fontSize = 13.sp,
                        color = if (product.estadoRevision == "rechazada") {
                            Color.Red
                        } else {
                            Color(0xFF2E7D32)
                        },
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAcceptClick,
                    modifier = Modifier.weight(1f),
                    enabled = product.estadoRevision != "aceptada",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32),
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Aceptar",
                        color = Color.White
                    )
                }

                Button(
                    onClick = onRejectClick,
                    modifier = Modifier.weight(1f),
                    enabled = product.estadoRevision != "rechazada",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Rechazar",
                        color = Color.White
                    )
                }
            }
        }
    }
}