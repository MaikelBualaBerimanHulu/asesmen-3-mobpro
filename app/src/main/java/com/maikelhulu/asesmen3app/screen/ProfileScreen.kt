package com.maikelhulu.asesmen3app.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.maikelhulu.asesmen3app.util.UserDataStore
import com.maikelhulu.asesmen3app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = viewModel()
    val items by viewModel.items.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        UserDataStore.getUserSession(context).collect { session ->
            email = session["email"].orEmpty()
            name = session["name"].orEmpty().ifBlank { "Pengguna" }
            photoUrl = session["photo_url"]
            if (email.isNotBlank()) viewModel.observeItems(email)
        }
    }

    val localCount = items.count { it.isLocal }
    val remoteCount = items.count { !it.isLocal }
    val favoriteCount = items.count { it.isFavorite }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Profil", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto profil",
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(email, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Jelajah", remoteCount.toString(), Modifier.weight(1f))
            StatCard("Koleksi", localCount.toString(), Modifier.weight(1f))
            StatCard("Favorit", favoriteCount.toString(), Modifier.weight(1f))
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                scope.launch {
                    UserDataStore.clearUserSession(context)
                    onLogout()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.size(10.dp))
            Text("Logout", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
