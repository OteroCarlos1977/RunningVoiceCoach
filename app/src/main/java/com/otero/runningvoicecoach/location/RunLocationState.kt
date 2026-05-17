package com.otero.runningvoicecoach.location

data class RunLocationState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val altitudeMeters: Double? = null,
    val speedMetersPerSecond: Float? = null,
    val totalDistanceMeters: Double = 0.0,
    val currentPaceSecondsPerKm: Int? = null,
    val timestampMillis: Long = 0L,
    val isTracking: Boolean = false,
    val lastError: String? = null
)
