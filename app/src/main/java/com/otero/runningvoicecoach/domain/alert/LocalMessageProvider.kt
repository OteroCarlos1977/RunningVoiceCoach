package com.otero.runningvoicecoach.domain.alert

import kotlin.math.roundToInt

object LocalMessageProvider {
    fun messageFor(event: AlertEvent): String {
        return when (event.type) {
            AlertType.STEP_STARTED -> stepStartedMessage(event)
            AlertType.STEP_ENDING_SOON -> stepEndingSoonMessage(event)
            AlertType.STEP_COMPLETED -> stepCompletedMessage(event)
            AlertType.TOO_FAST -> "Vas rapido. Baja un poco el ritmo."
            AlertType.TOO_SLOW -> "Vas lento. Subi apenas el ritmo."
            AlertType.WITHIN_TARGET -> "Buen ritmo. Mantene esta velocidad."
            AlertType.KILOMETER_COMPLETED -> kilometerCompletedMessage(event)
            AlertType.WORKOUT_FINISHED -> "Entrenamiento finalizado. Buen trabajo."
        }
    }

    private fun stepStartedMessage(event: AlertEvent): String {
        val stepName = event.stepName?.takeIf { it.isNotBlank() }
        return if (stepName != null) {
            "Empezas $stepName."
        } else {
            "Empezas un nuevo bloque."
        }
    }

    private fun stepEndingSoonMessage(event: AlertEvent): String {
        val remainingDistance = event.remainingDistanceMeters
        if (remainingDistance != null) {
            val roundedMeters = remainingDistance.roundToInt().coerceAtLeast(0)
            return "Faltan $roundedMeters metros para terminar este bloque."
        }

        val remainingTime = event.remainingTimeSeconds
        if (remainingTime != null) {
            val safeSeconds = remainingTime.coerceAtLeast(0L)
            return "Faltan $safeSeconds segundos para terminar este bloque."
        }

        return "Estas cerca de terminar este bloque."
    }

    private fun stepCompletedMessage(event: AlertEvent): String {
        val stepName = event.stepName?.takeIf { it.isNotBlank() }
        return if (stepName != null) {
            "Terminaste $stepName. Pasa al siguiente bloque."
        } else {
            "Terminaste el bloque. Pasa al siguiente."
        }
    }

    private fun kilometerCompletedMessage(event: AlertEvent): String {
        val kilometer = (event.totalDistanceMeters / METERS_PER_KILOMETER)
            .toInt()
            .coerceAtLeast(1)
        return "Completaste $kilometer kilometro."
    }

    private const val METERS_PER_KILOMETER = 1000.0
}
