package com.maikelhulu.asesmen3app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.maikelhulu.asesmen3app.model.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE userId = :userId ORDER BY id DESC")
    fun getItemsByUser(userId: String): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :itemId LIMIT 1")
    suspend fun getItemById(itemId: String): Item?

    @Query("SELECT * FROM items WHERE id = :itemId LIMIT 1")
    fun observeItemById(itemId: String): Flow<Item?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    @Query("UPDATE items SET title = :title, description = :description, updatedAt = :updatedAt WHERE id = :itemId")
    suspend fun updateItemText(itemId: String, title: String, description: String, updatedAt: Long)

    @Query("UPDATE items SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :itemId")
    suspend fun updateFavorite(itemId: String, isFavorite: Boolean, updatedAt: Long)

    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun deleteItem(itemId: String) // Ubah tipe jadi String sesuai PrimaryKey baru
}
