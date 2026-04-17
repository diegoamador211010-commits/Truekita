package com.example.truekita.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import java.util.Locale

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val darkBlue = Color(0xFF0D47A1)

    // FUNCIÓN PARA CAMBIAR IDIOMA Y REINICIAR LA APP
    fun changeLanguage(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)

        // Actualizamos la configuración del sistema para la App
        resources.updateConfiguration(configuration, resources.displayMetrics)

        // IMPORTANTE: Reiniciamos a la pantalla de Inicio y limpiamos el historial
        // para que Compose cargue los nuevos strings desde cero.
        navController.navigate(Screen.Home.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    // LISTAS USANDO LOS STRINGS.XML
    val securityOptions = listOf(
        SettingsOptionUi(stringResource(id = R.string.change_password), Screen.ChangePassword.route),
        SettingsOptionUi(stringResource(id = R.string.privacy), null)
    )

    val supportOptions = listOf(
        SettingsOptionUi(stringResource(id = R.string.help), Screen.Help.route),
        SettingsOptionUi(stringResource(id = R.string.about), null)
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.settings),
                showBack = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFE3F2FD)) // Fondo azul claro ITA
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // TARJETA DE PERFIL
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(56.dp).background(Color(0xFFD7E5FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = darkBlue)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Diego", fontWeight = FontWeight.Bold)
                            Text(stringResource(id = R.string.sample_email), color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }

            // SECCIÓN DE IDIOMAS
            item {
                Text(
                    text = "Idioma / Language",
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp, start = 8.dp)
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LanguageBtn("Español") { changeLanguage("es") }
                        LanguageBtn("English") { changeLanguage("en") }
                        LanguageBtn("Português") { changeLanguage("pt") }
                    }
                }
            }

            // SECCIÓN SEGURIDAD
            item {
                Text(
                    text = stringResource(id = R.string.security_section),
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp, start = 8.dp)
                )
            }
            items(securityOptions) { item ->
                SettingsItem(item.text) { item.route?.let { navController.navigate(it) } }
            }

            // SECCIÓN SOPORTE
            item {
                Text(
                    text = stringResource(id = R.string.support_section),
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp, start = 8.dp)
                )
            }
            items(supportOptions) { item ->
                SettingsItem(item.text) { item.route?.let { navController.navigate(it) } }
            }
        }
    }
}

@Composable
fun LanguageBtn(name: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F4FF)),
        modifier = Modifier.padding(2.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(name, color = Color(0xFF0D47A1), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SettingsItem(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text, fontSize = 16.sp, color = Color.Black)
            // Podrías agregar una flechita aquí para que se vea más pro
        }
    }
}

data class SettingsOptionUi(val text: String, val route: String? = null)