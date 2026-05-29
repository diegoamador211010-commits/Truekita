package com.example.truekita.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ForgotPasswordScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val lightBlue = Color(0xFFB2EBF2)
    val lightGreen = Color(0xFFA5D6A7)
    val buttonGreen = Color(0xFF81C784)

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            lightBlue,
                            lightGreen
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_truequita_logo),
                    contentDescription = "Logo Truekita",
                    modifier = Modifier.size(110.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.88f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Correo",
                            tint = buttonGreen,
                            modifier = Modifier.size(44.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Recuperar contraseña",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Escribe tu correo registrado. Firebase enviará un enlace a tu Gmail para restablecer tu contraseña.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Correo electrónico")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val correoLimpio = email.trim()

                                if (correoLimpio.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Ingresa tu correo electrónico",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
                                    Toast.makeText(
                                        context,
                                        "Ingresa un correo válido",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                cargando = true

                                auth.setLanguageCode("es")

                                auth.sendPasswordResetEmail(correoLimpio)
                                    .addOnCompleteListener { task ->
                                        cargando = false

                                        if (task.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Correo enviado. Revisa tu Gmail para restablecer tu contraseña.",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            navController.navigate(Screen.Login.route) {
                                                popUpTo(Screen.ForgotPassword.route) {
                                                    inclusive = true
                                                }
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "No se pudo enviar el correo. Verifica que el correo esté registrado.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                            },
                            enabled = !cargando,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonGreen
                            )
                        ) {
                            Text(
                                text = if (cargando) {
                                    "Enviando..."
                                } else {
                                    "Enviar enlace a Gmail"
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.ForgotPassword.route) {
                                        inclusive = true
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = "Volver al inicio de sesión",
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