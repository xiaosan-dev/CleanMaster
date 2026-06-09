package com.cleanmaster.trash

import androidx.room.*
import com.cleanmaster.core.model.TrashItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TrashDao {
    @Query("SELECT * FROM trash_items ORDER BY deletedAt DESC")
    fun getAllItems(): Flow<List<TrashItem>>

    @Query("SELECT * FROM trash_items WHERE id = :id")
    suspend fun getById(id: Long): TrashItem?

    @Query("SELECT SUM(size) FROM trash_items")
    suspend fun getTotalSize(): Long?

    @Query("SELECT COUNT(*) FROM trash_items")
    suspend fun getItemCount(): Int

    @Insert
    suspend fun insert(item: TrashItem)

    @Insert
    suspend fun insertAll(items: List<TrashItem>)

    @Delete
    suspend fun delete(item: TrashItem)

    @Query("DELETE FROM trash_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM trash_items")
    suspend fun deleteAll()

    @Query("SELECT * FROM trash_items ORDER BY deletedAt ASC LIMIT 1")
    suspend fun getOldestItem(): TrashItem?
}
