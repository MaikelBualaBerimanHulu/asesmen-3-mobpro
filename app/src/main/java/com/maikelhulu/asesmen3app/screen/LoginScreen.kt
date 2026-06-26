package com.maikelhulu.asesmen3app.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maikelhulu.asesmen3app.util.UserDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val session = UserDataStore.getUserSession(context).first()
        if (!session["email"].isNullOrBlank()) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MyCollection", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(
            "Masuk untuk melihat explore, koleksi offline, dan profil pengguna.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isLoading.value = true
                            errorMessage.value = null
                            val result = signIn(context)
                            result.onSuccess { user ->
                                UserDataStore.saveUserSession(
                                    context,
                                    email = user["email"].orEmpty(),
                                    name = user["name"].orEmpty().ifBlank { "Pengguna Google" },
                                    photoUrl = user["photoUrl"]
                                )
                                if (user["email"].orEmpty().isNotBlank()) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage.value = "Email Google tidak terbaca. Coba mode demo atau akun lain."
                                }
                            }.onFailure { error ->
                                errorMessage.value = error.message ?: "Login Google gagal."
                            }
                            isLoading.value = false
                        }
                    },
                    enabled = !isLoading.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                ) {
                    if (isLoading.value) {
                        CircularProgressIndicator()
                    } else {
                        androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Masuk dengan Google", style = MaterialTheme.typography.titleMedium)
                    }
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            UserDataStore.saveUserSession(
                                context,
                                email = "demo@asesmen.local",
                                name = "Demo User",
                                photoUrl = null
                            )
                            onLoginSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Text("Masuk Mode Demo", style = MaterialTheme.typography.titleMedium)
                }

                errorMessage.value?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
