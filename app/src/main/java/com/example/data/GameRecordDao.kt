package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: GameRecord): Long

    @Query("SELECT * FROM game_records ORDER BY score DESC LIMIT 10")
    fun getTopScores(): Flow<List<GameRecord>>

    @Query("SELECT MAX(score) FROM game_records")
    fun getHighestScore(): Flow<Int?>

    @Query("SELECT MAX(maxLength) FROM game_records")
    fun getLongestLength(): Flow<Int?>

    @Query("SELECT SUM(kills) FROM game_records")
    fun getTotalKills(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM game_records")
    fun getTotalGames(): Flow<Int>
}
