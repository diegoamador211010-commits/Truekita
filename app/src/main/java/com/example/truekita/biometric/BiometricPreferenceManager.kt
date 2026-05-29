package com.example.truekita.biometric

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object BiometricPreferenceManager {

    private const val PREF_NAME = "truekita_biometric_preferences"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled_"

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED + userId, enabled)
            .apply()
    }

    fun isBiometricEnabled(context: Context): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false

        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_BIOMETRIC_ENABLED + userId, false)
    }

    fun clearBiometric(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_BIOMETRIC_ENABLED + userId)
            .apply()
    }
}