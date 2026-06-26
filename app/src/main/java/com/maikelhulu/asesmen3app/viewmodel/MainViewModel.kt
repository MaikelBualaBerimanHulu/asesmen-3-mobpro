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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val itemDao = db.itemDao()

    private val _apiStatus = MutableStateFlow(ApiStatus.LOADING)
    val apiStatus: StateFlow<ApiStatus> = _apiStatus.asStateFlow()

    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    fun fetchAndSyncData(userId: String) {
        viewModelScope.launch {
            _apiStatus.value = ApiStatus.LOADING
            try {
                val remoteItems = RetrofitInstance.api.getItems(limit = 20)

                val localItems = remoteItems.map { apiItem ->
                    Item(
                        id = apiItem.id,
                        url = apiItem.url,
                        width = apiItem.width,
                        height = apiItem.height,
                        isLocal = false,
                        userId = userId
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

    fun observeLocalItems(userId: String) {
        viewModelScope.launch {
            itemDao.getItemsByUser(userId).collect { list ->
                _items.value = list
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

    fun saveItemLocally(userId: String, title: String, description: String, imageUri: Uri?) {
        viewModelScope.launch {
            val newItem = Item(
                id = UUID.randomUUID().toString(),
                url = imageUri?.toString() ?: "",
                width = 0,
                height = 0,
                isLocal = true,
                userId = userId
            )
            itemDao.insertItem(newItem)
        }
    }
}