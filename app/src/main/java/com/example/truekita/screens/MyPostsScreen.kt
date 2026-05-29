package com.example.truekita.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.truekita.R
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

data class MyPostProductDb(
    val id: String = "",
    val titulo: String = "",
    val condicion: String = "",
    val tipo: String = "",
    val precio: String = "",
    val zonaEntrega: String = "",
    val imagenUrl: String = "",
    val vendedorUid: String = "",
    val vendedorCorreo: String = "",
    val estado: String = ""
)

@Composable
fun MyPostsScreen(navController: NavController) {
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    val currentUser = auth.currentUser

    var myProducts by remember {
        mutableStateOf<List<MyPostProductDb>>(emptyList())
    }

    var loading by remember {
        mutableStateOf(true)
    }

    var productToEdit by remember {
        mutableStateOf<MyPostProductDb?>(null)
    }

    var productToDelete by remember {
        mutableStateOf<MyPostProductDb?>(null)
    }

    DisposableEffect(currentUser?.uid) {
        if (currentUser == null) {
            loading = false
            onDispose { }
        } else {
            val listener = db.collection("productos")
                .whereEqualTo("vendedorUid", currentUser.uid)
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

                    myProducts = snapshot?.documents
                        ?.sortedByDescending { document ->
                            document.getTimestamp("fechaPublicacion")?.toDate()?.time ?: 0L
                        }
                        ?.map { document ->
                            MyPostProductDb(
                                id = document.getString("id") ?: document.id,
                                titulo = document.getString("titulo") ?: "",
                                condicion = document.getString("condicion") ?: "",
                                tipo = document.getString("tipo") ?: "",
                                precio = document.getString("precio") ?: "",
                                zonaEntrega = document.getString("zonaEntrega") ?: "",
                                imagenUrl = document.getString("imagenUrl") ?: "",
                                vendedorUid = document.getString("vendedorUid") ?: "",
                                vendedorCorreo = document.getString("vendedorCorreo") ?: "",
                                estado = document.getString("estado") ?: "disponible"
                            )
                        } ?: emptyList()

                    loading = false
                }

            onDispose {
                listener.remove()
            }
        }
    }

    productToEdit?.let { product ->
        EditProductDialog(
            product = product,
            onDismiss = {
                productToEdit = null
            },
            onSave = { titulo, condicion, tipo, precio, estado ->
                db.collection("productos")
                    .document(product.id)
                    .update(
                        mapOf(
                            "titulo" to titulo,
                            "condicion" to condicion,
                            "tipo" to tipo,
                            "precio" to precio,
                            "estado" to estado,
                            "fechaActualizacion" to Timestamp.now()
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Publicación actualizada",
                            Toast.LENGTH_SHORT
                        ).show()

                        productToEdit = null
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(
                            context,
                            "Error al editar: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        )
    }

    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = {
                productToDelete = null
            },
            title = {
                Text(text = "Eliminar publicación")
            },
            text = {
                Text(text = "¿Seguro que quieres eliminar \"${product.titulo}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        db.collection("productos")
                            .document(product.id)
                            .delete()
                            .addOnSuccessListener {
                                if (product.imagenUrl.isNotBlank()) {
                                    storage.getReferenceFromUrl(product.imagenUrl)
                                        .delete()
                                        .addOnFailureListener {
                                            // Aunque falle borrar la imagen, el producto ya fue eliminado.
                                        }
                                }

                                Toast.makeText(
                                    context,
                                    "Publicación eliminada",
                                    Toast.LENGTH_SHORT
                                ).show()

                                productToDelete = null
                            }
                            .addOnFailureListener { error ->
                                Toast.makeText(
                                    context,
                                    "Error al eliminar: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        productToDelete = null
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.my_posts),
                showBack = true,
                onBackClick = {
                    navController.popBackStack()
                }
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

            Text(
                text = stringResource(id = R.string.active_posts),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            when {
                currentUser == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Inicia sesión para ver tus publicaciones",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }

                loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                myProducts.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no tienes publicaciones",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(myProducts, key = { it.id }) { product ->
                            MyPostProductCard(
                                product = product,
                                onDetailsClick = {
                                    navController.navigate(
                                        "${Screen.ProductDetail.route}/${product.id}"
                                    )
                                },
                                onEditClick = {
                                    productToEdit = product
                                },
                                onDeleteClick = {
                                    productToDelete = product
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
fun MyPostProductCard(
    product: MyPostProductDb,
    onDetailsClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
                    .height(180.dp)
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
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Precio: ${product.precio}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Condición: ${product.condicion}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Tipo: ${product.tipo}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            if (product.zonaEntrega.isNotBlank()) {
                Text(
                    text = "Zona de entrega: ${product.zonaEntrega}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            Text(
                text = "Estado: ${product.estado}",
                fontSize = 13.sp,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDetailsClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF81C784)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Ver detalles",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D47A1)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Editar",
                        color = Color.White
                    )
                }

                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Eliminar",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EditProductDialog(
    product: MyPostProductDb,
    onDismiss: () -> Unit,
    onSave: (
        titulo: String,
        condicion: String,
        tipo: String,
        precio: String,
        estado: String
    ) -> Unit
) {
    var titulo by remember { mutableStateOf(product.titulo) }
    var condicion by remember { mutableStateOf(product.condicion) }
    var tipo by remember { mutableStateOf(product.tipo) }
    var precio by remember { mutableStateOf(product.precio) }
    var estado by remember { mutableStateOf(product.estado) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Editar publicación")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true
                )

                MyPostDropdownField(
                    value = condicion,
                    onValueChange = { condicion = it },
                    label = "Condición",
                    options = listOf("Nuevo", "Semi nuevo")
                )

                MyPostDropdownField(
                    value = tipo,
                    onValueChange = { tipo = it },
                    label = "Tipo",
                    options = listOf("Venta", "Renta", "Intercambio")
                )

                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    singleLine = true
                )

                MyPostDropdownField(
                    value = estado,
                    onValueChange = { estado = it },
                    label = "Estado",
                    options = listOf("disponible", "vendido", "pausado")
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (
                        titulo.isBlank() ||
                        condicion.isBlank() ||
                        tipo.isBlank() ||
                        precio.isBlank() ||
                        estado.isBlank()
                    ) {
                        return@TextButton
                    }

                    onSave(
                        titulo,
                        condicion,
                        tipo,
                        precio,
                        estado
                    )
                }
            ) {
                Text(text = "Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPostDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(label)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(option)
                    },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}