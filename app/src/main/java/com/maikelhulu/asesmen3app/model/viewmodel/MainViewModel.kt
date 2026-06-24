package com.maikelhulu.asesmen3app.viewmodel

import android.app.Application
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
                        isLocal = false
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
}