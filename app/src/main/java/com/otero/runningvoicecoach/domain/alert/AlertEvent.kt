package com.otero.runningvoicecoach.domain.alert

import com.otero.runningvoicecoach.domain.model.PaceStatus

data class AlertEvent(
    val type: AlertType,
    val priority: AlertPriority,
    val createdAtMillis: Long,
    val stepIndex: Int,
    val stepName: String?,
    val totalDistanceMeters: Double,
    val remainingDistanceMeters: Double?,
    val remainingTimeSeconds: Long?,
    val paceStatus: PaceStatus
)
