package com.maikelhulu.asesmen3app.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.maikelhulu.asesmen3app.model.Item
import com.maikelhulu.asesmen3app.util.UserDataStore
import com.maikelhulu.asesmen3app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(navController: NavHostController) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    var userEmail by remember { mutableStateOf<String?>(null) }
    var itemToDelete by remember { mutableStateOf<Item?>(null) }
    val items by viewModel.items.collectAsState()

    LaunchedEffect(Unit) {
        UserDataStore.getUserSession(context).collect { s ->
            userEmail = s["email"]
            if (userEmail != null) viewModel.observeLocalItems(userEmail!!)
        }
    }

    val localItems = items.filter { it.isLocal }

    if (localItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // PERBAIKAN: Gunakan named parameter horizontalAlignment
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Koleksimu kosong.", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Tambahkan koleksi pertamamu!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(localItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    shape = RoundedCornerShape(20.dp),
                    onClick = { navController.navigate(com.maikelhulu.asesmen3app.navigation.Screen.ItemDetail.createRoute(item.id)) }
                ) {
                    Box(Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = item.url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Badge(modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
                            Text("Lokal", style = MaterialTheme.typography.labelLarge)
                        }
                        IconButton(
                            onClick = { itemToDelete = item },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog Hapus
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Hapus Koleksi?") },
            text = { Text("Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteItem(item.id); itemToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.height(52.dp)
                ) { Text("Hapus", style = MaterialTheme.typography.titleMedium) }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }, modifier = Modifier.height(52.dp)) {
                    Text("Batal", style = MaterialTheme.typography.titleMedium)
                }
            }
        )
    }
}
