package com.otero.runningvoicecoach.openai

import com.otero.runningvoicecoach.domain.alert.AlertEvent
import com.otero.runningvoicecoach.domain.alert.AlertType

data class RunningAlertContext(
    val alertType: AlertType,
    val currentStepName: String?,
    val targetPaceSecondsPerKm: Int?,
    val currentPaceSecondsPerKm: Int?,
    val paceDifferenceSeconds: Int?,
    val remainingDistanceMeters: Double?,
    val remainingTimeSeconds: Long?
) {
    companion object {
        fun fromAlertEvent(
            alertEvent: AlertEvent,
            targetPaceSecondsPerKm: Int?,
            currentPaceSecondsPerKm: Int?
        ): RunningAlertContext {
            val paceDifferenceSeconds = when {
                targetPaceSecondsPerKm == null || currentPaceSecondsPerKm == null -> null
                else -> currentPaceSecondsPerKm - targetPaceSecondsPerKm
            }

            return RunningAlertContext(
                alertType = alertEvent.type,
                currentStepName = alertEvent.stepName,
                targetPaceSecondsPerKm = targetPaceSecondsPerKm,
                currentPaceSecondsPerKm = currentPaceSecondsPerKm,
                paceDifferenceSeconds = paceDifferenceSeconds,
                remainingDistanceMeters = alertEvent.remainingDistanceMeters,
                remainingTimeSeconds = alertEvent.remainingTimeSeconds
            )
        }
    }
}
