package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.NotebookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM calculation_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM calculation_history WHERE expression LIKE '%' || :query || '%' OR result LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHistory(query: String): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryEntity): Long

    @Update
    suspend fun updateHistory(item: HistoryEntity)

    @Delete
    suspend fun deleteHistory(item: HistoryEntity)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAllHistory()
}

@Dao
interface NotebookDao {
    @Query("SELECT * FROM calculation_notebook ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM calculation_notebook WHERE title LIKE '%' || :query || '%' OR calculationText LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchNotes(query: String): Flow<List<NotebookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(item: NotebookEntity): Long

    @Update
    suspend fun updateNote(item: NotebookEntity)

    @Delete
    suspend fun deleteNote(item: NotebookEntity)
}
