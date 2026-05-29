package com.example.truekita.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.truekita.biometric.BiometricAuthManager
import com.example.truekita.biometric.BiometricPreferenceManager

@Composable
fun BiometricLoginScreen(
    onSuccess: () -> Unit,
    onUsePassword: () -> Unit
) {
    val context = LocalContext.current

    val activity = context as? FragmentActivity

    val executor = remember {
        ContextCompat.getMainExecutor(context)
    }

    fun showBiometricPrompt() {
        if (activity == null) {
            onUsePassword()
            return
        }

        if (!BiometricAuthManager.canAuthenticate(context)) {
            BiometricPreferenceManager.setBiometricEnabled(context, false)
            onUsePassword()
            return
        }

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)

                    if (
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                        errorCode == BiometricPrompt.ERROR_CANCELED
                    ) {
                        onUsePassword()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            }
        )

        val promptInfo = PromptInfo.Builder()
            .setTitle("Inicio seguro")
            .setSubtitle("Usa tu huella, rostro, PIN o patrón")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    LaunchedEffect(Unit) {
        showBiometricPrompt()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF7ADFF2),
                        Color(0xFFEAFBF0),
                        Color(0xFFA9F0D1)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.65f),
                        shape = CircleShape
                    )
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometría",
                    tint = Color(0xFF3AA9B5)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Inicio biométrico",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF246A73),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Desbloquea Truekita de forma segura.",
                color = Color(0xFF246A73)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    showBiometricPrompt()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3AA9B5)
                )
            ) {
                Text("Usar huella o rostro")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    onUsePassword()
                }
            ) {
                Text("Usar contraseña")
            }
        }
    }
}