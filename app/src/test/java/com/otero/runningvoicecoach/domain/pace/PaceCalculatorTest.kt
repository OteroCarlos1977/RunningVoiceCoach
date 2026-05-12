package com.otero.runningvoicecoach.domain.pace

import com.otero.runningvoicecoach.domain.model.PaceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaceCalculatorTest {
    @Test
    fun calculatePaceSecondsPerKm_returnsPaceForValidSpeed() {
        val result = PaceCalculator.calculatePaceSecondsPerKm(3.030303)

        assertEquals(330, result)
    }

    @Test
    fun calculatePaceSecondsPerKm_returnsNullForZeroSpeed() {
        val result = PaceCalculator.calculatePaceSecondsPerKm(0.0)

        assertNull(result)
    }

    @Test
    fun calculateAveragePaceSecondsPerKm_returnsNullForZeroDistance() {
        val result = PaceCalculator.calculateAveragePaceSecondsPerKm(
            distanceMeters = 0.0,
            durationSeconds = 300
        )

        assertNull(result)
    }

    @Test
    fun calculateAveragePaceSecondsPerKm_returnsPaceForValidDistanceAndDuration() {
        val result = PaceCalculator.calculateAveragePaceSecondsPerKm(
            distanceMeters = 2000.0,
            durationSeconds = 600
        )

        assertEquals(300, result)
    }

    @Test
    fun comparePace_returnsTooFastWhenCurrentIsBelowTargetBeyondTolerance() {
        val result = PaceCalculator.comparePace(
            current = 314,
            target = 330,
            tolerance = 15
        )

        assertEquals(PaceStatus.TOO_FAST, result)
    }

    @Test
    fun comparePace_returnsTooSlowWhenCurrentIsAboveTargetBeyondTolerance() {
        val result = PaceCalculator.comparePace(
            current = 346,
            target = 330,
            tolerance = 15
        )

        assertEquals(PaceStatus.TOO_SLOW, result)
    }

    @Test
    fun comparePace_returnsWithinTargetWhenCurrentIsInsideTolerance() {
        val result = PaceCalculator.comparePace(
            current = 345,
            target = 330,
            tolerance = 15
        )

        assertEquals(PaceStatus.WITHIN_TARGET, result)
    }

    @Test
    fun comparePace_returnsNoTargetWhenCurrentIsMissing() {
        val result = PaceCalculator.comparePace(
            current = null,
            target = 330,
            tolerance = 15
        )

        assertEquals(PaceStatus.NO_TARGET, result)
    }

    @Test
    fun comparePace_returnsNoTargetWhenTargetIsMissing() {
        val result = PaceCalculator.comparePace(
            current = 330,
            target = null,
            tolerance = 15
        )

        assertEquals(PaceStatus.NO_TARGET, result)
    }

    @Test
    fun formatPace_returnsMinutesAndSecondsPerKm() {
        val result = PaceCalculator.formatPace(330)

        assertEquals("5:30 /km", result)
    }

    @Test
    fun formatPace_returnsPlaceholderForMissingPace() {
        val result = PaceCalculator.formatPace(null)

        assertEquals("--:-- /km", result)
    }
}
