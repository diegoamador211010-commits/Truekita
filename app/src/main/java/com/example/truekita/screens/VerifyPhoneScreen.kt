package com.example.truekita.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.navigation.Screen
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

@Composable
fun VerifyPhoneScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as Activity

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var phoneNumber by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val lightBlue = Color(0xFFB2EBF2)
    val lightGreen = Color(0xFFA5D6A7)
    val buttonGreen = Color(0xFF81C784)

    fun irAHome() {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Login.route) {
                inclusive = true
            }
        }
    }

    fun marcarTelefonoVerificado(uid: String, telefono: String) {
        db.collection("usuarios")
            .document(uid)
            .update(
                mapOf(
                    "telefonoVerificado" to true,
                    "telefono" to telefono
                )
            )
            .addOnCompleteListener { updateTask ->
                loading = false

                if (updateTask.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Verificación completada",
                        Toast.LENGTH_SHORT
                    ).show()

                    irAHome()
                } else {
                    Toast.makeText(
                        context,
                        "Teléfono verificado, pero no se pudo actualizar Firestore: ${updateTask.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    fun verificarCredencial(credential: PhoneAuthCredential) {
        val user = auth.currentUser

        if (user == null) {
            loading = false
            Toast.makeText(
                context,
                "No hay usuario activo. Inicia sesión nuevamente.",
                Toast.LENGTH_LONG
            ).show()

            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Login.route) {
                    inclusive = true
                }
            }
            return
        }

        user.linkWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    marcarTelefonoVerificado(user.uid, phoneNumber)
                } else {
                    val exception = task.exception

                    /*
                     * Si el teléfono ya estaba vinculado anteriormente,
                     * no intentamos vincularlo otra vez.
                     * Solo marcamos el usuario como verificado en Firestore.
                     */
                    if (
                        exception is FirebaseAuthUserCollisionException ||
                        exception?.message?.contains("already", ignoreCase = true) == true ||
                        exception?.message?.contains("credential", ignoreCase = true) == true
                    ) {
                        marcarTelefonoVerificado(user.uid, phoneNumber)
                    } else {
                        loading = false

                        Toast.makeText(
                            context,
                            "Código incorrecto o error al verificar: ${exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(lightBlue, lightGreen)
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
                    text = "Verificación de dos pasos",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Ingresa tu número con código de país. Ejemplo: +5214491234567",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Número de teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (phoneNumber.isBlank()) {
                            Toast.makeText(
                                context,
                                "Ingresa un número",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (!phoneNumber.startsWith("+")) {
                            Toast.makeText(
                                context,
                                "Usa formato internacional. Ejemplo: +5214491234567",
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }

                        loading = true

                        val callbacks =
                            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                override fun onVerificationCompleted(
                                    credential: PhoneAuthCredential
                                ) {
                                    verificarCredencial(credential)
                                }

                                override fun onVerificationFailed(e: FirebaseException) {
                                    loading = false

                                    Toast.makeText(
                                        context,
                                        "Error al enviar código: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                override fun onCodeSent(
                                    id: String,
                                    token: PhoneAuthProvider.ForceResendingToken
                                ) {
                                    loading = false
                                    verificationId = id
                                    codeSent = true

                                    Toast.makeText(
                                        context,
                                        "Código enviado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        val options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(activity)
                            .setCallbacks(callbacks)
                            .build()

                        PhoneAuthProvider.verifyPhoneNumber(options)
                    },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonGreen
                    )
                ) {
                    Text(
                        text = if (loading) "Enviando..." else "Enviar código",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (codeSent) {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Código SMS") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (code.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Ingresa el código",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            if (verificationId.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Primero solicita el código",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            loading = true

                            val credential = PhoneAuthProvider.getCredential(
                                verificationId,
                                code
                            )

                            verificarCredencial(credential)
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonGreen
                        )
                    ) {
                        Text(
                            text = if (loading) "Verificando..." else "Verificar y entrar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Volver al inicio de sesión",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}