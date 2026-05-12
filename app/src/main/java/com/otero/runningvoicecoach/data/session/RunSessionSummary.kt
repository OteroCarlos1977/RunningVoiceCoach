package com.otero.runningvoicecoach.data.session

data class RunSessionSummary(
    val id: String,
    val workoutName: String,
    val finishedAtMillis: Long,
    val totalDistanceMeters: Double,
    val totalDurationSeconds: Long,
    val averagePaceSecondsPerKm: Int?
)
