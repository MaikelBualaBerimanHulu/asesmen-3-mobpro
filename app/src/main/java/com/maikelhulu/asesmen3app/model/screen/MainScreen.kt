package com.maikelhulu.asesmen3app.model.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.maikelhulu.asesmen3app.model.ApiStatus
import com.maikelhulu.asesmen3app.model.util.SettingsDataStore
import com.maikelhulu.asesmen3app.model.util.UserDataStore
import com.maikelhulu.asesmen3app.model.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isGridLayout by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var userEmail by remember { mutableStateOf<String?>(null) }

    val apiStatus by viewModel.apiStatus.collectAsState()
    val items by viewModel.items.collectAsState()

    LaunchedEffect(Unit) {
        SettingsDataStore.getLayoutPreference(context).collect { isGrid ->
            isGridLayout = isGrid
        }
    }

    LaunchedEffect(Unit) {
        UserDataStore.getUserSession(context).collect { session ->
            userEmail = session["email"]
            if (userEmail != null) {
                viewModel.observeLocalItems(userEmail!!)
                viewModel.fetchAndSyncData(userEmail!!)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyCollection") },
                actions = {
                    IconButton(onClick = {
                        if (userEmail == null) showLoginDialog = true
                        else { /* TODO: Show Profile Dialog */ }
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Profil")
                    }

                    IconButton(onClick = {
                        coroutineScope.launch {
                            isGridLayout = !isGridLayout
                            SettingsDataStore.saveLayoutPreference(context, isGridLayout)
                        }
                    }) {
                        Icon(
                            imageVector = if (isGridLayout) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                            contentDescription = "Toggle Layout"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Trigger Camera Launcher */ }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Item")
            }
        }
    ) { paddingValues ->
        when (apiStatus) {
            ApiStatus.LOADING -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            ApiStatus.FAILED -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Gagal memuat data. Cek koneksi internet.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.retryFetch(userEmail ?: "") }) {
                        Text("Coba Lagi")
                    }
                }
            }
            ApiStatus.SUCCESS -> {
                if (items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("Belum ada koleksi. Tambahkan sekarang!", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    if (isGridLayout) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.padding(paddingValues),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(items) { item ->
                                Card(modifier = Modifier.aspectRatio(1f)) {
                                    AsyncImage(
                                        model = item.url,
                                        contentDescription = "Item Image",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(paddingValues),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(items) { item ->
                                ListItem(
                                    headlineContent = { Text(item.id) },
                                    supportingContent = { Text("${item.width}x${item.height}") }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text("Login Diperlukan") },
            text = { Text("Silakan login untuk mengakses koleksi pribadi Anda.") },
            confirmButton = {
                Button(onClick = {
                    // TODO: Call Google Sign-In from Module 14
                    showLoginDialog = false
                }) { Text("Login") }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) { Text("Batal") }
            }
        )
    }
}
