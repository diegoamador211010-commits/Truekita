package com.example.truekita.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.navigation.Screen

@Composable
fun LoginScreen(
    navController: NavController
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    val lightBlue = Color(0xFFB2EBF2)
    val lightGreen = Color(0xFFA5D6A7)
    val buttonGreen = Color(0xFF81C784)

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(lightBlue, lightGreen))),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                // Logo de Truekita
                Image(
                    painter = painterResource(id = R.drawable.ic_truequita_logo),
                    contentDescription = "Logo Truequita",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.login_title),
                            fontSize = 20.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        OutlinedTextField(
                            value = email.value,
                            onValueChange = { email.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.institutional_email)) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password.value,
                            onValueChange = { password.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.password)) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        // --- BOTÓN OLVIDASTE CONTRASEÑA ---
                        TextButton(
                            onClick = {
                                // Esta línea DEBE estar así para que funcione
                                navController.navigate(Screen.ChangePassword.route)
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = stringResource(R.string.forgot_password),
                                fontSize = 12.sp,
                                color = buttonGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // --- BOTÓN INICIAR SESIÓN (Home) ---
                        Button(
                            onClick = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonGreen)
                        ) {
                            Text(text = stringResource(R.string.sign_in), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = stringResource(R.string.no_account), fontSize = 12.sp, color = Color.Gray)

                        // --- BOTÓN CREAR CUENTA ---
                        TextButton(
                            onClick = {
                                // Esta línea DEBE estar así para que funcione
                                navController.navigate(Screen.Register.route)
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.create_account),
                                color = buttonGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}