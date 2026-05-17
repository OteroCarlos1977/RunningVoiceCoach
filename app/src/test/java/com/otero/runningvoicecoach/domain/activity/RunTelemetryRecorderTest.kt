package com.otero.runningvoicecoach.domain.activity

import com.otero.runningvoicecoach.domain.model.RunRoutePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RunTelemetryRecorderTest {
    @Test
    fun recordSample_createsOneSplitPerCompletedKilometer() {
        val recorder = RunTelemetryRecorder()

        recorder.recordSample(elapsedSeconds = 360L, totalDistanceMeters = 1_000.0, speedKmh = 10.0)
        recorder.recordSample(elapsedSeconds = 720L, totalDistanceMeters = 2_000.0, speedKmh = 10.0)

        val snapshot = recorder.snapshot(
            totalDistanceMeters = 2_000.0,
            totalDurationSeconds = 720L,
            includePartialSplit = false
        )

        assertEquals(2, snapshot.kilometerSplits.size)
        assertEquals(1, snapshot.kilometerSplits[0].kilometer)
        assertEquals(360, snapshot.kilometerSplits[0].averagePaceSecondsPerKm)
        assertEquals(2, snapshot.kilometerSplits[1].kilometer)
    }

    @Test
    fun snapshot_includesPartialSplitWhenActivityDoesNotEndOnExactKilometer() {
        val recorder = RunTelemetryRecorder()

        recorder.recordSample(elapsedSeconds = 360L, totalDistanceMeters = 1_000.0, speedKmh = 10.0)

        val snapshot = recorder.snapshot(
            totalDistanceMeters = 1_500.0,
            totalDurationSeconds = 540L
        )

        assertEquals(2, snapshot.kilometerSplits.size)
        assertEquals(2, snapshot.kilometerSplits[1].kilometer)
        assertEquals(500.0, snapshot.kilometerSplits[1].distanceMeters, 0.001)
        assertEquals(360, snapshot.kilometerSplits[1].averagePaceSecondsPerKm)
    }

    @Test
    fun recordSample_keepsMaxSpeed() {
        val recorder = RunTelemetryRecorder()

        recorder.recordSample(elapsedSeconds = 1L, totalDistanceMeters = 1.0, speedKmh = 8.0)
        recorder.recordSample(elapsedSeconds = 2L, totalDistanceMeters = 4.0, speedKmh = 12.5)
        recorder.recordSample(elapsedSeconds = 3L, totalDistanceMeters = 8.0, speedKmh = 9.0)

        assertEquals(
            12.5,
            recorder.snapshot(totalDistanceMeters = 8.0, totalDurationSeconds = 3L).maxSpeedKmh ?: 0.0,
            0.001
        )
    }

    @Test
    fun recordSample_decimatesRoutePointsByTimeOrDistance() {
        val recorder = RunTelemetryRecorder(
            routeMinIntervalSeconds = 5L,
            routeMinDistanceMeters = 10.0
        )

        recorder.recordSample(elapsedSeconds = 1L, totalDistanceMeters = 1.0, speedKmh = null, routePoint = routePoint(1L, 1.0))
        recorder.recordSample(elapsedSeconds = 2L, totalDistanceMeters = 2.0, speedKmh = null, routePoint = routePoint(2L, 2.0))
        recorder.recordSample(elapsedSeconds = 6L, totalDistanceMeters = 6.0, speedKmh = null, routePoint = routePoint(6L, 6.0))
        recorder.recordSample(elapsedSeconds = 7L, totalDistanceMeters = 17.0, speedKmh = null, routePoint = routePoint(7L, 17.0))

        val snapshot = recorder.snapshot(totalDistanceMeters = 17.0, totalDurationSeconds = 7L)

        assertEquals(3, snapshot.routePoints.size)
        assertNotNull(snapshot.routePoints.firstOrNull { it.elapsedSeconds == 1L })
        assertNotNull(snapshot.routePoints.firstOrNull { it.elapsedSeconds == 6L })
        assertNotNull(snapshot.routePoints.firstOrNull { it.elapsedSeconds == 7L })
    }

    private fun routePoint(elapsedSeconds: Long, distanceMeters: Double): RunRoutePoint {
        return RunRoutePoint(
            latitude = -36.0,
            longitude = -57.0,
            recordedAtMillis = elapsedSeconds * 1_000L,
            elapsedSeconds = elapsedSeconds,
            distanceMeters = distanceMeters
        )
    }
}
