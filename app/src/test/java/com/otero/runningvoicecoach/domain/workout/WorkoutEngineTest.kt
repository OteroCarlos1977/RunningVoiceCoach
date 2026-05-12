package com.otero.runningvoicecoach.domain.workout

import com.otero.runningvoicecoach.domain.model.PaceStatus
import com.otero.runningvoicecoach.domain.model.StepType
import com.otero.runningvoicecoach.domain.model.TargetType
import com.otero.runningvoicecoach.domain.model.WorkoutPlan
import com.otero.runningvoicecoach.domain.model.WorkoutStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutEngineTest {
    private val engine = WorkoutEngine()

    @Test
    fun evaluate_returnsTimeStepNotFinished() {
        val plan = workoutPlan(
            timeStep(targetSeconds = 600.0)
        )

        val state = engine.evaluate(
            workoutPlan = plan,
            currentStepIndex = 0,
            totalDistanceMeters = 800.0,
            totalDurationSeconds = 300,
            stepDistanceMeters = 800.0,
            stepDurationSeconds = 300,
            currentPaceSecondsPerKm = null
        )

        assertEquals(plan.steps[0], state.currentStep)
        assertEquals(50f, state.stepProgressPercent)
        assertEquals(300L, state.remainingTimeSeconds)
        assertNull(state.remainingDistanceMeters)
        assertFalse(state.shouldMoveToNextStep)
        assertFalse(state.isWorkoutFinished)
    }

    @Test
    fun evaluate_returnsTimeStepFinished() {
        val plan = workoutPlan(
            timeStep(targetSeconds = 600.0),
            distanceStep(targetMeters = 1000.0)
        )

        val state = engine.evaluate(
            workoutPlan = plan,
            currentStepIndex = 0,
            totalDistanceMeters = 1400.0,
            totalDurationSeconds = 600,
            stepDistanceMeters = 1400.0,
            stepDurationSeconds = 600,
            currentPaceSecondsPerKm = null
        )

        assertEquals(100f, state.stepProgressPercent)
        assertEquals(0L, state.remainingTimeSeconds)
        assertTrue(state.shouldMoveToNextStep)
        assertFalse(state.isWorkoutFinished)
    }

    @Test
    fun evaluate_returnsDistanceStepNotFinished() {
        val plan = workoutPlan(
            distanceStep(targetMeters = 1000.0)
        )

        val state = engine.evaluate(
            workoutPlan = plan,
            currentStepIndex = 0,
            totalDistanceMeters = 750.0,
            totalDurationSeconds = 240,
            stepDistanceMeters = 750.0,
            stepDurationSeconds = 240,
            currentPaceSecondsPerKm = 320
        )

        assertEquals(75f, state.stepProgressPercent)
        assertEquals(250.0, state.remainingDistanceMeters ?: -1.0, 0.0)
        assertNull(state.remainingTimeSeconds)
        assertFalse(state.shouldMoveToNextStep)
        assertFalse(state.isWorkoutFinished)
    }

    @Test
    fun evaluate_returnsDistanceStepFinished() {
        val plan = workoutPlan(
            distanceStep(targetMeters = 1000.0),
            timeStep(targetSeconds = 120.0)
        )

        val state = engine.evaluate(
            workoutPlan = plan,
            currentStepIndex = 0,
            totalDistanceMeters = 1000.0,
            totalDurationSeconds = 330,
            stepDistanceMeters = 1000.0,
            stepDurationSeconds = 330,
            currentPaceSecondsPerKm = 330
        )

        assertEquals(100f, state.stepProgressPercent)
        assertEquals(0.0, state.remainingDistanceMeters ?: -1.0, 0.0)
        assertTrue(state.shouldMoveToNextStep)
        assertFalse(state.isWorkoutFinished)
    }

    @Test
    fun evaluate_returnsWorkoutFinishedWhenLastStepIsDone() {
        val plan = workoutPlan(
            distanceStep(targetMeters = 1000.0)
        )

        val state = engine.evaluate(
            workoutPlan = plan,
            currentStepIndex = 0,
            totalDistanceMeters = 1000.0,
            totalDurationSeconds = 330,
            stepDistanceMeters = 1000.0,
            stepDurationSeconds = 330,
            currentPaceSecondsPerKm = 330
        )

        assertTrue(state.shouldMoveToNextStep)
        assertTrue(state.isWorkoutFinished)
    }

    @Test
    fun evaluate_returnsWorkoutFinishedWhenStepIndexIsOutOfRange() {
        val plan = workoutPlan(
            distanceStep(targetMeters = 1000.0)
        )

        val state = engine.evaluate(
            workoutPlan = plan,
            currentStepIndex = 1,
            totalDistanceMeters = 1000.0,
            totalDurationSeconds = 330,
            stepDistanceMeters = 0.0,
            stepDurationSeconds = 0,
            currentPaceSecondsPerKm = null
        )

        assertNull(state.currentStep)
        assertTrue(state.isWorkoutFinished)
        assertFalse(state.shouldMoveToNextStep)
    }

    @Test
    fun evaluate_returnsTooFastWhenPaceIsFasterThanTarget() {
        val plan = workoutPlan(
            distanceStep(targetMeters = 1000.0)
        )

        val state = engine.evaluate(
            workoutPlan = plan,
            currentStepIndex = 0,
            totalDistanceMeters = 200.0,
            totalDurationSeconds = 60,
            stepDistanceMeters = 200.0,
            stepDurationSeconds = 60,
            currentPaceSecondsPerKm = 314
        )

        assertEquals(PaceStatus.TOO_FAST, state.paceStatus)
        assertEquals(-16, state.paceDifferenceSeconds)
    }

    @Test
    fun evaluate_returnsTooSlowWhenPaceIsSlowerThanTarget() {
        val plan = workoutPlan(
            distanceStep(targetMeters = 1000.0)
        )

        val state = engine.evaluate(
            workoutPlan = plan,
            currentStepIndex = 0,
            totalDistanceMeters = 200.0,
            totalDurationSeconds = 75,
            stepDistanceMeters = 200.0,
            stepDurationSeconds = 75,
            currentPaceSecondsPerKm = 346
        )

        assertEquals(PaceStatus.TOO_SLOW, state.paceStatus)
        assertEquals(16, state.paceDifferenceSeconds)
    }

    @Test
    fun evaluate_returnsWithinTargetWhenPaceIsInsideTolerance() {
        val plan = workoutPlan(
            distanceStep(targetMeters = 1000.0)
        )

        val state = engine.evaluate(
            workoutPlan = plan,
            currentStepIndex = 0,
            totalDistanceMeters = 200.0,
            totalDurationSeconds = 66,
            stepDistanceMeters = 200.0,
            stepDurationSeconds = 66,
            currentPaceSecondsPerKm = 345
        )

        assertEquals(PaceStatus.WITHIN_TARGET, state.paceStatus)
        assertEquals(15, state.paceDifferenceSeconds)
    }

    private fun workoutPlan(vararg steps: WorkoutStep): WorkoutPlan {
        return WorkoutPlan(
            id = "test-plan",
            name = "Test plan",
            steps = steps.toList()
        )
    }

    private fun timeStep(targetSeconds: Double): WorkoutStep {
        return WorkoutStep(
            id = "time-step",
            name = "Time step",
            type = StepType.WARMUP,
            targetType = TargetType.TIME_SECONDS,
            targetValue = targetSeconds,
            targetPaceSecondsPerKm = null,
            paceToleranceSeconds = 15
        )
    }

    private fun distanceStep(targetMeters: Double): WorkoutStep {
        return WorkoutStep(
            id = "distance-step",
            name = "Distance step",
            type = StepType.INTERVAL,
            targetType = TargetType.DISTANCE_METERS,
            targetValue = targetMeters,
            targetPaceSecondsPerKm = 330,
            paceToleranceSeconds = 15
        )
    }
}
