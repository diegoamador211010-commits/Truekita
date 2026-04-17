package com.example.truekita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar

@Composable
fun ChangePasswordScreen(navController: NavController) {
    val lightBlue = Color(0xFFE3F2FD)

    // Estados para los campos
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var repeatPass by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.change_password),
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
                .background(lightBlue)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.change_password),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Campo: Contraseña Actual
            OutlinedTextField(
                value = currentPass,
                onValueChange = { currentPass = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.current_password)) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp)
            )

            // Campo: Contraseña Nueva
            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.new_password)) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp)
            )

            // Campo: Repetir Contraseña
            OutlinedTextField(
                value = repeatPass,
                onValueChange = { repeatPass = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.repeat_new_password)) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Botón Confirmar (Verde Truekita)
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA5F0B5))
            ) {
                Text(
                    text = stringResource(id = R.string.confirm),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            // Link "¿Olvidaste tu contraseña?"
            TextButton(onClick = { /* Acción */ }) {
                Text(
                    text = stringResource(id = R.string.forgot_password_small),
                    color = Color.Gray
                )
            }
        }
    }
}