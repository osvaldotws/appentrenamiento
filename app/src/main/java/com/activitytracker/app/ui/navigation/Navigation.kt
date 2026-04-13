package com.activitytracker.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object ActivityDetail : Screen("activity_detail/{activityId}") {
        fun createRoute(activityId: Long) = "activity_detail/$activityId"
    }
    object AddActivity : Screen("add_activity")
}
