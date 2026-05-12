package com.otero.runningvoicecoach.domain.alert

import com.otero.runningvoicecoach.domain.model.PaceStatus
import com.otero.runningvoicecoach.domain.workout.WorkoutEngineState
import kotlin.math.floor

class AlertEngine(
    private val minPaceAlertIntervalMillis: Long = DEFAULT_MIN_PACE_ALERT_INTERVAL_MILLIS
) {
    private val startedStepIndexes = mutableSetOf<Int>()
    private val completedStepIndexes = mutableSetOf<Int>()
    private val endingSoonStepIndexes = mutableSetOf<Int>()
    private var lastPaceAlertAtMillis: Long? = null
    private var lastKilometerAlerted = 0
    private var workoutFinishedAlerted = false

    fun evaluate(
        state: WorkoutEngineState,
        totalDistanceMeters: Double,
        nowMillis: Long,
        minPaceAlertIntervalMillisOverride: Long? = null
    ): List<AlertEvent> {
        val alerts = mutableListOf<AlertEvent>()

        if (state.isWorkoutFinished) {
            addWorkoutFinishedAlert(
                alerts = alerts,
                state = state,
                totalDistanceMeters = totalDistanceMeters,
                nowMillis = nowMillis
            )
            return alerts.sortedByPriority()
        }

        addStepStartedAlert(
            alerts = alerts,
            state = state,
            totalDistanceMeters = totalDistanceMeters,
            nowMillis = nowMillis
        )

        addStepCompletedAlert(
            alerts = alerts,
            state = state,
            totalDistanceMeters = totalDistanceMeters,
            nowMillis = nowMillis
        )

        if (alerts.any { it.priority == AlertPriority.HIGH }) {
            return alerts.sortedByPriority()
        }

        addEndingSoonAlert(
            alerts = alerts,
            state = state,
            totalDistanceMeters = totalDistanceMeters,
            nowMillis = nowMillis
        )

        addKilometerAlert(
            alerts = alerts,
            state = state,
            totalDistanceMeters = totalDistanceMeters,
            nowMillis = nowMillis
        )

        addPaceAlert(
            alerts = alerts,
            state = state,
            totalDistanceMeters = totalDistanceMeters,
            nowMillis = nowMillis,
            minPaceAlertIntervalMillisOverride = minPaceAlertIntervalMillisOverride
        )

        return alerts.sortedByPriority()
    }

    fun reset() {
        startedStepIndexes.clear()
        completedStepIndexes.clear()
        endingSoonStepIndexes.clear()
        lastPaceAlertAtMillis = null
        lastKilometerAlerted = 0
        workoutFinishedAlerted = false
    }

    private fun addWorkoutFinishedAlert(
        alerts: MutableList<AlertEvent>,
        state: WorkoutEngineState,
        totalDistanceMeters: Double,
        nowMillis: Long
    ) {
        if (workoutFinishedAlerted) {
            return
        }

        workoutFinishedAlerted = true
        alerts += state.toAlertEvent(
            type = AlertType.WORKOUT_FINISHED,
            priority = AlertPriority.HIGH,
            totalDistanceMeters = totalDistanceMeters,
            nowMillis = nowMillis
        )
    }

    private fun addStepStartedAlert(
        alerts: MutableList<AlertEvent>,
        state: WorkoutEngineState,
        totalDistanceMeters: Double,
        nowMillis: Long
    ) {
        if (state.currentStep == null || !startedStepIndexes.add(state.currentStepIndex)) {
            return
        }

        alerts += state.toAlertEvent(
            type = AlertType.STEP_STARTED,
            priority = AlertPriority.HIGH,
            totalDistanceMeters = totalDistanceMeters,
            nowMillis = nowMillis
        )
    }

    private fun addStepCompletedAlert(
        alerts: MutableList<AlertEvent>,
        state: WorkoutEngineState,
        totalDistanceMeters: Double,
        nowMillis: Long
    ) {
        if (!state.shouldMoveToNextStep || !completedStepIndexes.add(state.currentStepIndex)) {
            return
        }

        alerts += state.toAlertEvent(
            type = AlertType.STEP_COMPLETED,
            priority = AlertPriority.HIGH,
            totalDistanceMeters = totalDistanceMeters,
            nowMillis = nowMillis
        )
    }

    private fun addEndingSoonAlert(
        alerts: MutableList<AlertEvent>,
        state: WorkoutEngineState,
        totalDistanceMeters: Double,
        nowMillis: Long
    ) {
        if (!endingSoonStepIndexes.add(state.currentStepIndex)) {
            return
        }

        val shouldAlert = state.remainingDistanceMeters?.let { it in 0.0..ENDING_SOON_METERS } == true ||
            state.remainingTimeSeconds?.let { it in 0L..ENDING_SOON_SECONDS } == true

        if (!shouldAlert || state.shouldMoveToNextStep) {
            endingSoonStepIndexes.remove(state.currentStepIndex)
            return
        }

        alerts += state.toAlertEvent(
            type = AlertType.STEP_ENDING_SOON,
            priority = AlertPriority.LOW,
            totalDistanceMeters = totalDistanceMeters,
            nowMillis = nowMillis
        )
    }

    private fun addKilometerAlert(
        alerts: MutableList<AlertEvent>,
        state: WorkoutEngineState,
        totalDistanceMeters: Double,
        nowMillis: Long
    ) {
        val completedKilometer = floor(totalDistanceMeters / METERS_PER_KILOMETER).toInt()
        if (completedKilometer <= 0 || completedKilometer <= lastKilometerAlerted) {
            return
        }

        lastKilometerAlerted = completedKilometer
        alerts += state.toAlertEvent(
            type = AlertType.KILOMETER_COMPLETED,
            priority = AlertPriority.LOW,
            totalDistanceMeters = totalDistanceMeters,
            nowMillis = nowMillis
        )
    }

    private fun addPaceAlert(
        alerts: MutableList<AlertEvent>,
        state: WorkoutEngineState,
        totalDistanceMeters: Double,
        nowMillis: Long,
        minPaceAlertIntervalMillisOverride: Long?
    ) {
        val alertType = when (state.paceStatus) {
            PaceStatus.TOO_FAST -> AlertType.TOO_FAST
            PaceStatus.TOO_SLOW -> AlertType.TOO_SLOW
            PaceStatus.WITHIN_TARGET -> AlertType.WITHIN_TARGET
            PaceStatus.NO_TARGET -> return
        }

        val lastAlert = lastPaceAlertAtMillis
        val intervalMillis = minPaceAlertIntervalMillisOverride ?: minPaceAlertIntervalMillis
        if (lastAlert != null && nowMillis - lastAlert < intervalMillis) {
            return
        }

        lastPaceAlertAtMillis = nowMillis
        alerts += state.toAlertEvent(
            type = alertType,
            priority = AlertPriority.NORMAL,
            totalDistanceMeters = totalDistanceMeters,
            nowMillis = nowMillis
        )
    }

    private fun WorkoutEngineState.toAlertEvent(
        type: AlertType,
        priority: AlertPriority,
        totalDistanceMeters: Double,
        nowMillis: Long
    ): AlertEvent {
        return AlertEvent(
            type = type,
            priority = priority,
            createdAtMillis = nowMillis,
            stepIndex = currentStepIndex,
            stepName = currentStep?.name,
            totalDistanceMeters = totalDistanceMeters,
            remainingDistanceMeters = remainingDistanceMeters,
            remainingTimeSeconds = remainingTimeSeconds,
            paceStatus = paceStatus
        )
    }

    private fun List<AlertEvent>.sortedByPriority(): List<AlertEvent> {
        return sortedByDescending { it.priority.ordinal }
    }

    private companion object {
        const val DEFAULT_MIN_PACE_ALERT_INTERVAL_MILLIS = 30_000L
        const val ENDING_SOON_METERS = 200.0
        const val ENDING_SOON_SECONDS = 60L
        const val METERS_PER_KILOMETER = 1000.0
    }
}
