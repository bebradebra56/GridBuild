package com.gridibuild.sfobud.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Projects : Screen("projects")
    object Tasks : Screen("tasks")
    object Budget : Screen("budget")
    object More : Screen("more")
    object Rooms : Screen("rooms")
    object RoomDetail : Screen("room_detail/{roomId}") {
        fun createRoute(roomId: Long) = "room_detail/$roomId"
    }
    object Materials : Screen("materials")
    object Shopping : Screen("shopping")
    object Measurements : Screen("measurements")
    object Calendar : Screen("calendar")
    object Photos : Screen("photos")
    object Contacts : Screen("contacts")
    object Insights : Screen("insights")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
}

val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Projects.route,
    Screen.Tasks.route,
    Screen.Budget.route,
    Screen.More.route
)
