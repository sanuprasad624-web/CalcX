package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.NotebookEntity
import kotlinx.coroutines.flow.Flow

class CalcRepository(database: AppDatabase) {
    private val historyDao = database.historyDao()
    private val notebookDao = database.notebookDao()

    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    val favoriteHistory: Flow<List<HistoryEntity>> = historyDao.getFavoriteHistory()
    val allNotes: Flow<List<NotebookEntity>> = notebookDao.getAllNotes()

    fun searchHistory(query: String): Flow<List<HistoryEntity>> = historyDao.searchHistory(query)
    fun searchNotes(query: String): Flow<List<NotebookEntity>> = notebookDao.searchNotes(query)

    suspend fun addHistory(expression: String, result: String, category: String, angleMode: String) {
        historyDao.insertHistory(
            HistoryEntity(
                expression = expression,
                result = result,
                category = category,
                angleMode = angleMode,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleFavoriteHistory(item: HistoryEntity) {
        historyDao.updateHistory(item.copy(isFavorite = !item.isFavorite))
    }

    suspend fun deleteHistory(item: HistoryEntity) {
        historyDao.deleteHistory(item)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    suspend fun addNote(title: String, calculationText: String, result: String, note: String, category: String) {
        notebookDao.insertNote(
            NotebookEntity(
                title = title,
                calculationText = calculationText,
                result = result,
                note = note,
                category = category,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateNote(item: NotebookEntity) {
        notebookDao.updateNote(item)
    }

    suspend fun deleteNote(item: NotebookEntity) {
        notebookDao.deleteNote(item)
    }
}
