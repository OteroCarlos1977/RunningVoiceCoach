package com.otero.runningvoicecoach.domain.activity

import com.otero.runningvoicecoach.domain.model.RunKilometerSplit
import com.otero.runningvoicecoach.domain.model.RunRoutePoint
import com.otero.runningvoicecoach.domain.pace.PaceCalculator

class RunTelemetryRecorder(
    private val routeMinIntervalSeconds: Long = DEFAULT_ROUTE_MIN_INTERVAL_SECONDS,
    private val routeMinDistanceMeters: Double = DEFAULT_ROUTE_MIN_DISTANCE_METERS
) {
    private val kilometerSplits = mutableListOf<RunKilometerSplit>()
    private val routePoints = mutableListOf<RunRoutePoint>()
    private var nextKilometer = 1
    private var lastSplitElapsedSeconds = 0L
    private var lastSplitDistanceMeters = 0.0
    private var lastRouteElapsedSeconds: Long? = null
    private var lastRouteDistanceMeters: Double? = null
    private var maxSpeedKmh: Double? = null

    fun recordSample(
        elapsedSeconds: Long,
        totalDistanceMeters: Double,
        speedKmh: Double?,
        routePoint: RunRoutePoint? = null
    ) {
        updateMaxSpeed(speedKmh)
        recordCompletedKilometers(elapsedSeconds, totalDistanceMeters, speedKmh)
        recordRoutePointIfNeeded(routePoint)
    }

    fun snapshot(
        totalDistanceMeters: Double,
        totalDurationSeconds: Long,
        includePartialSplit: Boolean = true
    ): RunTelemetrySnapshot {
        val splits = kilometerSplits.toMutableList()
        if (includePartialSplit) {
            val partial = buildPartialSplit(totalDistanceMeters, totalDurationSeconds)
            if (partial != null) {
                splits += partial
            }
        }

        return RunTelemetrySnapshot(
            averageSpeedKmh = calculateAverageSpeedKmh(totalDistanceMeters, totalDurationSeconds),
            maxSpeedKmh = maxSpeedKmh,
            kilometerSplits = splits,
            routePoints = routePoints.toList()
        )
    }

    fun reset() {
        kilometerSplits.clear()
        routePoints.clear()
        nextKilometer = 1
        lastSplitElapsedSeconds = 0L
        lastSplitDistanceMeters = 0.0
        lastRouteElapsedSeconds = null
        lastRouteDistanceMeters = null
        maxSpeedKmh = null
    }

    private fun updateMaxSpeed(speedKmh: Double?) {
        if (speedKmh == null || speedKmh <= 0.0) {
            return
        }
        maxSpeedKmh = maxOf(maxSpeedKmh ?: speedKmh, speedKmh)
    }

    private fun recordCompletedKilometers(
        elapsedSeconds: Long,
        totalDistanceMeters: Double,
        speedKmh: Double?
    ) {
        while (totalDistanceMeters >= nextKilometer * METERS_PER_KILOMETER) {
            val splitDistanceMeters = (nextKilometer * METERS_PER_KILOMETER) - lastSplitDistanceMeters
            val splitDurationSeconds = (elapsedSeconds - lastSplitElapsedSeconds).coerceAtLeast(0L)
            kilometerSplits += RunKilometerSplit(
                kilometer = nextKilometer,
                distanceMeters = splitDistanceMeters,
                durationSeconds = splitDurationSeconds,
                averagePaceSecondsPerKm = PaceCalculator.calculateAveragePaceSecondsPerKm(
                    distanceMeters = splitDistanceMeters,
                    durationSeconds = splitDurationSeconds
                ),
                averageSpeedKmh = speedKmh
            )
            lastSplitElapsedSeconds = elapsedSeconds
            lastSplitDistanceMeters = nextKilometer * METERS_PER_KILOMETER
            nextKilometer += 1
        }
    }

    private fun buildPartialSplit(
        totalDistanceMeters: Double,
        totalDurationSeconds: Long
    ): RunKilometerSplit? {
        val partialDistanceMeters = totalDistanceMeters - lastSplitDistanceMeters
        if (partialDistanceMeters <= 0.0) {
            return null
        }

        val partialDurationSeconds = (totalDurationSeconds - lastSplitElapsedSeconds).coerceAtLeast(0L)
        if (partialDurationSeconds <= 0L) {
            return null
        }

        return RunKilometerSplit(
            kilometer = nextKilometer,
            distanceMeters = partialDistanceMeters,
            durationSeconds = partialDurationSeconds,
            averagePaceSecondsPerKm = PaceCalculator.calculateAveragePaceSecondsPerKm(
                distanceMeters = partialDistanceMeters,
                durationSeconds = partialDurationSeconds
            ),
            averageSpeedKmh = calculateAverageSpeedKmh(partialDistanceMeters, partialDurationSeconds)
        )
    }

    private fun recordRoutePointIfNeeded(routePoint: RunRoutePoint?) {
        if (routePoint == null) {
            return
        }

        val lastElapsed = lastRouteElapsedSeconds
        val lastDistance = lastRouteDistanceMeters
        val enoughTimePassed = lastElapsed == null ||
            routePoint.elapsedSeconds - lastElapsed >= routeMinIntervalSeconds
        val enoughDistancePassed = lastDistance == null ||
            routePoint.distanceMeters - lastDistance >= routeMinDistanceMeters

        if (!enoughTimePassed && !enoughDistancePassed) {
            return
        }

        routePoints += routePoint
        lastRouteElapsedSeconds = routePoint.elapsedSeconds
        lastRouteDistanceMeters = routePoint.distanceMeters
    }

    private fun calculateAverageSpeedKmh(distanceMeters: Double, durationSeconds: Long): Double? {
        if (distanceMeters <= 0.0 || durationSeconds <= 0L) {
            return null
        }
        return (distanceMeters / METERS_PER_KILOMETER) / (durationSeconds / SECONDS_PER_HOUR)
    }

    private companion object {
        const val METERS_PER_KILOMETER = 1000.0
        const val SECONDS_PER_HOUR = 3600.0
        const val DEFAULT_ROUTE_MIN_INTERVAL_SECONDS = 5L
        const val DEFAULT_ROUTE_MIN_DISTANCE_METERS = 10.0
    }
}

data class RunTelemetrySnapshot(
    val averageSpeedKmh: Double?,
    val maxSpeedKmh: Double?,
    val kilometerSplits: List<RunKilometerSplit>,
    val routePoints: List<RunRoutePoint>
)
