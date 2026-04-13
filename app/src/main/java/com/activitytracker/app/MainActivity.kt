package com.activitytracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.activitytracker.app.ui.navigation.Screen
import com.activitytracker.app.ui.screens.AddActivityScreen
import com.activitytracker.app.ui.screens.HomeScreen
import com.activitytracker.app.ui.screens.SettingsScreen
import com.activitytracker.app.ui.screens.StatisticsScreen
import com.activitytracker.app.ui.theme.ActivityTrackerTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ActivityTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(navController = navController, startDestination = Screen.Home.route) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onNavigateToAddActivity = {
                                    navController.navigate(Screen.AddActivity.route)
                                },
                                onNavigateToActivityDetail = { activityId ->
                                    // Navigate to detail screen (could be implemented)
                                }
                            )
                        }
                        composable(Screen.AddActivity.route) {
                            AddActivityScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(Screen.Statistics.route) {
                            StatisticsScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
