package com.maikelhulu.asesmen3app.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.maikelhulu.asesmen3app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(itemId: String, onNavigateBack: () -> Unit) {
    val viewModel: MainViewModel = viewModel()
    val items by viewModel.items.collectAsState()
    val item = items.find { it.id == itemId }
    var isFavorite by remember { mutableStateOf(false) }

    if (item == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Item tidak ditemukan.") }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (item.isLocal) "Koleksi Lokal" else "Explore Item") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { /* Share logic */ }) { Icon(Icons.Default.Share, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(model = item.url, contentDescription = null, modifier = Modifier.fillMaxWidth().aspectRatio(1f))

            Spacer(Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Metadata", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("ID: ${item.id.take(12)}...")
                    Text("Dimensi: ${item.width} x ${item.height}px")
                    Text("Sumber: ${if (item.isLocal) "Upload Lokal" else "The Cat API"}")
                    Text("Status: ${if (item.isLocal) "Tersimpan Offline" else "Online Only"}")
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { /* Download simulasi */ },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) { Text("Simpan ke Galeri") }

            Spacer(Modifier.height(16.dp))
        }
    }
}