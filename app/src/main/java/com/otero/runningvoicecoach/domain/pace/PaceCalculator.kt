package com.otero.runningvoicecoach.domain.pace

import com.otero.runningvoicecoach.domain.model.PaceStatus
import kotlin.math.roundToInt

object PaceCalculator {
    fun calculatePaceSecondsPerKm(speedMetersPerSecond: Double): Int? {
        if (!speedMetersPerSecond.isFinite() || speedMetersPerSecond <= 0.0) {
            return null
        }

        return (METERS_PER_KILOMETER / speedMetersPerSecond).roundToInt()
    }

    fun calculateAveragePaceSecondsPerKm(
        distanceMeters: Double,
        durationSeconds: Long
    ): Int? {
        if (!distanceMeters.isFinite() || distanceMeters <= 0.0 || durationSeconds <= 0L) {
            return null
        }

        val distanceKilometers = distanceMeters / METERS_PER_KILOMETER
        return (durationSeconds / distanceKilometers).roundToInt()
    }

    fun formatPace(secondsPerKm: Int?): String {
        if (secondsPerKm == null || secondsPerKm < 0) {
            return "--:-- /km"
        }

        val minutes = secondsPerKm / SECONDS_PER_MINUTE
        val seconds = secondsPerKm % SECONDS_PER_MINUTE
        return "%d:%02d /km".format(minutes, seconds)
    }

    fun comparePace(
        current: Int?,
        target: Int?,
        tolerance: Int
    ): PaceStatus {
        if (current == null || target == null) {
            return PaceStatus.NO_TARGET
        }

        val safeTolerance = tolerance.coerceAtLeast(0)
        val difference = current - target

        return when {
            difference < -safeTolerance -> PaceStatus.TOO_FAST
            difference > safeTolerance -> PaceStatus.TOO_SLOW
            else -> PaceStatus.WITHIN_TARGET
        }
    }

    private const val METERS_PER_KILOMETER = 1000.0
    private const val SECONDS_PER_MINUTE = 60
}
