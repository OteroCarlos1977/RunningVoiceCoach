package com.otero.runningvoicecoach.domain.alert

import com.otero.runningvoicecoach.domain.model.PaceStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalMessageProviderTest {
    @Test
    fun messageFor_returnsMessageForEveryAlertType() {
        AlertType.entries.forEach { type ->
            val message = LocalMessageProvider.messageFor(alertEvent(type))

            assertTrue("Missing message for $type", message.isNotBlank())
        }
    }

    @Test
    fun messageFor_keepsEveryAlertTypeWithinEighteenWords() {
        AlertType.entries.forEach { type ->
            val message = LocalMessageProvider.messageFor(alertEvent(type))

            assertTrue(
                "Message for $type has too many words: $message",
                message.wordCount() <= 18
            )
        }
    }

    @Test
    fun messageFor_usesRemainingMetersWhenEndingSoonByDistance() {
        val message = LocalMessageProvider.messageFor(
            alertEvent(
                type = AlertType.STEP_ENDING_SOON,
                remainingDistanceMeters = 200.0,
                remainingTimeSeconds = null
            )
        )

        assertTrue(message.contains("200 metros"))
    }

    @Test
    fun messageFor_usesRemainingSecondsWhenEndingSoonByTime() {
        val message = LocalMessageProvider.messageFor(
            alertEvent(
                type = AlertType.STEP_ENDING_SOON,
                remainingDistanceMeters = null,
                remainingTimeSeconds = 60L
            )
        )

        assertTrue(message.contains("60 segundos"))
    }

    @Test
    fun messageFor_doesNotIncludeMedicalOrAggressiveLanguage() {
        AlertType.entries.forEach { type ->
            val message = LocalMessageProvider.messageFor(alertEvent(type)).lowercase()

            assertFalse(message.contains("lesion"))
            assertFalse(message.contains("medico"))
            assertFalse(message.contains("si o si"))
            assertFalse(message.contains("aunque te sientas mal"))
        }
    }

    private fun alertEvent(
        type: AlertType,
        remainingDistanceMeters: Double? = 200.0,
        remainingTimeSeconds: Long? = null
    ): AlertEvent {
        return AlertEvent(
            type = type,
            priority = AlertPriority.NORMAL,
            createdAtMillis = 0L,
            stepIndex = 0,
            stepName = "Intervalo 1",
            totalDistanceMeters = 1_000.0,
            remainingDistanceMeters = remainingDistanceMeters,
            remainingTimeSeconds = remainingTimeSeconds,
            paceStatus = PaceStatus.WITHIN_TARGET
        )
    }

    private fun String.wordCount(): Int {
        return trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .size
    }
}
