package com.maikelhulu.asesmen3app.screen

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// WEB_CLIENT_ID HARDCODE SEMENTARA UNTUK UJIAN BESOK
private const val WEB_CLIENT_ID = "665052332737-btd6rne3hkqk2siip69q8pu9r21rah80.apps.googleusercontent.com"

suspend fun signIn(context: Context): Result<Map<String, String>> {
    return withContext(Dispatchers.IO) {
        try {
            Log.d("LOGIN_DEBUG", "Starting sign in with client ID: $WEB_CLIENT_ID")

            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                    val userData = mapOf(
                        "email" to googleIdTokenCredential.id,
                        "name" to (googleIdTokenCredential.displayName ?: ""),
                        "photoUrl" to (googleIdTokenCredential.profilePictureUri?.toString() ?: "")
                    )

                    Log.d("LOGIN_DEBUG", "Login sukses untuk: ${userData["email"]}")
                    Result.success(userData)
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e("LOGIN_DEBUG", "Gagal parse token Google", e)
                    Result.failure(e)
                }
            } else {
                Log.e("LOGIN_DEBUG", "Credential bukan Google ID Token")
                Result.failure(Exception("Credential bukan Google ID Token"))
            }
        } catch (e: GetCredentialException) {
            Log.e("LOGIN_DEBUG", "GetCredentialException: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("LOGIN_DEBUG", "Exception umum: ${e.message}", e)
            Result.failure(e)
        }
    }
}