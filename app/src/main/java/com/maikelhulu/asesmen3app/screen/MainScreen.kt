package com.maikelhulu.asesmen3app.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.maikelhulu.asesmen3app.model.ApiStatus
import com.maikelhulu.asesmen3app.model.Item
import com.maikelhulu.asesmen3app.util.SettingsDataStore
import com.maikelhulu.asesmen3app.util.UserDataStore
import com.maikelhulu.asesmen3app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isGridLayout by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var hasFetchedData by remember { mutableStateOf(false) }

    // State untuk dialog konfirmasi hapus
    var itemToDelete by remember { mutableStateOf<Item?>(null) }

    var userEmail by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf<String?>(null) }
    var userPhoto by remember { mutableStateOf<String?>(null) }

    val apiStatus by viewModel.apiStatus.collectAsState()
    val items by viewModel.items.collectAsState()

    // Ambil preferensi layout
    LaunchedEffect(Unit) {
        SettingsDataStore.getLayoutPreference(context).collect { isGrid ->
            isGridLayout = isGrid
        }
    }

    // Ambil sesi user dan fetch data HANYA sekali saat login pertama kali
    LaunchedEffect(Unit) {
        UserDataStore.getUserSession(context).collect { session ->
            userEmail = session["email"]
            userName = session["name"]
            userPhoto = session["photo_url"]

            if (userEmail != null && !hasFetchedData) {
                viewModel.observeLocalItems(userEmail!!)
                viewModel.fetchAndSyncData(userEmail!!)
                hasFetchedData = true
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
                        else showProfileDialog = true
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
                            imageVector = if (isGridLayout) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle Layout"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (userEmail != null) navController.navigate("add_item")
                else showLoginDialog = true
            }) {
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
                    Text("Gagal memuat data.", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        hasFetchedData = false
                        viewModel.retryFetch(userEmail ?: "")
                    }) { Text("Coba Lagi") }
                }
            }
            ApiStatus.SUCCESS -> {
                if (items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("Belum ada koleksi.", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    if (isGridLayout) {
                        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.padding(paddingValues), contentPadding = PaddingValues(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(items) { item ->
                                Card(modifier = Modifier.aspectRatio(1f)) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        AsyncImage(model = item.url, contentDescription = null, modifier = Modifier.fillMaxSize())
                                        // Tombol hapus hanya muncul untuk item lokal
                                        if (item.isLocal) {
                                            IconButton(
                                                onClick = { itemToDelete = item },
                                                modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.padding(paddingValues), contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(items) { item ->
                                ListItem(
                                    headlineContent = { Text(if (item.isLocal) "Koleksi Lokal" else "Koleksi API", fontWeight = FontWeight.Bold) },
                                    supportingContent = {
                                        Column {
                                            Text("ID: ${item.id.take(8)}...")
                                            Text("Dimensi: ${item.width}x${item.height}")
                                        }
                                    },
                                    leadingContent = {
                                        AsyncImage(model = item.url, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape))
                                    },
                                    trailingContent = {
                                        if (item.isLocal) {
                                            IconButton(onClick = { itemToDelete = item }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Login
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text("Login Diperlukan") },
            text = { Text("Silakan login untuk mengakses koleksi pribadi Anda.") },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        signIn(context).onSuccess { userData ->
                            UserDataStore.saveUserSession(context, userData["email"] ?: "", userData["name"] ?: "", userData["photoUrl"])
                            showLoginDialog = false
                            hasFetchedData = false
                        }.onFailure { /* Handle error */ }
                    }
                }) { Text("Login") }
            },
            dismissButton = { TextButton(onClick = { showLoginDialog = false }) { Text("Batal") } }
        )
    }

    // Dialog Profil & Logout
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            icon = { AsyncImage(model = userPhoto, contentDescription = null, modifier = Modifier.size(64.dp).clip(CircleShape)) },
            title = { Text(userName ?: "User", fontWeight = FontWeight.Bold) },
            text = { Text(userEmail ?: "") },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        UserDataStore.clearUserSession(context)
                        showProfileDialog = false
                        hasFetchedData = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Logout")
                }
            },
            dismissButton = { TextButton(onClick = { showProfileDialog = false }) { Text("Tutup") } }
        )
    }

    // Dialog Konfirmasi Hapus (Wajib Rubrik)
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Hapus Koleksi?") },
            text = { Text("Apakah Anda yakin ingin menghapus koleksi ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItem(item.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Batal") }
            }
        )
    }
}