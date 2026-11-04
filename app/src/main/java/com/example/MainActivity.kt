package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.theme.AbyssDark
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GameState
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AbyssDark
                ) {
                    NeonSerpentApp()
                }
            }
        }
    }
}

@Composable
fun NeonSerpentApp(
    viewModel: GameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    val selectedSkin by viewModel.selectedSkin.collectAsState()
    val highestScore by viewModel.highestScore.collectAsState()
    val topScores by viewModel.topScores.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val globalLeaderboard by viewModel.globalLeaderboard.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    Crossfade(
        targetState = (gameState != GameState.MENU),
        label = "screen_transition"
    ) { inGame ->
        if (inGame) {
            GameScreen(
                viewModel = viewModel,
                onBackToMenu = {
                    viewModel.returnToMenu()
                }
            )
        } else {
            MenuScreen(
                selectedSkin = selectedSkin,
                highestScore = highestScore,
                topScores = topScores,
                currentUser = currentUser,
                globalLeaderboard = globalLeaderboard,
                authError = authError,
                isSyncing = isSyncing,
                onSelectSkin = { skin ->
                    viewModel.selectSkin(skin)
                },
                onStartGame = {
                    viewModel.startGame()
                },
                onSignInWithGoogle = { activityContext ->
                    viewModel.signInWithGoogle(activityContext)
                },
                onSignOut = {
                    viewModel.signOut()
                },
                onClearError = {
                    viewModel.clearAuthError()
                }
            )
        }
    }
}
