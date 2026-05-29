package com.example.truekita.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.biometric.BiometricPreferenceManager
import com.example.truekita.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(
    navController: NavController
) {
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var cargando by remember {
        mutableStateOf(false)
    }

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
                    contentDescription = "Logo Truequita",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.85f)
                    )
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
                            value = email,
                            onValueChange = {
                                email = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(stringResource(R.string.institutional_email))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(stringResource(R.string.password))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            )
                        )

                        TextButton(
                            onClick = {
                                navController.navigate(Screen.ForgotPassword.route)
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

                        Button(
                            onClick = {
                                val correoLimpio = email.trim()

                                if (correoLimpio.isBlank() || password.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Ingresa correo y contraseña",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                cargando = true

                                auth.signInWithEmailAndPassword(
                                    correoLimpio,
                                    password
                                ).addOnCompleteListener { task ->

                                    if (task.isSuccessful) {
                                        val uid = auth.currentUser?.uid

                                        if (uid == null) {
                                            cargando = false

                                            Toast.makeText(
                                                context,
                                                "No se pudo obtener el usuario",
                                                Toast.LENGTH_LONG
                                            ).show()

                                            return@addOnCompleteListener
                                        }

                                        db.collection("usuarios")
                                            .document(uid)
                                            .get()
                                            .addOnSuccessListener { document ->
                                                cargando = false

                                                val rol = document.getString("rol") ?: "usuario"

                                                BiometricPreferenceManager.setBiometricEnabled(
                                                    context = context,
                                                    enabled = true
                                                )

                                                Toast.makeText(
                                                    context,
                                                    "Inicio de sesión correcto",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                if (rol == "admin") {
                                                    navController.navigate(Screen.AdminHome.route) {
                                                        popUpTo(Screen.Login.route) {
                                                            inclusive = true
                                                        }
                                                    }
                                                } else {
                                                    navController.navigate(Screen.Home.route) {
                                                        popUpTo(Screen.Login.route) {
                                                            inclusive = true
                                                        }
                                                    }
                                                }
                                            }
                                            .addOnFailureListener { error ->
                                                cargando = false

                                                Toast.makeText(
                                                    context,
                                                    "Error al revisar rol: ${error.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                    } else {
                                        cargando = false

                                        Toast.makeText(
                                            context,
                                            "Correo o contraseña incorrectos",
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
                                    "Iniciando..."
                                } else {
                                    stringResource(R.string.sign_in)
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.no_account),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        TextButton(
                            onClick = {
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