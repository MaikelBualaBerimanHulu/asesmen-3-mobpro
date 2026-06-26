package com.maikelhulu.asesmen3app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.maikelhulu.asesmen3app.screen.AddItemScreen
import com.maikelhulu.asesmen3app.screen.MainScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(route = Screen.Main.route) {
            MainScreen(navController = navController)
        }

        composable(route = Screen.AddItem.route) {
            AddItemScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}