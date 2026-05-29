package com.example.truekita.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.truekita.R
import com.example.truekita.components.AppTopBar
import com.example.truekita.components.BottomNavBar
import com.example.truekita.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val darkBlue = Color(0xFF0D47A1)

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var userName by remember { mutableStateOf("Usuario") }
    var userEmail by remember { mutableStateOf(currentUser?.email ?: "Sin correo") }

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid

        if (uid != null) {
            db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("nombre") ?: "Usuario"
                    userEmail = document.getString("correo") ?: currentUser.email ?: "Sin correo"
                }
        }
    }

    fun changeLanguage(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)

        navController.navigate(Screen.Home.route) {
            popUpTo(0) {
                inclusive = true
            }
        }
    }

    val accountOptions = listOf(
        SettingsOptionUi(
            text = "Mi perfil",
            route = Screen.Profile.route,
            icon = Icons.Default.Person
        ),
        SettingsOptionUi(
            text = stringResource(id = R.string.my_posts),
            route = Screen.MyPosts.route,
            icon = Icons.Default.Inventory
        ),
        SettingsOptionUi(
            text = stringResource(id = R.string.my_routes),
            route = Screen.MyRoutes.route,
            icon = Icons.Default.DirectionsCar
        )
    )

    val securityOptions = listOf(
        SettingsOptionUi(
            text = stringResource(id = R.string.change_password),
            route = Screen.ChangePassword.route,
            icon = Icons.Default.Lock
        ),
        SettingsOptionUi(
            text = stringResource(id = R.string.privacy),
            route = Screen.Privacy.route,
            icon = Icons.Default.Security
        )
    )

    val supportOptions = listOf(
        SettingsOptionUi(
            text = stringResource(id = R.string.help),
            route = Screen.Help.route,
            icon = Icons.Default.SupportAgent
        ),
        SettingsOptionUi(
            text = stringResource(id = R.string.about),
            route = Screen.About.route,
            icon = Icons.Default.Info
        )
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.settings),
                showBack = true,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFE3F2FD))
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFD7E5FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = darkBlue
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = userName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Text(
                                text = userEmail,
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                SectionTitle(text = "Idioma / Language")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = darkBlue
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Selecciona idioma",
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            LanguageBtn("Español") {
                                changeLanguage("es")
                            }

                            LanguageBtn("English") {
                                changeLanguage("en")
                            }

                            LanguageBtn("Português") {
                                changeLanguage("pt")
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle(text = "Cuenta")
            }

            items(accountOptions) { item ->
                SettingsItem(
                    option = item,
                    onClick = {
                        navController.navigate(item.route)
                    }
                )
            }

            item {
                SectionTitle(text = stringResource(id = R.string.security_section))
            }

            items(securityOptions) { item ->
                SettingsItem(
                    option = item,
                    onClick = {
                        navController.navigate(item.route)
                    }
                )
            }

            item {
                SectionTitle(text = stringResource(id = R.string.support_section))
            }

            items(supportOptions) { item ->
                SettingsItem(
                    option = item,
                    onClick = {
                        navController.navigate(item.route)
                    }
                )
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.Gray,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 10.dp, start = 8.dp)
    )
}

@Composable
fun LanguageBtn(
    name: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF0F4FF)
        ),
        modifier = Modifier.padding(2.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = name,
            color = Color(0xFF0D47A1),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingsItem(
    option: SettingsOptionUi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = Color(0xFF0D47A1)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = option.text,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

data class SettingsOptionUi(
    val text: String,
    val route: String,
    val icon: ImageVector
)