package com.otero.runningvoicecoach.openai

import com.otero.runningvoicecoach.domain.alert.AlertEvent
import com.otero.runningvoicecoach.domain.alert.AlertPriority
import com.otero.runningvoicecoach.domain.alert.AlertType
import com.otero.runningvoicecoach.domain.model.PaceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RunningAlertContextTest {
    @Test
    fun `fromAlertEvent keeps only running alert data`() {
        val alertEvent = AlertEvent(
            type = AlertType.TOO_FAST,
            priority = AlertPriority.HIGH,
            createdAtMillis = 1_000L,
            stepIndex = 2,
            stepName = "Serie 1K",
            totalDistanceMeters = 1_400.0,
            remainingDistanceMeters = 250.0,
            remainingTimeSeconds = null,
            paceStatus = PaceStatus.TOO_FAST
        )

        val context = RunningAlertContext.fromAlertEvent(
            alertEvent = alertEvent,
            targetPaceSecondsPerKm = 420,
            currentPaceSecondsPerKm = 390
        )

        assertEquals(AlertType.TOO_FAST, context.alertType)
        assertEquals("Serie 1K", context.currentStepName)
        assertEquals(420, context.targetPaceSecondsPerKm)
        assertEquals(390, context.currentPaceSecondsPerKm)
        assertEquals(-30, context.paceDifferenceSeconds)
        assertEquals(250.0, context.remainingDistanceMeters)
        assertNull(context.remainingTimeSeconds)
    }
}
