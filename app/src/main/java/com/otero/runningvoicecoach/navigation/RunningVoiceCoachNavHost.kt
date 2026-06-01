package com.otero.runningvoicecoach.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.otero.runningvoicecoach.domain.workout.ExampleWorkouts
import com.otero.runningvoicecoach.ui.activeRun.ActiveRunScreen
import com.otero.runningvoicecoach.ui.health.HealthScreen
import com.otero.runningvoicecoach.ui.home.HomeScreen
import com.otero.runningvoicecoach.ui.summary.ActivityCaloriesSummaryScreen
import com.otero.runningvoicecoach.ui.summary.ActivityDistanceSummaryScreen
import com.otero.runningvoicecoach.ui.summary.ActivityMapScreen
import com.otero.runningvoicecoach.ui.summary.ActivityPaceSummaryScreen
import com.otero.runningvoicecoach.ui.summary.ActivityResultScreen
import com.otero.runningvoicecoach.ui.settings.SettingsScreen
import com.otero.runningvoicecoach.ui.summary.RunSummaryScreen
import com.otero.runningvoicecoach.ui.workouts.WorkoutEditorScreen
import com.otero.runningvoicecoach.ui.workouts.WorkoutListScreen

@Composable
fun RunningVoiceCoachNavHost() {
    val navController = rememberNavController()
    val goHome = {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Home.route) { inclusive = true }
        }
    }
    val goRoutines = {
        navController.navigate(Screen.WorkoutList.route) {
            launchSingleTop = true
        }
    }
    val goProgress = {
        navController.navigate(Screen.RunSummary.route) {
            launchSingleTop = true
        }
    }
    val goRecentActivity = {
        navController.navigate(Screen.ActivityResult.route) {
            launchSingleTop = true
        }
    }
    val goActivityMap = {
        navController.navigate(Screen.ActivityMap.route) {
            launchSingleTop = true
        }
    }
    val goActivityDistances = {
        navController.navigate(Screen.ActivityDistances.route) {
            launchSingleTop = true
        }
    }
    val goActivityPaces = {
        navController.navigate(Screen.ActivityPaces.route) {
            launchSingleTop = true
        }
    }
    val goActivityCalories = {
        navController.navigate(Screen.ActivityCalories.route) {
            launchSingleTop = true
        }
    }
    val goHealth = {
        navController.navigate(Screen.Health.route) {
            launchSingleTop = true
        }
    }
    val goProfile = {
        navController.navigate(Screen.Settings.route) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onWorkouts = goRoutines,
                onHistory = goProgress,
                onRecentActivity = goRecentActivity,
                onDistanceSummary = goActivityDistances,
                onPaceSummary = goActivityPaces,
                onCaloriesSummary = goActivityCalories,
                onHealth = goHealth,
                onSettings = goProfile
            )
        }
        composable(Screen.WorkoutList.route) {
            WorkoutListScreen(
                onBack = { navController.popBackStack() },
                onCreateWorkout = { navController.navigate(Screen.WorkoutEditor.route) },
                onSelectWorkout = { workoutPlanId ->
                    navController.navigate(Screen.ActiveRun.createRoute(workoutPlanId))
                },
                onStartFreeRun = {
                    navController.navigate(Screen.ActiveRun.createRoute(ExampleWorkouts.freeRun.id))
                },
                onHome = goHome,
                onProgress = goProgress,
                onHealth = goHealth,
                onProfile = goProfile
            )
        }
        composable(Screen.WorkoutEditor.route) {
            WorkoutEditorScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.ActiveRun.route,
            arguments = listOf(navArgument(Screen.ActiveRun.ARG_WORKOUT_PLAN_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            ActiveRunScreen(
                workoutPlanId = backStackEntry.arguments?.getString(Screen.ActiveRun.ARG_WORKOUT_PLAN_ID),
                onBack = { navController.popBackStack() },
                onFinish = {
                    navController.popBackStack()
                    navController.navigate(Screen.ActivityResult.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.ActivityResult.route) {
            ActivityResultScreen(
                onHome = goHome,
                onRoutines = goRoutines,
                onProgress = goProgress,
                onOpenMap = goActivityMap,
                onHealth = goHealth,
                onProfile = goProfile
            )
        }
        composable(Screen.ActivityMap.route) {
            ActivityMapScreen(
                onBack = { navController.popBackStack() },
                onHome = goHome,
                onRoutines = goRoutines,
                onProgress = goProgress,
                onHealth = goHealth,
                onProfile = goProfile
            )
        }
        composable(Screen.ActivityDistances.route) {
            ActivityDistanceSummaryScreen(
                onHome = goHome,
                onRoutines = goRoutines,
                onProgress = goProgress,
                onHealth = goHealth,
                onProfile = goProfile
            )
        }
        composable(Screen.ActivityPaces.route) {
            ActivityPaceSummaryScreen(
                onHome = goHome,
                onRoutines = goRoutines,
                onProgress = goProgress,
                onHealth = goHealth,
                onProfile = goProfile
            )
        }
        composable(Screen.ActivityCalories.route) {
            ActivityCaloriesSummaryScreen(
                onHome = goHome,
                onRoutines = goRoutines,
                onProgress = goProgress,
                onHealth = goHealth,
                onProfile = goProfile
            )
        }
        composable(Screen.RunSummary.route) {
            RunSummaryScreen(
                onHome = goHome,
                onRoutines = goRoutines,
                onProgress = {},
                onHealth = goHealth,
                onProfile = goProfile
            )
        }
        composable(Screen.Health.route) {
            HealthScreen(
                onHome = goHome,
                onRoutines = goRoutines,
                onProgress = goProgress,
                onHealth = {},
                onProfile = goProfile
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onHome = goHome,
                onRoutines = goRoutines,
                onProgress = goProgress,
                onHealth = goHealth,
                onProfile = {}
            )
        }
    }
}
