package com.example.memogame


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.memogame.ui.screens.GameScreen
import com.example.memogame.ui.screens.LevelSelectionScreen
import com.example.memogame.ui.screens.MainScreen
import com.example.memogame.ui.theme.MemoGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemoGameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") {
                            MainScreen(
                                onStartClick = { navController.navigate("level_selection") },
                                application = application as MemoGameApplication
                            )
                        }
                        composable("level_selection") {
                            LevelSelectionScreen(
                                onLevelSelected = { levelId ->
                                    navController.navigate("game/$levelId")
                                },
                                onBack = { navController.popBackStack() },
                                application = application as MemoGameApplication
                            )
                        }
                        composable(
                            route = "game/{levelId}",
                            arguments = listOf(
                                navArgument("levelId") {
                                    type = NavType.IntType
                                }
                            )
                        ) { backStackEntry ->
                            val levelId = backStackEntry.arguments?.getInt("levelId") ?: 1
                            GameScreen(
                                levelId = levelId,
                                onBack = { navController.popBackStack() },
                                application = application as MemoGameApplication
                            )
                        }
                    }
                }
            }
        }
    }
}