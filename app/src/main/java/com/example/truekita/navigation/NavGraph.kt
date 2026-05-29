package com.example.truekita.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.truekita.screens.*
import com.example.truekita.screens.admin.AdminHomeScreen
import com.example.truekita.screens.admin.AdminProductsScreen
import com.example.truekita.screens.admin.AdminRoutesScreen
import com.example.truekita.screens.admin.AdminTicketsScreen
import com.example.truekita.screens.admin.AdminChatsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    pendingNotifications: Int = 0,
    onOpenNotifications: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        // --- AUTH ---
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController)
        }


        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(navController)
        }

        composable(Screen.SavePassword.route) {
            SavePasswordScreen(navController)
        }

        composable(Screen.BiometricLogin.route) {
            BiometricLoginScreen(
                onSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.BiometricLogin.route) {
                            inclusive = true
                        }
                    }
                },
                onUsePassword = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.BiometricLogin.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.VerifyPhone.route) {
            VerifyPhoneScreen(navController)
        }

        // --- ADMIN ---
        composable(Screen.AdminHome.route) {
            AdminHomeScreen(navController)
        }

        composable(Screen.AdminTickets.route) {
            AdminTicketsScreen(navController)
        }

        composable(Screen.AdminProducts.route) {
            AdminProductsScreen(navController)
        }

        composable(Screen.AdminRoutes.route) {
            AdminRoutesScreen(navController)
        }

        composable(Screen.AdminChats.route) {
            AdminChatsScreen(navController)
        }

        // --- PRINCIPALES ---
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                pendingNotifications = pendingNotifications,
                onOpenNotifications = onOpenNotifications
            )
        }

        composable(Screen.ChatList.route) {
            ChatListScreen(navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }

        composable(Screen.Search.route) {
            SearchScreen(navController)
        }

        // --- TRANSPORTE / RUTAS ---
        composable(Screen.ViewRoute.route) {
            ViewRouteScreen(navController)
        }

        composable(Screen.MapasRuta.route) {
            MapasRuta(
                navController = navController,
                routeId = ""
            )
        }

        composable("${Screen.MapasRuta.route}/{routeId}") { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: ""

            MapasRuta(
                navController = navController,
                routeId = routeId
            )
        }

        composable(Screen.PublishRoute.route) {
            PublishRouteScreen(navController)
        }

        composable(Screen.MyRoutes.route) {
            MyRoutesScreen(navController)
        }

        composable("${Screen.RouteRequests.route}/{routeId}") { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: ""

            RouteRequestsScreen(
                navController = navController,
                routeId = routeId
            )
        }

        // --- GOOGLE MAPS / UBICACIONES ---
        composable(Screen.LocationPicker.route) {
            LocationPickerScreen(
                navController = navController,
                tipoUbicacion = "punto_entrega",
                tipoUsuario = "usuario"
            )
        }

        composable(Screen.MeetingPoints.route) {
            MeetingPointsScreen(
                navController = navController,
                title = "Puntos de Encuentro"
            )
        }

        composable(Screen.ConfigurePoints.route) {
            ConfigurePointsScreen(navController)
        }

        // --- PRODUCTOS ---
        composable(Screen.PublishProduct.route) {
            PublishProductScreen(navController)
        }

        composable(Screen.MyPosts.route) {
            MyPostsScreen(navController)
        }

        composable("${Screen.ProductDetail.route}/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""

            ProductDetailScreen(
                navController = navController,
                productId = productId
            )
        }

        // --- PERFIL / AYUDA ---
        composable(Screen.Help.route) {
            HelpScreen(navController)
        }

        composable(Screen.Privacy.route) {
            PrivacyScreen(navController)
        }

        composable(Screen.About.route) {
            AboutScreen(navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }

        // --- CHAT ---
        composable(Screen.ChatDetail.route) {
            ChatDetailScreen(
                navController = navController,
                chatId = "general",
                otherUserName = "Chat"
            )
        }

        composable("${Screen.ChatDetail.route}/{chatId}/{otherUserName}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: "general"

            val otherUserNameEncoded =
                backStackEntry.arguments?.getString("otherUserName") ?: "Chat"

            val otherUserName = Uri.decode(otherUserNameEncoded)

            ChatDetailScreen(
                navController = navController,
                chatId = chatId,
                otherUserName = otherUserName
            )
        }
    }
}