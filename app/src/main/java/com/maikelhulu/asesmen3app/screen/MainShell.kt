package com.maikelhulu.asesmen3app.screen

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost // Import eksplisit
import androidx.navigation.compose.composable // Import eksplisit
import androidx.navigation.compose.currentBackStackEntryAsState
import com.maikelhulu.asesmen3app.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(Screen.Explore.route) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(96.dp)
            ) {
                NavigationBarItem(
                    selected = selectedTab == Screen.Explore.route,
                    onClick = {
                        selectedTab = Screen.Explore.route
                        navController.navigate(Screen.Explore.route) {
                            popUpTo(Screen.Main.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Explore", modifier = Modifier.size(30.dp)) },
                    label = { Text("Explore", style = MaterialTheme.typography.titleMedium) }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.AddItem.route) },
                    icon = {
                        FloatingActionButton(
                            onClick = { navController.navigate(Screen.AddItem.route) },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(34.dp))
                        }
                    },
                    label = null
                )

                NavigationBarItem(
                    selected = selectedTab == Screen.Collection.route,
                    onClick = {
                        selectedTab = Screen.Collection.route
                        navController.navigate(Screen.Collection.route) {
                            popUpTo(Screen.Main.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = "Koleksi", modifier = Modifier.size(30.dp)) },
                    label = { Text("Koleksi", style = MaterialTheme.typography.titleMedium) }
                )

                NavigationBarItem(
                    selected = selectedTab == Screen.Profile.route,
                    onClick = {
                        selectedTab = Screen.Profile.route
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Main.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil", modifier = Modifier.size(30.dp)) },
                    label = { Text("Profil", style = MaterialTheme.typography.titleMedium) }
                )
            }
        }
    ) { paddingValues ->
        // Nested NavHost untuk tabs
        NavHost(
            navController = navController,
            startDestination = Screen.Explore.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Explore.route) { ExploreScreen(navController) }
            composable(Screen.Collection.route) { CollectionScreen(navController) }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
