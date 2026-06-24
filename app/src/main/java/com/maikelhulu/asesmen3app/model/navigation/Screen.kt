package com.maikelhulu.asesmen3app.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
}