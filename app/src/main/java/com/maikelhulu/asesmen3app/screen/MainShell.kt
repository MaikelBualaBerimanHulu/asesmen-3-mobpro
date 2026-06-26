package com.maikelhulu.asesmen3app.screen

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost // Import eksplisit
import androidx.navigation.compose.composable // Import eksplisit
import androidx.navigation.compose.rememberNavController
import com.maikelhulu.asesmen3app.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(rootNavController: NavHostController) {
    val tabNavController = rememberNavController()
    var selectedTab by remember { mutableStateOf(Screen.Explore.route) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { rootNavController.navigate(Screen.AddItem.route) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(34.dp))
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(88.dp)
            ) {
                NavigationBarItem(
                    selected = selectedTab == Screen.Explore.route,
                    onClick = {
                        selectedTab = Screen.Explore.route
                        tabNavController.navigate(Screen.Explore.route) {
                            popUpTo(Screen.Explore.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Jelajah", modifier = Modifier.size(30.dp)) },
                    label = { Text("Jelajah", style = MaterialTheme.typography.labelLarge) }
                )

                NavigationBarItem(
                    selected = selectedTab == Screen.Collection.route,
                    onClick = {
                        selectedTab = Screen.Collection.route
                        tabNavController.navigate(Screen.Collection.route) {
                            popUpTo(Screen.Explore.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Koleksi", modifier = Modifier.size(30.dp)) },
                    label = { Text("Koleksi", style = MaterialTheme.typography.labelLarge) }
                )

                NavigationBarItem(
                    selected = selectedTab == Screen.Facts.route,
                    onClick = {
                        selectedTab = Screen.Facts.route
                        tabNavController.navigate(Screen.Facts.route) {
                            popUpTo(Screen.Explore.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = "Fakta", modifier = Modifier.size(30.dp)) },
                    label = { Text("Fakta", style = MaterialTheme.typography.labelLarge) }
                )

                NavigationBarItem(
                    selected = selectedTab == Screen.Profile.route,
                    onClick = {
                        selectedTab = Screen.Profile.route
                        tabNavController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Explore.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil", modifier = Modifier.size(30.dp)) },
                    label = { Text("Profil", style = MaterialTheme.typography.labelLarge) }
                )
            }
        }
    ) { paddingValues ->
        // Nested NavHost untuk tabs
        NavHost(
            navController = tabNavController,
            startDestination = Screen.Explore.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Explore.route) { ExploreScreen(rootNavController) }
            composable(Screen.Collection.route) { CollectionScreen(rootNavController) }
            composable(Screen.Facts.route) { FactsScreen() }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
