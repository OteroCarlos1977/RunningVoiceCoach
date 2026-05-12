package com.otero.runningvoicecoach.domain.model

data class WorkoutStep(
    val id: String,
    val name: String,
    val type: StepType,
    val targetType: TargetType,
    val targetValue: Double,
    val targetPaceSecondsPerKm: Int?,
    val paceToleranceSeconds: Int = 15
)
