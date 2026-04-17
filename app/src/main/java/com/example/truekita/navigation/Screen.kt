package com.example.truekita.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object Home : Screen("home_screen")
    object Search : Screen("search_screen")
    object Profile : Screen("profile_screen")
    object ChatList : Screen("chat_list_screen")
    object ChatDetail : Screen("chat_detail_screen")
    object ProductDetail : Screen("product_detail_screen") // Ruta base
    object Settings : Screen("settings_screen")
    object Notifications : Screen("notifications_screen")
    object ChangePassword : Screen("change_password_screen")
    object ViewRoute : Screen("view_route_screen")
    object MapasRuta : Screen("mapas_ruta_screen")
    object PublishRoute : Screen("publish_route_screen")
    object PublishProduct : Screen("publish_product_screen")
    object MeetingPoints : Screen("meeting_points_screen")
    object Help : Screen("help_screen")
    object MyPosts : Screen("my_posts_screen")
    object MyRoutes : Screen("my_routes_screen")
}