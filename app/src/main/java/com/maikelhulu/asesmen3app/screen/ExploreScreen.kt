package com.maikelhulu.asesmen3app.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.maikelhulu.asesmen3app.model.ApiStatus
import com.maikelhulu.asesmen3app.util.UserDataStore
import com.maikelhulu.asesmen3app.viewmodel.MainViewModel

@Composable
fun SkeletonCard() {
    val transition = rememberInfiniteTransition(label = "skel")
    val alpha by transition.animateFloat(0.3f, 0.7f, infiniteRepeatable(tween(800)), label = "shim")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color.Gray.copy(alpha), Color.LightGray.copy(alpha)))))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(navController: NavHostController) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    var hasFetched by remember { mutableStateOf(false) }

    val apiStatus by viewModel.apiStatus.collectAsState()
    val items by viewModel.items.collectAsState()
    var userEmail by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var favoritesOnly by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        UserDataStore.getUserSession(context).collect { s -> userEmail = s["email"] }
    }

    LaunchedEffect(userEmail) {
        if (userEmail != null && !hasFetched) {
            viewModel.observeItems(userEmail!!)
            viewModel.fetchAndSyncData(userEmail!!)
            hasFetched = true
        }
    }

    val remoteItems = items
        .filter { !it.isLocal }
        .filter { item ->
            val matchesQuery = query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true) ||
                item.id.contains(query, ignoreCase = true)
            val matchesFavorite = !favoritesOnly || item.isFavorite
            matchesQuery && matchesFavorite
        }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Explore", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        hasFetched = false
                        viewModel.retryFetch(userEmail ?: "")
                    },
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh API", modifier = Modifier.size(30.dp))
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("Cari foto dari API") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            FilterChip(
                selected = favoritesOnly,
                onClick = { favoritesOnly = !favoritesOnly },
                label = { Text("Favorit saja") },
                leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null) }
            )
        }

        when (apiStatus) {
            ApiStatus.LOADING -> LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(6) { SkeletonCard() }
            }
            ApiStatus.FAILED -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Gagal memuat data.", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { hasFetched = false; viewModel.retryFetch(userEmail ?: "") },
                        modifier = Modifier.height(54.dp)
                    ) { Text("Retry", style = MaterialTheme.typography.titleMedium) }
                }
            }
            ApiStatus.SUCCESS -> {
                if (remoteItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak ada data yang cocok.", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        contentPadding = PaddingValues(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(remoteItems) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(360.dp),
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
                                    IconButton(
                                        onClick = { viewModel.toggleFavorite(item) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(10.dp)
                                            .size(50.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Favorite,
                                            contentDescription = "Favorit",
                                            tint = if (item.isFavorite) MaterialTheme.colorScheme.error else Color.White,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.48f),
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    ) {
                                        Column(Modifier.padding(14.dp)) {
                                            Text(
                                                item.title,
                                                color = Color.White,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                item.description,
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
