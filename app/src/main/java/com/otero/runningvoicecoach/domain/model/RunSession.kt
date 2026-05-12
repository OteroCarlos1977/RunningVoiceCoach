package com.otero.runningvoicecoach.domain.model

data class RunSession(
    val id: String,
    val workoutPlanId: String?,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val totalDistanceMeters: Double,
    val totalDurationSeconds: Long,
    val averagePaceSecondsPerKm: Int?,
    val stepResults: List<RunStepResult>
)
