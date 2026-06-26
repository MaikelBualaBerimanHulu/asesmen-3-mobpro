package com.maikelhulu.asesmen3app.screen

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.maikelhulu.asesmen3app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun signIn(context: Context): Result<Map<String, String>> {
    return withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.WEB_CLIENT_ID)
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

                    Result.success(userData)
                } catch (e: GoogleIdTokenParsingException) {
                    Result.failure(e)
                }
            } else {
                Result.failure(Exception("Credential bukan Google ID Token"))
            }
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
