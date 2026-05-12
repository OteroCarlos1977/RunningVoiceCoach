package com.otero.runningvoicecoach.data.session

import com.otero.runningvoicecoach.domain.model.PaceStatus

data class RunSessionSummary(
    val id: String,
    val workoutName: String,
    val finishedAtMillis: Long,
    val totalDistanceMeters: Double,
    val totalDurationSeconds: Long,
    val averagePaceSecondsPerKm: Int?,
    val stepSummaries: List<RunStepSummary> = emptyList()
)

data class RunStepSummary(
    val stepName: String,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val averagePaceSecondsPerKm: Int?,
    val paceStatus: PaceStatus
)
