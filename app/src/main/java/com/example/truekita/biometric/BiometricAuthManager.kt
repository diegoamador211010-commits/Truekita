package com.example.truekita.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL

object BiometricAuthManager {

    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)

        val result = biometricManager.canAuthenticate(
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        )

        return result == BiometricManager.BIOMETRIC_SUCCESS
    }
}