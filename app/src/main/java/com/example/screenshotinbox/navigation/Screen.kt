package com.example.screenshotinbox.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Detail : Screen("detail/{id}") {
        fun createRoute(id: Long) = "detail/$id"
    }
    data object Categories : Screen("categories")
    data object CategoryEditor : Screen("categoryEditor/{id}") {
        fun createRoute(id: Long) = "categoryEditor/$id"
        const val NEW_ID = -1L
    }
    data object Settings : Screen("settings")
}
