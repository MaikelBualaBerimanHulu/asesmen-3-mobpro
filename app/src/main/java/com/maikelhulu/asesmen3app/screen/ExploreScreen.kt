package com.maikelhulu.asesmen3app.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    LaunchedEffect(Unit) {
        UserDataStore.getUserSession(context).collect { s -> userEmail = s["email"] }
    }

    LaunchedEffect(userEmail) {
        if (userEmail != null && !hasFetched) {
            viewModel.fetchAndSyncData(userEmail!!)
            hasFetched = true
        }
    }

    when (apiStatus) {
        ApiStatus.LOADING -> LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(6) { SkeletonCard() }
        }
        ApiStatus.FAILED -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // PERBAIKAN: Gunakan named parameter horizontalAlignment
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
            val remoteItems = items.filter { !it.isLocal }
            if (remoteItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada data dari API.", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(remoteItems) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            shape = RoundedCornerShape(20.dp),
                            onClick = { navController.navigate(com.maikelhulu.asesmen3app.navigation.Screen.ItemDetail.createRoute(item.id)) }
                        ) {
                            AsyncImage(
                                model = item.url,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}
