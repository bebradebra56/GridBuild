package com.gridibuild.sfobud.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.gridibuild.sfobud.ui.screens.*
import com.gridibuild.sfobud.ui.screens.budget.BudgetScreen
import com.gridibuild.sfobud.ui.screens.calendar.CalendarScreen
import com.gridibuild.sfobud.ui.screens.contacts.ContactsScreen
import com.gridibuild.sfobud.ui.screens.home.HomeScreen
import com.gridibuild.sfobud.ui.screens.insights.InsightsScreen
import com.gridibuild.sfobud.ui.screens.materials.MaterialsScreen
import com.gridibuild.sfobud.ui.screens.measurements.MeasurementsScreen
import com.gridibuild.sfobud.ui.screens.more.MoreScreen
import com.gridibuild.sfobud.ui.screens.notifications.NotificationsScreen
import com.gridibuild.sfobud.ui.screens.photos.PhotosScreen
import com.gridibuild.sfobud.ui.screens.projects.ProjectsScreen
import com.gridibuild.sfobud.ui.screens.rooms.RoomDetailScreen
import com.gridibuild.sfobud.ui.screens.rooms.RoomsScreen
import com.gridibuild.sfobud.ui.screens.settings.SettingsScreen
import com.gridibuild.sfobud.ui.screens.shopping.ShoppingScreen
import com.gridibuild.sfobud.ui.screens.tasks.TasksScreen
import com.gridibuild.sfobud.viewmodel.AuthViewModel

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = { if (showBottomBar) BottomNavBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigate = { isOnboardingDone ->
                        if (!isOnboardingDone) {
                            navController.navigate(Screen.Onboarding.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                        } else {
                            navController.navigate(Screen.Home.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                        }
                    }
                )
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Screen.Home.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(Screen.Projects.route) {
                ProjectsScreen(navController = navController)
            }
            composable(Screen.Tasks.route) {
                TasksScreen(navController = navController)
            }
            composable(Screen.Budget.route) {
                BudgetScreen(navController = navController)
            }
            composable(Screen.More.route) {
                MoreScreen(navController = navController)
            }
            composable(Screen.Rooms.route) {
                RoomsScreen(navController = navController)
            }
            composable(Screen.RoomDetail.route) { backStack ->
                val roomId = backStack.arguments?.getString("roomId")?.toLongOrNull() ?: -1L
                RoomDetailScreen(roomId = roomId, navController = navController)
            }
            composable(Screen.Materials.route) {
                MaterialsScreen(navController = navController)
            }
            composable(Screen.Shopping.route) {
                ShoppingScreen(navController = navController)
            }
            composable(Screen.Measurements.route) {
                MeasurementsScreen(navController = navController)
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(navController = navController)
            }
            composable(Screen.Photos.route) {
                PhotosScreen(navController = navController)
            }
            composable(Screen.Contacts.route) {
                ContactsScreen(navController = navController)
            }
            composable(Screen.Insights.route) {
                InsightsScreen(navController = navController)
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
