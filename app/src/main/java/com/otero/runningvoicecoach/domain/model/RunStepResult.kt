package com.otero.runningvoicecoach.domain.model

data class RunStepResult(
    val stepId: String,
    val stepName: String,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val averagePaceSecondsPerKm: Int?,
    val targetPaceSecondsPerKm: Int?,
    val complianceStatus: PaceStatus
)
