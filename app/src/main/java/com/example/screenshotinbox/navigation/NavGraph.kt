package com.example.screenshotinbox.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.screenshotinbox.data.local.PreferencesManager
import com.example.screenshotinbox.data.repository.CategoryRepository
import com.example.screenshotinbox.data.repository.ScreenshotRepository
import com.example.screenshotinbox.ui.screens.CategoriesScreen
import com.example.screenshotinbox.ui.screens.CategoryEditorScreen
import com.example.screenshotinbox.ui.screens.HomeScreen
import com.example.screenshotinbox.ui.screens.OnboardingScreen
import com.example.screenshotinbox.ui.screens.ScreenshotDetailScreen
import com.example.screenshotinbox.ui.screens.SettingsScreen
import com.example.screenshotinbox.viewmodel.ViewModelFactory

@Composable
fun ScreenshotInboxNavGraph(
    screenshotRepository: ScreenshotRepository,
    categoryRepository: CategoryRepository,
    preferencesManager: PreferencesManager,
    navController: NavHostController = rememberNavController()
) {
    val onboardingDone by preferencesManager.onboardingDone.collectAsState(initial = null)
    val startDestination = when (onboardingDone) {
        null -> null // not loaded yet, don't render nav host
        true -> Screen.Home.route
        false -> Screen.Onboarding.route
    }

    val baseFactory = ViewModelFactory(screenshotRepository, categoryRepository, preferencesManager)

    if (startDestination == null) return

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                factory = baseFactory,
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                factory = baseFactory,
                onOpenDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                onOpenCategories = { navController.navigate(Screen.Categories.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: -1L
            val factory = ViewModelFactory(screenshotRepository, categoryRepository, preferencesManager, screenshotId = id)
            ScreenshotDetailScreen(factory = factory, onBack = { navController.popBackStack() })
        }

        composable(Screen.Categories.route) {
            CategoriesScreen(
                factory = baseFactory,
                onBack = { navController.popBackStack() },
                onEditCategory = { id -> navController.navigate(Screen.CategoryEditor.createRoute(id)) },
                onAddCategory = { navController.navigate(Screen.CategoryEditor.createRoute(Screen.CategoryEditor.NEW_ID)) }
            )
        }

        composable(
            route = Screen.CategoryEditor.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: Screen.CategoryEditor.NEW_ID
            CategoryEditorScreen(
                factory = baseFactory,
                categoryId = id,
                onDone = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                factory = baseFactory,
                onBack = { navController.popBackStack() },
                onManageCategories = { navController.navigate(Screen.Categories.route) }
            )
        }
    }
}
