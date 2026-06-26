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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun deleteItem(itemId: String) // Ubah tipe jadi String sesuai PrimaryKey baru
}