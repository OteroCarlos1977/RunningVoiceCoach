package com.otero.runningvoicecoach.domain.alert

import com.otero.runningvoicecoach.domain.model.PaceStatus
import com.otero.runningvoicecoach.domain.model.StepType
import com.otero.runningvoicecoach.domain.model.TargetType
import com.otero.runningvoicecoach.domain.model.WorkoutStep
import com.otero.runningvoicecoach.domain.workout.WorkoutEngineState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertEngineTest {
    @Test
    fun evaluate_returnsStepStartedAlertOnceWithHighPriority() {
        val engine = AlertEngine()
        val state = engineState()

        val firstAlerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 0.0,
            nowMillis = 0L
        )
        val secondAlerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 0.0,
            nowMillis = 1_000L
        )

        assertEquals(AlertType.STEP_STARTED, firstAlerts.first().type)
        assertEquals(AlertPriority.HIGH, firstAlerts.first().priority)
        assertFalse(secondAlerts.any { it.type == AlertType.STEP_STARTED })
    }

    @Test
    fun evaluate_doesNotRepeatPaceAlertBeforeMinimumInterval() {
        val engine = AlertEngine()
        val state = engineState(paceStatus = PaceStatus.TOO_FAST)

        engine.evaluate(state = state, totalDistanceMeters = 0.0, nowMillis = 0L)
        val firstPaceAlerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 100.0,
            nowMillis = 1_000L
        )
        val repeatedPaceAlerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 200.0,
            nowMillis = 10_000L
        )

        assertTrue(firstPaceAlerts.any { it.type == AlertType.TOO_FAST })
        assertFalse(repeatedPaceAlerts.any { it.type == AlertType.TOO_FAST })
    }

    @Test
    fun evaluate_allowsPaceAlertAfterMinimumInterval() {
        val engine = AlertEngine()
        val state = engineState(paceStatus = PaceStatus.TOO_SLOW)

        engine.evaluate(state = state, totalDistanceMeters = 0.0, nowMillis = 0L)
        engine.evaluate(state = state, totalDistanceMeters = 100.0, nowMillis = 1_000L)
        val alerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 200.0,
            nowMillis = 31_000L
        )

        assertTrue(alerts.any { it.type == AlertType.TOO_SLOW })
    }

    @Test
    fun evaluate_returnsWithinTargetPaceAlert() {
        val engine = AlertEngine()
        val state = engineState(paceStatus = PaceStatus.WITHIN_TARGET)

        engine.evaluate(state = state, totalDistanceMeters = 0.0, nowMillis = 0L)
        val alerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 100.0,
            nowMillis = 1_000L
        )

        assertTrue(alerts.any { it.type == AlertType.WITHIN_TARGET })
    }

    @Test
    fun evaluate_returnsEndingSoonAlertForDistanceStepOnce() {
        val engine = AlertEngine()
        val state = engineState(
            remainingDistanceMeters = 200.0,
            remainingTimeSeconds = null
        )

        engine.evaluate(state = state, totalDistanceMeters = 700.0, nowMillis = 0L)
        val firstAlerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 800.0,
            nowMillis = 1_000L
        )
        val repeatedAlerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 850.0,
            nowMillis = 2_000L
        )

        assertTrue(firstAlerts.any { it.type == AlertType.STEP_ENDING_SOON })
        assertFalse(repeatedAlerts.any { it.type == AlertType.STEP_ENDING_SOON })
    }

    @Test
    fun evaluate_returnsEndingSoonAlertForTimeStep() {
        val engine = AlertEngine()
        val state = engineState(
            remainingDistanceMeters = null,
            remainingTimeSeconds = 60L
        )

        engine.evaluate(state = state, totalDistanceMeters = 0.0, nowMillis = 0L)
        val alerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 0.0,
            nowMillis = 1_000L
        )

        assertTrue(alerts.any { it.type == AlertType.STEP_ENDING_SOON })
    }

    @Test
    fun evaluate_returnsKilometerCompletedAlertOncePerKilometer() {
        val engine = AlertEngine()
        val state = engineState()

        engine.evaluate(state = state, totalDistanceMeters = 0.0, nowMillis = 0L)
        val firstKilometerAlerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 1_000.0,
            nowMillis = 1_000L
        )
        val repeatedKilometerAlerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 1_200.0,
            nowMillis = 2_000L
        )

        assertTrue(firstKilometerAlerts.any { it.type == AlertType.KILOMETER_COMPLETED })
        assertFalse(repeatedKilometerAlerts.any { it.type == AlertType.KILOMETER_COMPLETED })
    }

    @Test
    fun evaluate_returnsStepCompletedAlertWithHighPriority() {
        val engine = AlertEngine()
        val state = engineState(shouldMoveToNextStep = true)

        val alerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 1_000.0,
            nowMillis = 0L
        )

        assertTrue(alerts.any { it.type == AlertType.STEP_COMPLETED })
        assertTrue(alerts.filter { it.type == AlertType.STEP_COMPLETED }.all { it.priority == AlertPriority.HIGH })
    }

    @Test
    fun evaluate_returnsWorkoutFinishedAlertWithHighPriorityOnlyOnce() {
        val engine = AlertEngine()
        val state = engineState(
            shouldMoveToNextStep = true,
            isWorkoutFinished = true
        )

        val firstAlerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 5_000.0,
            nowMillis = 0L
        )
        val repeatedAlerts = engine.evaluate(
            state = state,
            totalDistanceMeters = 5_000.0,
            nowMillis = 1_000L
        )

        assertEquals(AlertType.WORKOUT_FINISHED, firstAlerts.first().type)
        assertEquals(AlertPriority.HIGH, firstAlerts.first().priority)
        assertTrue(repeatedAlerts.isEmpty())
    }

    private fun engineState(
        paceStatus: PaceStatus = PaceStatus.NO_TARGET,
        remainingDistanceMeters: Double? = 500.0,
        remainingTimeSeconds: Long? = null,
        shouldMoveToNextStep: Boolean = false,
        isWorkoutFinished: Boolean = false
    ): WorkoutEngineState {
        return WorkoutEngineState(
            currentStep = WorkoutStep(
                id = "interval-1",
                name = "Intervalo 1",
                type = StepType.INTERVAL,
                targetType = TargetType.DISTANCE_METERS,
                targetValue = 1000.0,
                targetPaceSecondsPerKm = 330,
                paceToleranceSeconds = 15
            ),
            currentStepIndex = 0,
            totalSteps = 3,
            stepProgressPercent = 50f,
            remainingDistanceMeters = remainingDistanceMeters,
            remainingTimeSeconds = remainingTimeSeconds,
            paceStatus = paceStatus,
            paceDifferenceSeconds = null,
            shouldMoveToNextStep = shouldMoveToNextStep,
            isWorkoutFinished = isWorkoutFinished
        )
    }
}
