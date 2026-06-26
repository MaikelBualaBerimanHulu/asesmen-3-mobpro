package com.maikelhulu.asesmen3app.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.maikelhulu.asesmen3app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(itemId: String, onNavigateBack: () -> Unit) {
    val viewModel: MainViewModel = viewModel()
    val item by viewModel.selectedItem.collectAsState()

    LaunchedEffect(itemId) {
        viewModel.observeItem(itemId)
    }

    val currentItem = item
    if (currentItem == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("Item tidak ditemukan.", style = MaterialTheme.typography.headlineSmall)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (currentItem.isLocal) "Koleksi Lokal" else "Explore Item",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(currentItem) }, modifier = Modifier.size(56.dp)) {
                        Icon(if (currentItem.isFavorite) Icons.Default.Favorite else Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = if (currentItem.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { /* Share logic */ }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.Share, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = currentItem.url,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(28.dp))

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(currentItem.title.ifBlank { "Tanpa Judul" }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    Text(currentItem.description.ifBlank { "Tidak ada deskripsi" }, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Metadata", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text("ID: ${currentItem.id.take(12)}...", style = MaterialTheme.typography.titleMedium)
                    Text("Dimensi: ${currentItem.width} x ${currentItem.height}px", style = MaterialTheme.typography.titleMedium)
                    Text("Sumber: ${if (currentItem.isLocal) "Upload Lokal" else "The Cat API"}", style = MaterialTheme.typography.titleMedium)
                    Text("Status: ${if (currentItem.isLocal) "Tersimpan Offline" else "Online Only"}", style = MaterialTheme.typography.titleMedium)
                    Text("Favorit: ${if (currentItem.isFavorite) "Ya" else "Tidak"}", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = { /* Download simulasi */ },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(58.dp)
            ) { Text("Simpan ke Galeri", style = MaterialTheme.typography.titleMedium) }

            Spacer(Modifier.height(20.dp))
        }
    }
}
