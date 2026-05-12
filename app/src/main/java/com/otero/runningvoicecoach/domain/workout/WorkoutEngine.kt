package com.otero.runningvoicecoach.domain.workout

import com.otero.runningvoicecoach.domain.model.PaceStatus
import com.otero.runningvoicecoach.domain.model.TargetType
import com.otero.runningvoicecoach.domain.model.WorkoutPlan
import com.otero.runningvoicecoach.domain.model.WorkoutStep
import com.otero.runningvoicecoach.domain.pace.PaceCalculator

class WorkoutEngine {
    fun evaluate(
        workoutPlan: WorkoutPlan,
        currentStepIndex: Int,
        totalDistanceMeters: Double,
        totalDurationSeconds: Long,
        stepDistanceMeters: Double,
        stepDurationSeconds: Long,
        currentPaceSecondsPerKm: Int?,
        paceToleranceSeconds: Int? = null
    ): WorkoutEngineState {
        val totalSteps = workoutPlan.steps.size
        val currentStep = workoutPlan.steps.getOrNull(currentStepIndex)

        if (currentStep == null) {
            return WorkoutEngineState(
                currentStep = null,
                currentStepIndex = currentStepIndex,
                totalSteps = totalSteps,
                stepProgressPercent = 1f,
                remainingDistanceMeters = null,
                remainingTimeSeconds = null,
                paceStatus = PaceStatus.NO_TARGET,
                paceDifferenceSeconds = null,
                shouldMoveToNextStep = false,
                isWorkoutFinished = true
            )
        }

        val stepProgress = calculateStepProgress(
            currentStep = currentStep,
            stepDistanceMeters = stepDistanceMeters,
            stepDurationSeconds = stepDurationSeconds
        )
        val shouldMoveToNextStep = stepProgress >= 1f
        val isLastStep = currentStepIndex == totalSteps - 1
        val paceStatus = PaceCalculator.comparePace(
            current = currentPaceSecondsPerKm,
            target = currentStep.targetPaceSecondsPerKm,
            tolerance = paceToleranceSeconds ?: currentStep.paceToleranceSeconds
        )

        return WorkoutEngineState(
            currentStep = currentStep,
            currentStepIndex = currentStepIndex,
            totalSteps = totalSteps,
            stepProgressPercent = stepProgress * PERCENT_MULTIPLIER,
            remainingDistanceMeters = currentStep.remainingDistance(stepDistanceMeters),
            remainingTimeSeconds = currentStep.remainingTime(stepDurationSeconds),
            paceStatus = paceStatus,
            paceDifferenceSeconds = calculatePaceDifference(
                current = currentPaceSecondsPerKm,
                target = currentStep.targetPaceSecondsPerKm
            ),
            shouldMoveToNextStep = shouldMoveToNextStep,
            isWorkoutFinished = shouldMoveToNextStep && isLastStep
        )
    }

    private fun calculateStepProgress(
        currentStep: WorkoutStep,
        stepDistanceMeters: Double,
        stepDurationSeconds: Long
    ): Float {
        val rawProgress = when (currentStep.targetType) {
            TargetType.TIME_SECONDS -> stepDurationSeconds / currentStep.targetValue
            TargetType.DISTANCE_METERS -> stepDistanceMeters / currentStep.targetValue
        }

        if (!rawProgress.isFinite()) {
            return 0f
        }

        return rawProgress.toFloat().coerceIn(0f, 1f)
    }

    private fun WorkoutStep.remainingDistance(stepDistanceMeters: Double): Double? {
        if (targetType != TargetType.DISTANCE_METERS) {
            return null
        }

        return (targetValue - stepDistanceMeters).coerceAtLeast(0.0)
    }

    private fun WorkoutStep.remainingTime(stepDurationSeconds: Long): Long? {
        if (targetType != TargetType.TIME_SECONDS) {
            return null
        }

        return (targetValue.toLong() - stepDurationSeconds).coerceAtLeast(0L)
    }

    private fun calculatePaceDifference(
        current: Int?,
        target: Int?
    ): Int? {
        if (current == null || target == null) {
            return null
        }

        return current - target
    }

    private companion object {
        const val PERCENT_MULTIPLIER = 100f
    }
}
