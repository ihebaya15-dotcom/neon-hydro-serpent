package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameRecord
import com.example.data.ScoreRepository
import com.example.game.GameEngine
import com.example.game.LeaderboardEntry
import com.example.game.Serpent
import com.example.game.SkinTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GameState {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER
}

data class MatchStats(
    val score: Int = 0,
    val length: Int = 0,
    val kills: Int = 0,
    val survivalSeconds: Int = 0,
    val isNewHighScore: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScoreRepository
    private val vibrator: Vibrator?

    private val _gameState = MutableStateFlow(GameState.MENU)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _selectedSkin = MutableStateFlow(SkinTheme.CYAN_STORM)
    val selectedSkin: StateFlow<SkinTheme> = _selectedSkin.asStateFlow()

    private val _lastMatchStats = MutableStateFlow(MatchStats())
    val lastMatchStats: StateFlow<MatchStats> = _lastMatchStats.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    val topScores: StateFlow<List<GameRecord>>
    val highestScore: StateFlow<Int?>

    val gameEngine: GameEngine

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ScoreRepository(db.gameRecordDao())
        topScores = repository.topScores.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        highestScore = repository.highestScore.stateIn(viewModelScope, SharingStarted.Lazily, 0)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        gameEngine = GameEngine(
            onPlayerKilled = { victim ->
                handleGameOver(victim)
            },
            onPlayerKillStreak = {
                triggerHaptic(HapticType.KILL)
            },
            onEatOrb = {
                // Subtle tactile click on eating
            }
        )
    }

    fun selectSkin(skin: SkinTheme) {
        _selectedSkin.value = skin
    }

    fun startGame() {
        gameEngine.resetGame(_selectedSkin.value)
        _gameState.value = GameState.PLAYING
    }

    fun restartGame() {
        gameEngine.resetGame(_selectedSkin.value)
        _gameState.value = GameState.PLAYING
    }

    fun pauseGame() {
        if (_gameState.value == GameState.PLAYING) {
            _gameState.value = GameState.PAUSED
        }
    }

    fun resumeGame() {
        if (_gameState.value == GameState.PAUSED) {
            _gameState.value = GameState.PLAYING
        }
    }

    fun returnToMenu() {
        _gameState.value = GameState.MENU
    }

    fun onJoystickMoved(x: Float, y: Float) {
        if (_gameState.value == GameState.PLAYING) {
            gameEngine.setPlayerDirection(x, y)
        }
    }

    fun onBoostChanged(boosting: Boolean) {
        if (_gameState.value == GameState.PLAYING) {
            gameEngine.setPlayerBoosting(boosting)
            if (boosting) {
                triggerHaptic(HapticType.BOOST_START)
            }
        }
    }

    fun updateGameLoop(deltaTime: Float) {
        if (_gameState.value == GameState.PLAYING) {
            gameEngine.update(deltaTime)
            _leaderboard.value = gameEngine.getLeaderboard()
        }
    }

    private fun handleGameOver(player: Serpent) {
        triggerHaptic(HapticType.DEATH)
        val score = player.score
        val length = player.segments.size
        val kills = player.kills
        val survivalTime = gameEngine.elapsedTime.toInt()
        val prevBest = highestScore.value ?: 0
        val isBest = score > prevBest

        val match = MatchStats(
            score = score,
            length = length,
            kills = kills,
            survivalSeconds = survivalTime,
            isNewHighScore = isBest
        )
        _lastMatchStats.value = match
        _gameState.value = GameState.GAME_OVER

        // Persist match in Room
        viewModelScope.launch {
            repository.saveRecord(
                GameRecord(
                    score = score,
                    maxLength = length,
                    kills = kills,
                    survivalSeconds = survivalTime,
                    skinName = _selectedSkin.value.nameEn
                )
            )
        }
    }

    private fun triggerHaptic(type: HapticType) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    when (type) {
                        HapticType.BOOST_START -> {
                            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                        HapticType.KILL -> {
                            vibrator.vibrate(
                                VibrationEffect.createWaveform(
                                    longArrayOf(0, 40, 30, 60),
                                    intArrayOf(0, 180, 0, 255),
                                    -1
                                )
                            )
                        }
                        HapticType.DEATH -> {
                            vibrator.vibrate(
                                VibrationEffect.createWaveform(
                                    longArrayOf(0, 70, 40, 100),
                                    intArrayOf(0, 255, 0, 200),
                                    -1
                                )
                            )
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(40)
                }
            }
        } catch (_: Exception) {
            // Safe fallback if device suppresses vibration
        }
    }
}

enum class HapticType {
    BOOST_START,
    KILL,
    DEATH
}
