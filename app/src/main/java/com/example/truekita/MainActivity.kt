package com.example.truekita

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.truekita.components.NotificationBadgeIcon
import com.example.truekita.navigation.NavGraph
import com.example.truekita.navigation.Screen
import com.example.truekita.screens.NotificationsBottomSheet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TruekitaApp()
        }
    }
}

@Composable
fun TruekitaApp() {
    val navController = rememberNavController()

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var showNotifications by remember {
        mutableStateOf(false)
    }

    var currentUserUid by remember {
        mutableStateOf<String?>(null)
    }

    var pendingNotifications by remember {
        mutableIntStateOf(0)
    }

    DisposableEffect(Unit) {
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUserUid = firebaseAuth.currentUser?.uid
        }

        auth.addAuthStateListener(authListener)

        onDispose {
            auth.removeAuthStateListener(authListener)
        }
    }

    DisposableEffect(currentUserUid) {
        var listener: ListenerRegistration? = null

        if (currentUserUid == null) {
            pendingNotifications = 0
        } else {
            listener = db.collection("notificaciones")
                .whereEqualTo("usuarioUid", currentUserUid)
                .whereEqualTo("leida", false)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        pendingNotifications = 0
                        return@addSnapshotListener
                    }

                    pendingNotifications = snapshot?.size() ?: 0
                }
        }

        onDispose {
            listener?.remove()
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val screensWithoutNotifications = listOf(
        Screen.Splash.route,
        Screen.Login.route,
        Screen.Register.route,
        Screen.ChangePassword.route,
        Screen.SavePassword.route,
        Screen.VerifyPhone.route,
        Screen.Settings.route,
        Screen.ForgotPassword.route,
        Screen.BiometricLogin.route
    )

    val mostrarCampana = currentRoute !in screensWithoutNotifications

    LaunchedEffect(currentRoute) {
        if (!mostrarCampana) {
            showNotifications = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        NavGraph(
            navController = navController,
            pendingNotifications = pendingNotifications,
            onOpenNotifications = {
                if (mostrarCampana) {
                    showNotifications = true
                }
            }
        )

        if (mostrarCampana && !showNotifications) {
            FloatingActionButton(
                onClick = {
                    showNotifications = true
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 32.dp, end = 16.dp)
            ) {
                NotificationBadgeIcon(
                    pendingCount = pendingNotifications,
                    contentDescription = "Notificaciones"
                )
            }
        }

        if (mostrarCampana && showNotifications) {
            NotificationsBottomSheet(
                onDismiss = {
                    showNotifications = false
                }
            )
        }
    }
}