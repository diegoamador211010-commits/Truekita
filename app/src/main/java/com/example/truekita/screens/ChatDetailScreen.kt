package com.example.truekita.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource // IMPORTANTE
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(navController: NavController) {
    var message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        // Usamos la etiqueta del vendedor Omar
                        Text(text = stringResource(id = R.string.seller_omar))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // Usamos la etiqueta "regresar" o "atrás"
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(id = R.string.more_options))
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Mensajes de ejemplo usando las etiquetas del XML
            ChatBubble(stringResource(id = R.string.sample_message_1), isMine = false)
            Spacer(modifier = Modifier.height(10.dp))
            ChatBubble(stringResource(id = R.string.sample_message_2), isMine = true)
            Spacer(modifier = Modifier.height(10.dp))
            ChatBubble(stringResource(id = R.string.sample_message_3), isMine = false)

            Spacer(modifier = Modifier.weight(1f))

            // Input de mensaje con hint traducido
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                placeholder = { Text(stringResource(id = R.string.write_message)) },
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    if (message.isNotEmpty()) {
                        IconButton(onClick = { message = "" }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFF3AA9B5))
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ChatBubble(text: String, isMine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isMine) Color(0xFFA8E6B2) else Color.White
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Text(text = text, modifier = Modifier.padding(14.dp), fontSize = 15.sp)
        }
    }
}