package com.example.truekita.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
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
import com.example.truekita.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.Normalizer
import java.util.Locale

data class SearchProductDb(
    val id: String = "",
    val titulo: String = "",
    val condicion: String = "",
    val tipo: String = "",
    val precio: String = "",
    val zonaEntrega: String = "",
    val fechaEntrega: String = "",
    val horaEntrega: String = "",
    val imagenUrl: String = "",
    val vendedorUid: String = "",
    val vendedorCorreo: String = "",
    val estado: String = "",
    val estadoRevision: String = ""
)

@Composable
fun SearchScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val saleFilter = stringResource(id = R.string.sale_caps)
    val rentFilter = stringResource(id = R.string.rent_caps)
    val exchangeFilter = stringResource(id = R.string.exchange_caps)

    var searchQuery by remember { mutableStateOf("") }
    var selectedSearchFilter by remember { mutableStateOf("TODO") }

    var products by remember { mutableStateOf<List<SearchProductDb>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val listener = db.collection("productos")
            .whereEqualTo("estadoRevision", "aceptada")
            .whereEqualTo("estado", "disponible")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    loading = false
                    errorMessage = error.message ?: "Error desconocido"

                    Toast.makeText(
                        context,
                        "Error al buscar productos: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addSnapshotListener
                }

                products = snapshot?.documents
                    ?.sortedByDescending { document ->
                        document.getTimestamp("fechaPublicacion")?.toDate()?.time ?: 0L
                    }
                    ?.map { document ->
                        SearchProductDb(
                            id = document.getString("id") ?: document.id,
                            titulo = document.getString("titulo") ?: "",
                            condicion = document.getString("condicion") ?: "",
                            tipo = document.getString("tipo") ?: "",
                            precio = document.getString("precio") ?: "",
                            zonaEntrega = document.getString("zonaEntrega") ?: "",
                            fechaEntrega = document.getString("fechaEntrega") ?: "",
                            horaEntrega = document.getString("horaEntrega") ?: "",
                            imagenUrl = document.getString("imagenUrl") ?: "",
                            vendedorUid = document.getString("vendedorUid") ?: "",
                            vendedorCorreo = document.getString("vendedorCorreo") ?: "",
                            estado = document.getString("estado") ?: "disponible",
                            estadoRevision = document.getString("estadoRevision") ?: ""
                        )
                    } ?: emptyList()

                loading = false
            }

        onDispose {
            listener.remove()
        }
    }

    fun abrirOCrearChatConVendedor(product: SearchProductDb) {
        val usuarioActual = auth.currentUser

        if (usuarioActual == null) {
            Toast.makeText(
                context,
                "Debes iniciar sesión para enviar mensaje",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (product.vendedorUid.isBlank()) {
            Toast.makeText(
                context,
                "Este producto no tiene vendedor asignado",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (product.vendedorUid == usuarioActual.uid) {
            Toast.makeText(
                context,
                "Este producto es tuyo",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val uidActual = usuarioActual.uid
        val nombreActual = usuarioActual.displayName ?: usuarioActual.email ?: "Usuario"
        val nombreVendedor = product.vendedorCorreo.ifBlank { "Vendedor" }

        val chatId = listOf(uidActual, product.vendedorUid)
            .sorted()
            .joinToString("_")

        val chatData = hashMapOf(
            "participantes" to listOf(uidActual, product.vendedorUid),
            "participantesNombres" to mapOf(
                uidActual to nombreActual,
                product.vendedorUid to nombreVendedor
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

    val filteredProducts = remember(searchQuery, selectedSearchFilter, products) {
        val query = normalizeSearchText(searchQuery)

        products.filter { product ->
            val productType = normalizeSearchText(product.tipo)
            val selectedType = normalizeSearchText(selectedSearchFilter)

            val matchesFilter =
                selectedSearchFilter == "TODO" || productType == selectedType

            val matchesSearch =
                query.isBlank() || productMatchesSearch(product, query)

            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.search),
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
                .background(Color(0xFFE3F2FD))
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(stringResource(id = R.string.search_hint))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChipButton(
                    "TODO",
                    selectedSearchFilter == "TODO",
                    {
                        selectedSearchFilter = "TODO"
                    },
                    Modifier.weight(1f)
                )

                FilterChipButton(
                    saleFilter,
                    selectedSearchFilter == "Venta",
                    {
                        selectedSearchFilter = "Venta"
                    },
                    Modifier.weight(1f)
                )

                FilterChipButton(
                    rentFilter,
                    selectedSearchFilter == "Renta",
                    {
                        selectedSearchFilter = "Renta"
                    },
                    Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChipButton(
                    exchangeFilter,
                    selectedSearchFilter == "Intercambio",
                    {
                        selectedSearchFilter = "Intercambio"
                    },
                    Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.weight(2f))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    loading -> {
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

                    errorMessage.isNotBlank() -> {
                        item {
                            Text(
                                text = "Error al cargar productos: $errorMessage",
                                modifier = Modifier.padding(16.dp),
                                color = Color.Red
                            )
                        }
                    }

                    products.isEmpty() -> {
                        item {
                            Text(
                                text = "No hay productos aprobados por el bot.",
                                modifier = Modifier.padding(16.dp),
                                color = Color.Gray
                            )
                        }
                    }

                    searchQuery.isBlank() && selectedSearchFilter == "TODO" -> {
                        item {
                            Text(
                                text = "Busca productos aprobados por nombre, tipo, condición o palabras relacionadas.",
                                modifier = Modifier.padding(16.dp),
                                color = Color.Gray
                            )
                        }
                    }

                    filteredProducts.isEmpty() -> {
                        item {
                            Text(
                                text = "No hay resultados para \"$searchQuery\".",
                                modifier = Modifier.padding(16.dp),
                                color = Color.Gray
                            )
                        }
                    }

                    else -> {
                        items(filteredProducts, key = { it.id }) { product ->
                            SearchProductCard(
                                product = product,
                                onDetailsClick = {
                                    navController.navigate(
                                        "${Screen.ProductDetail.route}/${product.id}"
                                    )
                                },
                                onMessageClick = {
                                    abrirOCrearChatConVendedor(product)
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
fun SearchProductCard(
    product: SearchProductDb,
    onDetailsClick: () -> Unit,
    onMessageClick: () -> Unit
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
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

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
                    text = "Zona de entrega: ${product.zonaEntrega} ${product.horaEntrega}",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

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

fun productMatchesSearch(
    product: SearchProductDb,
    normalizedQuery: String
): Boolean {
    val searchableText = normalizeSearchText(
        listOf(
            product.titulo,
            product.condicion,
            product.tipo,
            product.precio,
            product.zonaEntrega,
            product.fechaEntrega,
            product.horaEntrega,
            product.estado,
            product.estadoRevision,
            product.vendedorCorreo,
            getRelatedWordsForProduct(product.titulo)
        ).joinToString(" ")
    )

    val queryWords = normalizedQuery
        .split(" ")
        .filter { it.isNotBlank() }

    return queryWords.all { word ->
        searchableText.contains(word) ||
                relatedSearchDictionary(word).any { related ->
                    searchableText.contains(related)
                }
    }
}

fun getRelatedWordsForProduct(title: String): String {
    val text = normalizeSearchText(title)

    return when {
        text.contains("iphone") ||
                text.contains("apple") ||
                text.contains("cargador") ||
                text.contains("cable") -> {
            "iphone apple cargador cable celular telefono lightning cargador iphone cargador apple ios smartphone movil"
        }

        text.contains("celular") ||
                text.contains("telefono") ||
                text.contains("smartphone") -> {
            "celular telefono smartphone movil iphone android apple samsung"
        }

        text.contains("calculadora") ||
                text.contains("casio") -> {
            "calculadora casio matematica calculo escolar ingenieria"
        }

        text.contains("arduino") -> {
            "arduino electronica placa sensor protoboard proyecto"
        }

        text.contains("laptop") ||
                text.contains("computadora") -> {
            "laptop computadora pc notebook cargador tecnologia"
        }

        else -> ""
    }
}

fun relatedSearchDictionary(word: String): List<String> {
    return when (word) {
        "iphone", "iphon", "ifone", "apple", "ios" -> listOf(
            "iphone",
            "apple",
            "ios",
            "celular",
            "telefono",
            "smartphone",
            "movil",
            "cargador",
            "lightning",
            "cable"
        )

        "cargador", "carga", "charger", "cable", "lightning" -> listOf(
            "cargador",
            "cable",
            "lightning",
            "iphone",
            "apple",
            "telefono",
            "celular"
        )

        "celular", "telefono", "phone", "smartphone", "movil" -> listOf(
            "celular",
            "telefono",
            "smartphone",
            "movil",
            "iphone",
            "apple"
        )

        "calculadora", "calcu", "casio" -> listOf(
            "calculadora",
            "casio",
            "matematica",
            "calculo"
        )

        "arduino", "electronica" -> listOf(
            "arduino",
            "electronica",
            "sensor",
            "placa",
            "proyecto"
        )

        else -> emptyList()
    }
}

fun normalizeSearchText(text: String): String {
    val lower = text.lowercase(Locale.getDefault()).trim()

    return Normalizer.normalize(lower, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
}