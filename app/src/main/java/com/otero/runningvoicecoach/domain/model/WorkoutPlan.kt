package com.otero.runningvoicecoach.domain.model

data class WorkoutPlan(
    val id: String,
    val name: String,
    val description: String? = null,
    val steps: List<WorkoutStep>
)
