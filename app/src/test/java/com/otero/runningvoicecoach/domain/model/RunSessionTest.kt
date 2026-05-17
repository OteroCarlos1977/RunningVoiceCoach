package com.otero.runningvoicecoach.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RunSessionTest {
    @Test
    fun runSession_calculatesDistanceKilometers() {
        val session = runSession(totalDistanceMeters = 10_500.0)

        assertEquals(10.5, session.distanceKilometers, 0.001)
    }

    @Test
    fun runSession_defaultsToFinishedWhenEndTimeExists() {
        val session = runSession(endTimeMillis = 2_000L)

        assertTrue(session.isFinished)
        assertEquals(RunSessionStatus.FINISHED, session.status)
    }

    @Test
    fun runSession_defaultsToActiveWhenEndTimeIsMissing() {
        val session = runSession(endTimeMillis = null)

        assertFalse(session.isFinished)
        assertEquals(RunSessionStatus.ACTIVE, session.status)
    }

    @Test
    fun pauseSegment_calculatesDurationWhenClosed() {
        val pause = RunPauseSegment(
            startedAtMillis = 1_000L,
            endedAtMillis = 11_000L,
            startedAtElapsedSeconds = 60L,
            endedAtElapsedSeconds = 70L
        )

        assertEquals(10L, pause.durationSeconds)
    }

    @Test
    fun pauseSegment_hasNoDurationWhenStillOpen() {
        val pause = RunPauseSegment(
            startedAtMillis = 1_000L,
            endedAtMillis = null,
            startedAtElapsedSeconds = 60L,
            endedAtElapsedSeconds = null
        )

        assertNull(pause.durationSeconds)
    }

    private fun runSession(
        endTimeMillis: Long? = 2_000L,
        totalDistanceMeters: Double = 1_000.0
    ): RunSession {
        return RunSession(
            id = "session-1",
            workoutPlanId = "workout-1",
            workoutName = "Prueba",
            startTimeMillis = 1_000L,
            endTimeMillis = endTimeMillis,
            totalDistanceMeters = totalDistanceMeters,
            totalDurationSeconds = 300L,
            averagePaceSecondsPerKm = 300,
            stepResults = emptyList()
        )
    }
}
