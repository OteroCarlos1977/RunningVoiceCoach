package com.otero.runningvoicecoach.domain.workout

import com.otero.runningvoicecoach.domain.model.PaceStatus
import com.otero.runningvoicecoach.domain.model.WorkoutStep

data class WorkoutEngineState(
    val currentStep: WorkoutStep?,
    val currentStepIndex: Int,
    val totalSteps: Int,
    val stepProgressPercent: Float,
    val remainingDistanceMeters: Double?,
    val remainingTimeSeconds: Long?,
    val paceStatus: PaceStatus,
    val paceDifferenceSeconds: Int?,
    val shouldMoveToNextStep: Boolean,
    val isWorkoutFinished: Boolean
)
