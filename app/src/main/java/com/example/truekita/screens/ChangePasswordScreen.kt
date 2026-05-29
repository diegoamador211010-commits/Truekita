package com.example.truekita.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.navigation.Screen
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    val lightBlue = Color(0xFFE3F2FD)

    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var repeatPass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.change_password),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
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

            OutlinedTextField(
                value = currentPass,
                onValueChange = {
                    currentPass = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(id = R.string.current_password))
                },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = newPass,
                onValueChange = {
                    newPass = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(id = R.string.new_password))
                },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = repeatPass,
                onValueChange = {
                    repeatPass = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(id = R.string.repeat_new_password))
                },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (user == null) {
                        Toast.makeText(
                            context,
                            "No hay usuario activo",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    val email = user.email

                    if (email.isNullOrBlank()) {
                        Toast.makeText(
                            context,
                            "No se pudo obtener el correo del usuario",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (currentPass.isBlank() || newPass.isBlank() || repeatPass.isBlank()) {
                        Toast.makeText(
                            context,
                            "Completa todos los campos",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (newPass != repeatPass) {
                        Toast.makeText(
                            context,
                            "Las contraseñas no coinciden",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (newPass.length < 6) {
                        Toast.makeText(
                            context,
                            "La nueva contraseña debe tener mínimo 6 caracteres",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    loading = true

                    val credential = EmailAuthProvider.getCredential(email, currentPass)

                    user.reauthenticate(credential)
                        .addOnSuccessListener {
                            user.updatePassword(newPass)
                                .addOnSuccessListener {
                                    loading = false

                                    auth.signOut()

                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0) {
                                            inclusive = true
                                        }
                                    }
                                }
                                .addOnFailureListener { error ->
                                    loading = false

                                    Toast.makeText(
                                        context,
                                        "Error al actualizar contraseña: ${error.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                        .addOnFailureListener {
                            loading = false

                            Toast.makeText(
                                context,
                                "Contraseña actual incorrecta",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                },
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA5F0B5)
                )
            ) {
                Text(
                    text = if (loading) "Guardando..." else stringResource(id = R.string.confirm),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = {
                    val email = user?.email

                    if (email.isNullOrBlank()) {
                        Toast.makeText(
                            context,
                            "No se pudo obtener el correo del usuario",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@TextButton
                    }

                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Correo de recuperación enviado",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(
                                context,
                                "Error: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            ) {
                Text(
                    text = stringResource(id = R.string.forgot_password_small),
                    color = Color.Gray
                )
            }
        }
    }
}