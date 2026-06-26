package com.maikelhulu.asesmen3app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maikelhulu.asesmen3app.database.AppDatabase
import com.maikelhulu.asesmen3app.model.ApiItem
import com.maikelhulu.asesmen3app.model.ApiStatus
import com.maikelhulu.asesmen3app.model.Item
import com.maikelhulu.asesmen3app.network.RetrofitInstance
import com.maikelhulu.asesmen3app.util.catNameFor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val itemDao = db.itemDao()

    private val _apiStatus = MutableStateFlow(ApiStatus.LOADING)
    val apiStatus: StateFlow<ApiStatus> = _apiStatus.asStateFlow()

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    private val _selectedItem = MutableStateFlow<Item?>(null)
    val selectedItem: StateFlow<Item?> = _selectedItem.asStateFlow()

    fun fetchAndSyncData(userId: String) {
        viewModelScope.launch {
            _apiStatus.value = ApiStatus.LOADING
            try {
                cleanOldRemoteCopy()
                val remoteItems = RetrofitInstance.api.getItems(limit = 20)

                val localItems = remoteItems.map { apiItem ->
                    Item(
                        id = apiItem.id,
                        url = apiItem.url,
                        width = apiItem.width,
                        height = apiItem.height,
                        isLocal = false,
                        userId = userId,
                        title = catNameFor(apiItem.id),
                        description = "",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                }

                for (item in localItems) {
                    itemDao.insertItem(item)
                }

                _apiStatus.value = ApiStatus.SUCCESS
            } catch (e: Exception) {
                _apiStatus.value = ApiStatus.FAILED
            }
        }
    }

    fun observeItems(userId: String) {
        viewModelScope.launch {
            cleanOldRemoteCopy()
            itemDao.getItemsByUser(userId).collect { list ->
                _items.value = list
            }
        }
    }

    fun observeItem(itemId: String) {
        viewModelScope.launch {
            itemDao.observeItemById(itemId).collect { item ->
                _selectedItem.value = item
            }
        }
    }

    fun observeLocalItems(userId: String) {
        viewModelScope.launch {
            itemDao.getItemsByUser(userId).map { list ->
                list.filter { it.isLocal }
            }.collect { localItems ->
                _items.value = localItems
            }
        }
    }

    fun retryFetch(userId: String) {
        fetchAndSyncData(userId)
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            itemDao.deleteItem(itemId)
        }
    }

    fun updateItem(itemId: String, title: String, description: String) {
        viewModelScope.launch {
            itemDao.updateItemText(itemId, title.trim(), description.trim(), System.currentTimeMillis())
        }
    }

    fun toggleFavorite(item: Item) {
        viewModelScope.launch {
            itemDao.updateFavorite(item.id, !item.isFavorite, System.currentTimeMillis())
        }
    }

    fun saveItemLocally(userId: String, title: String, description: String, imageUri: Uri?) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newItem = Item(
                id = UUID.randomUUID().toString(),
                url = imageUri?.toString() ?: "",
                width = 0,
                height = 0,
                isLocal = true,
                userId = userId,
                title = title.trim(),
                description = description.trim(),
                createdAt = now,
                updatedAt = now
            )
            itemDao.insertItem(newItem)
        }
    }

    fun saveRemoteToCollection(userId: String, item: Item) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val collectionItem = item.copy(
                id = UUID.randomUUID().toString(),
                isLocal = true,
                userId = userId,
                title = item.displayTitle(),
                description = item.description,
                isFavorite = false,
                createdAt = now,
                updatedAt = now
            )
            itemDao.insertItem(collectionItem)
        }
    }

    private suspend fun cleanOldRemoteCopy() {
        itemDao.cleanOldRemoteTitles()
        itemDao.cleanOldRemoteDescriptions()
    }

    private fun Item.displayTitle(): String {
        return if (!isLocal && (title.startsWith("Explore ", ignoreCase = true) || title.equals("Foto pilihan", ignoreCase = true))) {
            catNameFor(id)
        } else {
            title.ifBlank { catNameFor(id) }
        }
    }
}
