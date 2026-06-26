package com.maikelhulu.asesmen3app.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.maikelhulu.asesmen3app.model.Item
import com.maikelhulu.asesmen3app.navigation.Screen
import com.maikelhulu.asesmen3app.util.SettingsDataStore
import com.maikelhulu.asesmen3app.util.UserDataStore
import com.maikelhulu.asesmen3app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private enum class CollectionSort(val label: String) {
    Newest("Terbaru"),
    Title("Judul A-Z"),
    Favorite("Favorit")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(navController: NavHostController) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var userEmail by remember { mutableStateOf<String?>(null) }
    var itemToDelete by remember { mutableStateOf<Item?>(null) }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    var query by remember { mutableStateOf("") }
    var favoritesOnly by remember { mutableStateOf(false) }
    var sort by remember { mutableStateOf(CollectionSort.Newest) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    val isGrid by SettingsDataStore.getLayoutPreference(context).collectAsState(initial = false)
    val items by viewModel.items.collectAsState()

    LaunchedEffect(Unit) {
        UserDataStore.getUserSession(context).collect { session ->
            userEmail = session["email"]
            if (userEmail != null) viewModel.observeLocalItems(userEmail!!)
        }
    }

    val localItems = items
        .filter { it.isLocal }
        .filter { item ->
            val matchesQuery = query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true)
            val matchesFavorite = !favoritesOnly || item.isFavorite
            matchesQuery && matchesFavorite
        }
        .let { list ->
            when (sort) {
                CollectionSort.Newest -> list.sortedByDescending { it.createdAt }
                CollectionSort.Title -> list.sortedBy { it.title.lowercase() }
                CollectionSort.Favorite -> list.sortedWith(compareByDescending<Item> { it.isFavorite }.thenByDescending { it.createdAt })
            }
        }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Koleksi", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        scope.launch { SettingsDataStore.saveLayoutPreference(context, !isGrid) }
                    },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        if (isGrid) Icons.Default.ViewAgenda else Icons.Default.ViewModule,
                        contentDescription = "Ubah layout",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Box {
                    IconButton(onClick = { sortMenuOpen = true }, modifier = Modifier.size(52.dp)) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Urutkan", modifier = Modifier.size(30.dp))
                    }
                    DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                        CollectionSort.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    sort = option
                                    sortMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("Cari judul atau deskripsi") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = favoritesOnly,
                    onClick = { favoritesOnly = !favoritesOnly },
                    label = { Text("Favorit") },
                    leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) }
                )
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text("${localItems.size} item") }
                )
            }
        }

        if (localItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                columns = GridCells.Fixed(if (isGrid) 2 else 1),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(localItems) { item ->
                    CollectionItemCard(
                        item = item,
                        isGrid = isGrid,
                        onOpen = { navController.navigate(Screen.ItemDetail.createRoute(item.id)) },
                        onFavorite = { viewModel.toggleFavorite(item) },
                        onEdit = { itemToEdit = item },
                        onDelete = { itemToDelete = item }
                    )
                }
            }
        }
    }

    itemToEdit?.let { item ->
        EditItemDialog(
            item = item,
            onDismiss = { itemToEdit = null },
            onSave = { title, description ->
                viewModel.updateItem(item.id, title, description)
                itemToEdit = null
            }
        )
    }

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

@Composable
private fun CollectionItemCard(
    item: Item,
    isGrid: Boolean,
    onOpen: () -> Unit,
    onFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isGrid) 260.dp else 350.dp),
        shape = RoundedCornerShape(20.dp),
        onClick = onOpen
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
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onFavorite, modifier = Modifier.size(46.dp)) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favorit",
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.error else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(46.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(46.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
                }
            }
            Surface(color = Color.Black.copy(alpha = 0.52f), modifier = Modifier.align(Alignment.BottomStart)) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(
                        item.title.ifBlank { "Tanpa Judul" },
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        item.description.ifBlank { "Tidak ada deskripsi" },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (isGrid) 1 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EditItemDialog(
    item: Item,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember(item.id) { mutableStateOf(item.title) }
    var description by remember(item.id) { mutableStateOf(item.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Koleksi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, description) },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
