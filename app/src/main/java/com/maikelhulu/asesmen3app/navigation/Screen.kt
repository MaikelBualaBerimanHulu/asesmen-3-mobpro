package com.maikelhulu.asesmen3app.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Explore : Screen("explore")
    object Collection : Screen("collection")
    object AddItem : Screen("add_item")
    object ItemDetail : Screen("item_detail/{itemId}") {
        fun createRoute(itemId: String) = "item_detail/$itemId"
    }
}