package com.example.truekita.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var cargando by remember {
        mutableStateOf(false)
    }

    val buttonGreen = Color(0xFF81C784)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFB2EBF2),
                        Color(0xFFA5D6A7)
                    )
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
                    text = stringResource(id = R.string.create_account),
                    fontSize = 20.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                RegisterTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = stringResource(id = R.string.name_label)
                )

                RegisterTextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    label = stringResource(id = R.string.institutional_email),
                    keyboardType = KeyboardType.Email
                )

                RegisterTextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    label = stringResource(id = R.string.new_password),
                    isPassword = true,
                    keyboardType = KeyboardType.Password
                )

                RegisterTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                    },
                    label = stringResource(id = R.string.repeat_new_password),
                    isPassword = true,
                    keyboardType = KeyboardType.Password
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (
                            name.isBlank() ||
                            email.isBlank() ||
                            password.isBlank() ||
                            confirmPassword.isBlank()
                        ) {
                            Toast.makeText(
                                context,
                                "Completa todos los campos",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (password != confirmPassword) {
                            Toast.makeText(
                                context,
                                "Las contraseñas no coinciden",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (password.length < 6) {
                            Toast.makeText(
                                context,
                                "La contraseña debe tener mínimo 6 caracteres",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        cargando = true

                        auth.createUserWithEmailAndPassword(
                            email.trim(),
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

                                val datosUsuario = hashMapOf(
                                    "uid" to uid,
                                    "nombre" to name.trim(),
                                    "correo" to email.trim(),
                                    "rol" to "usuario",
                                    "telefonoVerificado" to false,
                                    "fechaRegistro" to Timestamp.now()
                                )

                                db.collection("usuarios")
                                    .document(uid)
                                    .set(datosUsuario)
                                    .addOnCompleteListener { firestoreTask ->
                                        cargando = false

                                        if (firestoreTask.isSuccessful) {
                                            navController.navigate(Screen.VerifyPhone.route) {
                                                popUpTo(Screen.Register.route) {
                                                    inclusive = true
                                                }
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Error al guardar usuario: ${firestoreTask.exception?.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                            } else {
                                cargando = false

                                Toast.makeText(
                                    context,
                                    "Error al crear cuenta: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonGreen
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = if (cargando) {
                            "Creando cuenta..."
                        } else {
                            "Crear cuenta"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Text(
                        text = "¿Ya tienes cuenta? Iniciar sesión",
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
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        label = {
            Text(
                text = label,
                fontSize = 14.sp
            )
        },
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF81C784),
            unfocusedBorderColor = Color.LightGray
        )
    )
}