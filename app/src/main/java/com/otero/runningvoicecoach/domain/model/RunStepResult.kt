package com.otero.runningvoicecoach.domain.model

data class RunStepResult(
    val stepId: String,
    val stepName: String,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val averagePaceSecondsPerKm: Int?,
    val targetPaceSecondsPerKm: Int?,
    val complianceStatus: PaceStatus,
    val stepIndex: Int? = null,
    val stepType: StepType? = null,
    val targetType: TargetType? = null,
    val targetValue: Double? = null,
    val startedAtElapsedSeconds: Long? = null,
    val endedAtElapsedSeconds: Long? = null
)
