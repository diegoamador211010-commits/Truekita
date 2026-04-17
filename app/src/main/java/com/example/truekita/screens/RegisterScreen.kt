package com.example.truekita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource // IMPORTANTE
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.navigation.Screen

@Composable
fun RegisterScreen(navController: NavController) {
    // Estados para capturar lo que escribe el usuario
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val buttonGreen = Color(0xFF81C784)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFB2EBF2), Color(0xFFA5D6A7))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.create_account), // "Crear cuenta"
                    fontSize = 20.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Campos usando stringResource para que cambien de idioma
                RegisterTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(id = R.string.name_label)
                )
                RegisterTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = stringResource(id = R.string.institutional_email)
                )
                RegisterTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(id = R.string.new_password),
                    isPassword = true
                )
                RegisterTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = stringResource(id = R.string.repeat_new_password),
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón principal
                Button(
                    onClick = {
                        navController.navigate(Screen.Home.route)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonGreen),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.sign_in), // O una etiqueta de "Registrarse"
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enlace para regresar
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(
                        text = stringResource(id = R.string.no_account), // "¿Ya tienes cuenta? Iniciar sesión"
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        label = { Text(label, fontSize = 14.sp) },
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        // Agregamos transformación visual si es contraseña
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF81C784),
            unfocusedBorderColor = Color.LightGray
        )
    )
}