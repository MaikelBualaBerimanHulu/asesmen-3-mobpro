package com.maikelhulu.asesmen3app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

object SettingsDataStore {
    private val KEY_LAYOUT_GRID = booleanPreferencesKey("layout_grid")

    fun getLayoutPreference(context: Context): Flow<Boolean> {
        return context.settingsDataStore.data.map { preferences ->
            preferences[KEY_LAYOUT_GRID] ?: false
        }
    }

    suspend fun saveLayoutPreference(context: Context, isGrid: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_LAYOUT_GRID] = isGrid
        }
    }
}

object UserDataStore {
    private val KEY_EMAIL = stringPreferencesKey("email")
    private val KEY_NAME = stringPreferencesKey("name")
    private val KEY_PHOTO = stringPreferencesKey("photo_url")

    fun getUserSession(context: Context): Flow<Map<String, String?>> {
        return context.userDataStore.data.map { preferences ->
            mapOf(
                "email" to preferences[KEY_EMAIL],
                "name" to preferences[KEY_NAME],
                "photo_url" to preferences[KEY_PHOTO]
            )
        }
    }

    suspend fun saveUserSession(context: Context, email: String, name: String, photoUrl: String?) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_EMAIL] = email
            preferences[KEY_NAME] = name
            preferences[KEY_PHOTO] = photoUrl ?: ""
        }
    }

    suspend fun clearUserSession(context: Context) {
        context.userDataStore.edit { it.clear() }
    }
}
