package com.otero.runningvoicecoach.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.otero.runningvoicecoach.ui.activeRun.ActiveRunScreen
import com.otero.runningvoicecoach.ui.home.HomeScreen
import com.otero.runningvoicecoach.ui.settings.SettingsScreen
import com.otero.runningvoicecoach.ui.summary.RunSummaryScreen
import com.otero.runningvoicecoach.ui.workouts.WorkoutEditorScreen
import com.otero.runningvoicecoach.ui.workouts.WorkoutListScreen

@Composable
fun RunningVoiceCoachNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNewRun = { navController.navigate(Screen.ActiveRun.createRoute("simulacion-ritmo-7min")) },
                onWorkouts = { navController.navigate(Screen.WorkoutList.route) },
                onHistory = { navController.navigate(Screen.RunSummary.route) },
                onSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.WorkoutList.route) {
            WorkoutListScreen(
                onBack = { navController.popBackStack() },
                onCreateWorkout = { navController.navigate(Screen.WorkoutEditor.route) },
                onSelectWorkout = { workoutPlanId ->
                    navController.navigate(Screen.ActiveRun.createRoute(workoutPlanId))
                }
            )
        }
        composable(Screen.WorkoutEditor.route) {
            WorkoutEditorScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.ActiveRun.route,
            arguments = listOf(navArgument(Screen.ActiveRun.ARG_WORKOUT_PLAN_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            ActiveRunScreen(
                workoutPlanId = backStackEntry.arguments?.getString(Screen.ActiveRun.ARG_WORKOUT_PLAN_ID),
                onBack = { navController.popBackStack() },
                onFinish = {
                    navController.navigate(Screen.RunSummary.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        composable(Screen.RunSummary.route) {
            RunSummaryScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
