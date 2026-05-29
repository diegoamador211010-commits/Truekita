package com.example.truekita.screens

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DirectionsCar
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
import com.example.truekita.components.FilterChipButton
import com.example.truekita.components.SearchBarPlaceholder
import com.example.truekita.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

data class ProductoHomeDb(
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
fun HomeScreen(
    navController: NavController,
    pendingNotifications: Int,
    onOpenNotifications: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var showExitDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("TODO") }

    var productos by remember { mutableStateOf<List<ProductoHomeDb>>(emptyList()) }
    var cargandoProductos by remember { mutableStateOf(true) }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = {
                showExitDialog = false
            },
            title = {
                Text(text = "Salir de TRUEKITA")
            },
            text = {
                Text(text = "¿Estás seguro de que quieres cerrar la aplicación?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        activity?.finish()
                    }
                ) {
                    Text(text = "Sí, salir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    DisposableEffect(Unit) {
        val listener = db.collection("productos")
            .orderBy("fechaPublicacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    cargandoProductos = false

                    Toast.makeText(
                        context,
                        "Error al cargar productos: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addSnapshotListener
                }

                productos = snapshot?.documents?.map { document ->
                    ProductoHomeDb(
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

                cargandoProductos = false
            }

        onDispose {
            listener.remove()
        }
    }

    fun abrirOCrearChatConVendedor(producto: ProductoHomeDb) {
        val usuarioActual = auth.currentUser

        if (usuarioActual == null) {
            Toast.makeText(
                context,
                "Debes iniciar sesión para enviar mensaje",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (producto.vendedorUid.isBlank()) {
            Toast.makeText(
                context,
                "Este producto no tiene vendedor asignado",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (producto.vendedorUid == usuarioActual.uid) {
            Toast.makeText(
                context,
                "Este producto es tuyo",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val uidActual = usuarioActual.uid
        val nombreActual = usuarioActual.displayName ?: usuarioActual.email ?: "Usuario"
        val nombreVendedor = producto.vendedorCorreo.ifBlank { "Vendedor" }

        val chatId = listOf(uidActual, producto.vendedorUid)
            .sorted()
            .joinToString("_")

        val chatData = hashMapOf(
            "participantes" to listOf(uidActual, producto.vendedorUid),
            "participantesNombres" to mapOf(
                uidActual to nombreActual,
                producto.vendedorUid to nombreVendedor
            ),
            "ultimoMensaje" to "",
            "fechaUltimoMensaje" to Timestamp.now()
        )

        db.collection("chats")
            .document(chatId)
            .set(chatData, SetOptions.merge())
            .addOnSuccessListener {
                navController.navigate(
                    "${Screen.ChatDetail.route}/$chatId/${Uri.encode(nombreVendedor)}"
                )
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    context,
                    "Error al abrir chat: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    val filterAll = stringResource(id = R.string.all_caps)
    val filterChat = stringResource(id = R.string.chat)
    val filterTransport = stringResource(id = R.string.transport)

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.app_name),
                showBack = false,
                pendingNotifications = pendingNotifications,
                onNotificationClick = onOpenNotifications
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                SearchBarPlaceholder(
                    stringResource(id = R.string.search_hint),
                    {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChipButton(
                        filterAll,
                        selectedFilter == "TODO",
                        {
                            selectedFilter = "TODO"
                        },
                        Modifier.weight(1f)
                    )

                    FilterChipButton(
                        filterChat,
                        selectedFilter == "Chat",
                        {
                            selectedFilter = "Chat"
                        },
                        Modifier.weight(1f)
                    )

                    FilterChipButton(
                        filterTransport,
                        selectedFilter == "Transporte",
                        {
                            selectedFilter = "Transporte"
                        },
                        Modifier.weight(1f)
                    )
                }
            }

            when (selectedFilter) {
                "TODO" -> {
                    when {
                        cargandoProductos -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        productos.isEmpty() -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Aún no hay productos publicados",
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        else -> {
                            items(productos, key = { it.id }) { producto ->
                                ProductFirebaseCard(
                                    producto = producto,
                                    onMessageClick = {
                                        abrirOCrearChatConVendedor(producto)
                                    },
                                    onDetailsClick = {
                                        navController.navigate(
                                            "${Screen.ProductDetail.route}/${producto.id}"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                "Chat" -> {
                    item {
                        SimpleHomeOptionCard(
                            title = "Mis chats",
                            subtitle = "Ver conversaciones guardadas en Firebase",
                            iconType = "chat",
                            onClick = {
                                navController.navigate(Screen.ChatList.route)
                            }
                        )
                    }
                }

                "Transporte" -> {
                    item {
                        SimpleHomeOptionCard(
                            title = "Rutas disponibles",
                            subtitle = "Ver rutas publicadas desde Firebase",
                            iconType = "transport",
                            onClick = {
                                navController.navigate(Screen.ViewRoute.route)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductFirebaseCard(
    producto: ProductoHomeDb,
    onMessageClick: () -> Unit,
    onDetailsClick: () -> Unit
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
                if (producto.imagenUrl.isNotBlank()) {
                    AsyncImage(
                        model = producto.imagenUrl,
                        contentDescription = producto.titulo,
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
                text = producto.titulo.ifBlank { "Sin título" },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Precio: ${producto.precio}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Condición: ${producto.condicion}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Tipo: ${producto.tipo}",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            if (producto.zonaEntrega.isNotBlank()) {
                Text(
                    text = "Zona de entrega: ${producto.zonaEntrega}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            Text(
                text = "Estado: ${producto.estado}",
                fontSize = 13.sp,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onMessageClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D47A1)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Mensaje",
                        color = Color.White
                    )
                }

                Button(
                    onClick = onDetailsClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF81C784)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Detalles",
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleHomeOptionCard(
    title: String,
    subtitle: String,
    iconType: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (iconType == "chat") {
                    Icons.Default.Chat
                } else {
                    Icons.Default.DirectionsCar
                },
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = Color(0xFF0D47A1)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    }
}