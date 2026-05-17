package com.otero.runningvoicecoach.domain.model

data class RunSession(
    val id: String,
    val workoutPlanId: String?,
    val workoutName: String = "",
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val totalDistanceMeters: Double,
    val totalDurationSeconds: Long,
    val averagePaceSecondsPerKm: Int?,
    val stepResults: List<RunStepResult>,
    val status: RunSessionStatus = if (endTimeMillis == null) RunSessionStatus.ACTIVE else RunSessionStatus.FINISHED,
    val elapsedDurationSeconds: Long = totalDurationSeconds,
    val pausedDurationSeconds: Long = 0L,
    val averageSpeedKmh: Double? = null,
    val maxSpeedKmh: Double? = null,
    val estimatedCalories: Int? = null,
    val elevationGainMeters: Double? = null,
    val elevationLossMeters: Double? = null,
    val kilometerSplits: List<RunKilometerSplit> = emptyList(),
    val routePoints: List<RunRoutePoint> = emptyList(),
    val pauseSegments: List<RunPauseSegment> = emptyList()
) {
    val distanceKilometers: Double
        get() = totalDistanceMeters / METERS_PER_KILOMETER

    val isFinished: Boolean
        get() = status == RunSessionStatus.FINISHED

    companion object {
        private const val METERS_PER_KILOMETER = 1000.0
    }
}

enum class RunSessionStatus {
    ACTIVE,
    PAUSED,
    FINISHED,
    CANCELLED
}

data class RunKilometerSplit(
    val kilometer: Int,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val averagePaceSecondsPerKm: Int?,
    val averageSpeedKmh: Double? = null,
    val elevationGainMeters: Double? = null,
    val elevationLossMeters: Double? = null
)

data class RunRoutePoint(
    val latitude: Double,
    val longitude: Double,
    val recordedAtMillis: Long,
    val elapsedSeconds: Long,
    val distanceMeters: Double,
    val accuracyMeters: Float? = null,
    val altitudeMeters: Double? = null,
    val speedMetersPerSecond: Float? = null
)

data class RunPauseSegment(
    val startedAtMillis: Long,
    val endedAtMillis: Long?,
    val startedAtElapsedSeconds: Long,
    val endedAtElapsedSeconds: Long?
) {
    val durationSeconds: Long?
        get() {
            val ended = endedAtElapsedSeconds ?: return null
            return (ended - startedAtElapsedSeconds).coerceAtLeast(0L)
        }
}
