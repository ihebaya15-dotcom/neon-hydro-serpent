package com.example.data

import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val dao: GameRecordDao) {
    val topScores: Flow<List<GameRecord>> = dao.getTopScores()
    val highestScore: Flow<Int?> = dao.getHighestScore()
    val longestLength: Flow<Int?> = dao.getLongestLength()
    val totalKills: Flow<Int?> = dao.getTotalKills()
    val totalGames: Flow<Int> = dao.getTotalGames()

    suspend fun saveRecord(record: GameRecord): Long {
        return dao.insert(record)
    }
}
