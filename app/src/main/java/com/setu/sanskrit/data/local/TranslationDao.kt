package com.setu.sanskrit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translations ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TranslationEntity>>

    @Insert
    suspend fun insert(entity: TranslationEntity): Long

    @Query("DELETE FROM translations WHERE id = :id")
    suspend fun delete(id: Long)
}
