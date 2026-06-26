package com.maikelhulu.asesmen3app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maikelhulu.asesmen3app.screen.*

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        // MAIN SHELL DENGAN BOTTOM NAV
        composable(route = Screen.Main.route) {
            MainShell(navController = navController)
        }

        // ADD ITEM SCREEN
        composable(route = Screen.AddItem.route) {
            AddItemScreen(onNavigateBack = { navController.popBackStack() })
        }

        // DETAIL SCREEN DENGAN ARGUMEN
        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            ItemDetailScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}