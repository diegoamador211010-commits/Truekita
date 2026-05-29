package com.example.truekita.navigation

sealed class Screen(val route: String) {

    // --- AUTH ---
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object ChangePassword : Screen("change_password_screen")
    object SavePassword : Screen("save_password_screen")
    object VerifyPhone : Screen("verify_phone_screen")
    object BiometricLogin : Screen("biometric_login_screen")
    object ForgotPassword : Screen("forgot_password_screen")

    // --- ADMIN ---
    object AdminHome : Screen("admin_home_screen")
    object AdminTickets : Screen("admin_tickets_screen")
    object AdminProducts : Screen("admin_products_screen")
    object AdminRoutes : Screen("admin_routes_screen")
    object AdminChats : Screen("admin_chats_screen")

    // --- PRINCIPALES ---
    object Home : Screen("home_screen")
    object Search : Screen("search_screen")
    object Profile : Screen("profile_screen")
    object Settings : Screen("settings_screen")

    // --- CHAT ---
    object ChatList : Screen("chat_list_screen")

    object ChatDetail : Screen("chat_detail_screen") {
        fun createRoute(
            chatId: String,
            otherUserName: String
        ): String {
            return "$route/$chatId/$otherUserName"
        }
    }

    // --- PRODUCTOS / VENTAS ---
    object PublishProduct : Screen("publish_product_screen")

    object ProductDetail : Screen("product_detail_screen") {
        fun createRoute(productId: String): String {
            return "$route/$productId"
        }
    }

    object Privacy : Screen("privacy_screen")
    object About : Screen("about_screen")
    object MyPosts : Screen("my_posts_screen")

    // --- RUTAS / TRANSPORTE ---
    object ViewRoute : Screen("view_route_screen")
    object PublishRoute : Screen("publish_route_screen")
    object MyRoutes : Screen("my_routes_screen")

    object RouteRequests : Screen("route_requests_screen") {
        fun createRoute(routeId: String): String {
            return "$route/$routeId"
        }
    }

    object MapasRuta : Screen("mapas_ruta_screen") {
        fun createRoute(routeId: String): String {
            return "$route/$routeId"
        }
    }

    // --- GOOGLE MAPS / UBICACIONES ---
    object LocationPicker : Screen("location_picker_screen")
    object MeetingPoints : Screen("meeting_points_screen")
    object ConfigurePoints : Screen("configure_points_screen")

    // --- PERFIL / AYUDA ---
    object Help : Screen("help_screen")
}