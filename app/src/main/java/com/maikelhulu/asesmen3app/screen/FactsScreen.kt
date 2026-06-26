package com.maikelhulu.asesmen3app.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maikelhulu.asesmen3app.network.CatFactRetrofitInstance
import com.maikelhulu.asesmen3app.util.SavedFactStore
import kotlinx.coroutines.launch

@Composable
fun FactsScreen() {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val savedFacts by SavedFactStore.getSavedFacts(context).collectAsState(initial = emptySet())
    var fact by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun loadFact() {
        scope.launch {
            isLoading = true
            errorText = null
            try {
                fact = CatFactRetrofitInstance.api.getRandomFact().fact
            } catch (error: Exception) {
                errorText = "Fakta belum bisa dimuat. Coba lagi sebentar."
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadFact()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Fakta", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { loadFact() }, modifier = Modifier.size(52.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Muat fakta baru", modifier = Modifier.size(30.dp))
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when {
                    isLoading -> Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    errorText != null -> Text(errorText.orEmpty(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    else -> Text(fact, style = MaterialTheme.typography.titleLarge)
                }

                Button(
                    onClick = {
                        if (fact.isNotBlank()) {
                            scope.launch { SavedFactStore.saveFact(context, fact) }
                        }
                    },
                    enabled = fact.isNotBlank() && !savedFacts.contains(fact),
                    modifier = Modifier.fillMaxWidth().height(54.dp)
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Text(if (savedFacts.contains(fact)) "Sudah disimpan" else "Simpan fakta")
                }
            }
        }

        Text("Tersimpan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        if (savedFacts.isEmpty()) {
            Text("Simpan fakta favoritmu di sini.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(savedFacts.toList()) { savedFact ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(savedFact, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    scope.launch { SavedFactStore.removeFact(context, savedFact) }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus")
                            }
                        }
                    }
                }
            }
        }
    }
}
