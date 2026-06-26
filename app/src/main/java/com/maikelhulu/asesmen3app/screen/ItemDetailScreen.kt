package com.maikelhulu.asesmen3app.screen

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.maikelhulu.asesmen3app.model.Item
import com.maikelhulu.asesmen3app.util.UserDataStore
import com.maikelhulu.asesmen3app.util.catNameFor
import com.maikelhulu.asesmen3app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(itemId: String, onNavigateBack: () -> Unit) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val item by viewModel.selectedItem.collectAsState()
    var userEmail by remember { mutableStateOf("") }
    var savedToCollection by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        viewModel.observeItem(itemId)
    }

    LaunchedEffect(Unit) {
        UserDataStore.getUserSession(context).collect { session ->
            userEmail = session["email"].orEmpty()
        }
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
                        if (currentItem.isLocal) "Detail Koleksi" else "Detail Foto",
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
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${currentItem.cleanTitle()}\n${currentItem.url}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan foto"))
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
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
                    val title = currentItem.cleanTitle()
                    val description = currentItem.cleanDescription()
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    if (description.isNotBlank()) {
                        Text(description, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (currentItem.isLocal) "Tersimpan di koleksi" else "Belum tersimpan di koleksi",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Ukuran gambar: ${currentItem.width} x ${currentItem.height}px",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            if (!currentItem.isLocal) {
                Button(
                    onClick = {
                        if (userEmail.isNotBlank()) {
                            viewModel.saveRemoteToCollection(userEmail, currentItem)
                            savedToCollection = true
                        }
                    },
                    enabled = userEmail.isNotBlank() && !savedToCollection,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(58.dp)
                ) {
                    Text(
                        if (savedToCollection) "Sudah masuk Koleksi" else "Simpan ke Koleksi",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

private fun Item.cleanTitle(): String {
    return if (!isLocal && (title.startsWith("Explore ", ignoreCase = true) || title.equals("Foto pilihan", ignoreCase = true))) {
        catNameFor(id)
    } else {
        title.ifBlank { catNameFor(id) }
    }
}

private fun Item.cleanDescription(): String {
    return if (!isLocal && description.contains("The Cat API", ignoreCase = true)) {
        ""
    } else {
        description
    }
}
