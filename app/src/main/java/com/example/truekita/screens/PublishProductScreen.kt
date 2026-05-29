package com.example.truekita.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishProductScreen(
    navController: NavController
) {
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    val currentUser = auth.currentUser

    var title by rememberSaveable {
        mutableStateOf("")
    }

    var condition by rememberSaveable {
        mutableStateOf("")
    }

    var type by rememberSaveable {
        mutableStateOf("")
    }

    var price by rememberSaveable {
        mutableStateOf("")
    }

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var selectedBitmap by remember {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }

    var uploading by rememberSaveable {
        mutableStateOf(false)
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

    val zonaEntrega by savedStateHandle
        ?.getStateFlow("zonaEntrega", "")
        ?.collectAsState()
        ?: remember {
            mutableStateOf("")
        }

    val fechaEntrega by savedStateHandle
        ?.getStateFlow("fechaEntrega", "")
        ?.collectAsState()
        ?: remember {
            mutableStateOf("")
        }

    val horaEntrega by savedStateHandle
        ?.getStateFlow("horaEntrega", "")
        ?.collectAsState()
        ?: remember {
            mutableStateOf("")
        }

    val conditionOptions = listOf(
        "Nuevo",
        "Semi nuevo"
    )

    val typeOptions = listOf(
        "Venta",
        "Renta",
        "Intercambio"
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri

            val bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                uri
            )

            selectedBitmap = bitmap
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.publish_product),
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
                .padding(16.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = Color(0xFFC8E6C9),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedBitmap != null) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "Imagen seleccionada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.height(60.dp),
                                    tint = Color.Gray
                                )

                                Text(
                                    text = stringResource(id = R.string.tap_to_add_image),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                item {
                    ProductInputField(
                        value = title,
                        onValueChange = {
                            title = it
                        },
                        label = stringResource(id = R.string.title_hint)
                    )
                }

                item {
                    ProductDropdownField(
                        value = condition,
                        onValueChange = {
                            condition = it
                        },
                        label = "Condición del producto",
                        options = conditionOptions
                    )
                }

                item {
                    ProductDropdownField(
                        value = type,
                        onValueChange = {
                            type = it
                        },
                        label = "Tipo de publicación",
                        options = typeOptions
                    )
                }

                item {
                    ProductInputField(
                        value = price,
                        onValueChange = {
                            price = it
                        },
                        label = stringResource(id = R.string.product_price_hint)
                    )
                }

                item {
                    Button(
                        onClick = {
                            navController.navigate(Screen.MeetingPoints.route)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFA5F0B5)
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.choose_delivery_zone),
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF0D47A1)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Zona de entrega",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (zonaEntrega.isNotBlank()) {
                                        "$zonaEntrega - $horaEntrega"
                                    } else {
                                        "Aún no seleccionas zona de entrega"
                                    },
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )

                                if (fechaEntrega.isNotBlank()) {
                                    Text(
                                        text = "Fecha: $fechaEntrega",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (currentUser == null) {
                                Toast.makeText(
                                    context,
                                    "Debes iniciar sesión para publicar",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            if (
                                title.isBlank() ||
                                condition.isBlank() ||
                                type.isBlank() ||
                                price.isBlank() ||
                                zonaEntrega.isBlank() ||
                                horaEntrega.isBlank()
                            ) {
                                Toast.makeText(
                                    context,
                                    "Completa todos los campos y selecciona zona de entrega",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            if (selectedImageUri == null) {
                                Toast.makeText(
                                    context,
                                    "Selecciona una imagen",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            uploading = true

                            val productId = UUID.randomUUID().toString()

                            val imageRef = storage.reference
                                .child("productos")
                                .child(currentUser.uid)
                                .child("$productId.jpg")

                            imageRef.putFile(selectedImageUri!!)
                                .addOnSuccessListener {
                                    imageRef.downloadUrl
                                        .addOnSuccessListener { downloadUri ->

                                            val productData = hashMapOf(
                                                "id" to productId,
                                                "titulo" to title.trim(),
                                                "condicion" to condition,
                                                "tipo" to type,
                                                "precio" to price.trim(),
                                                "zonaEntrega" to zonaEntrega,
                                                "fechaEntrega" to fechaEntrega,
                                                "horaEntrega" to horaEntrega,
                                                "imagenUrl" to downloadUri.toString(),
                                                "vendedorUid" to currentUser.uid,
                                                "vendedorCorreo" to (currentUser.email ?: ""),
                                                "fechaPublicacion" to Timestamp.now(),
                                                "estado" to "disponible",
                                                "estadoRevision" to "pendiente",
                                                "motivoRechazo" to "",
                                                "fechaRevision" to null
                                            )

                                            db.collection("productos")
                                                .document(productId)
                                                .set(productData)
                                                .addOnSuccessListener {
                                                    uploading = false

                                                    Toast.makeText(
                                                        context,
                                                        "Producto enviado a revisión del administrador",
                                                        Toast.LENGTH_LONG
                                                    ).show()

                                                    navController.navigate(Screen.Home.route) {
                                                        popUpTo(0) {
                                                            inclusive = true
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener { error ->
                                                    uploading = false

                                                    Toast.makeText(
                                                        context,
                                                        "Error al guardar producto: ${error.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        }
                                        .addOnFailureListener { error ->
                                            uploading = false

                                            Toast.makeText(
                                                context,
                                                "Error al obtener URL: ${error.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                                .addOnFailureListener { error ->
                                    uploading = false

                                    Toast.makeText(
                                        context,
                                        "Error al subir imagen: ${error.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        },
                        enabled = !uploading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFA5F0B5)
                        ),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text(
                            text = if (uploading) {
                                "Enviando a revisión..."
                            } else {
                                stringResource(id = R.string.publish_action)
                            },
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    Text(
                        text = "Tu publicación será revisada por un administrador antes de aparecer en la página principal.",
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
fun ProductInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(label, fontSize = 14.sp)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF1F8E9),
            unfocusedContainerColor = Color(0xFFF1F8E9),
            focusedBorderColor = Color(0xFFC8E6C9),
            unfocusedBorderColor = Color(0xFFC8E6C9)
        ),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>
) {
    var expanded by remember {
        mutableStateOf(false)
    }

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
            placeholder = {
                Text(label, fontSize = 14.sp)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF1F8E9),
                unfocusedContainerColor = Color(0xFFF1F8E9),
                focusedBorderColor = Color(0xFFC8E6C9),
                unfocusedBorderColor = Color(0xFFC8E6C9)
            ),
            singleLine = true
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
                        Text(text = option)
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