package com.maikelhulu.asesmen3app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "items")
data class Item(
    @PrimaryKey
    val id: String,
    val url: String,
    val width: Int,
    val height: Int,
    val isLocal: Boolean = false,
    val userId: String = "" // TAMBAHKAN INI
)

data class ApiItem(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int
)

data class User(
    val email: String,
    val name: String,
    val photoUrl: String?
)

enum class ApiStatus {
    LOADING, SUCCESS, FAILED
}