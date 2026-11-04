package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val score: Int,
    val maxLength: Int,
    val kills: Int,
    val survivalSeconds: Int,
    val skinName: String
)
