package com.example.truekita.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.truekita.screens.*

@Composable
fun NavGraph(
    navController: NavHostController,
    onOpenNotifications: () -> Unit = {} // Para que el botón de la campana funcione en la Home
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // --- AUTH ---
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.ChangePassword.route) { ChangePasswordScreen(navController) }

        // --- PRINCIPALES ---
        composable(Screen.Home.route) { HomeScreen(navController, onOpenNotifications) }
        composable(Screen.ChatList.route) { ChatListScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.Search.route) { SearchScreen(navController) }

        // --- TRANSPORTE ---
        composable(Screen.ViewRoute.route) { ViewRouteScreen(navController) }
        composable(Screen.MapasRuta.route) { MapasRuta(navController) }
        composable(Screen.PublishRoute.route) { PublishRouteScreen(navController) }

        // --- VENTAS ---
        composable(Screen.PublishProduct.route) { PublishProductScreen(navController) }
        composable(Screen.MeetingPoints.route) {
            MeetingPointsScreen(navController, title = "Puntos de Encuentro")
        }

        // --- PANTALLAS DEL PERFIL (AGREGADAS PARA EVITAR EL CIERRE) ---
        composable(Screen.MyPosts.route) { MyPostsScreen(navController) }
        composable(Screen.MyRoutes.route) { MyRoutesScreen(navController) }
        composable(Screen.Help.route) { HelpScreen(navController) }

        // --- DETALLES DINÁMICOS ---
        composable(route = Screen.ProductDetail.route + "/{productTitle}") { backStackEntry ->
            val title = backStackEntry.arguments?.getString("productTitle") ?: ""
            ProductDetailScreen(navController, title)
        }

        // --- OTROS ---
        composable(Screen.ChatDetail.route) { ChatDetailScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
    }
}