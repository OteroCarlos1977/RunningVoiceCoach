package com.otero.runningvoicecoach.navigation

sealed class Screen(val route: String, val title: String) {
    data object Home : Screen("home", "RunningVoiceCoach")
    data object WorkoutList : Screen("workouts", "Rutinas")
    data object WorkoutEditor : Screen("workout-editor", "Crear rutina")
    data object ActiveRun : Screen("active-run/{workoutPlanId}", "Carrera activa") {
        const val ARG_WORKOUT_PLAN_ID = "workoutPlanId"

        fun createRoute(workoutPlanId: String): String = "active-run/$workoutPlanId"
    }
    data object RunSummary : Screen("run-summary", "Resumen")
    data object Health : Screen("health", "Salud")
    data object Settings : Screen("settings", "Configuración")
}
