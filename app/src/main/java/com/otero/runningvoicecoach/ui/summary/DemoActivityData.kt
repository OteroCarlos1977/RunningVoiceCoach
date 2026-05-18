package com.otero.runningvoicecoach.ui.summary

import com.otero.runningvoicecoach.domain.model.RunKilometerSplit
import com.otero.runningvoicecoach.domain.model.RunRoutePoint
import com.otero.runningvoicecoach.domain.model.RunSession
import com.otero.runningvoicecoach.domain.model.RunSessionStatus

internal fun demoRunSession(): RunSession {
    val now = System.currentTimeMillis()
    return RunSession(
        id = "demo-actividad",
        workoutPlanId = "demo",
        workoutName = "10K prueba exterior",
        startTimeMillis = now - 4_145_000L,
        endTimeMillis = now,
        totalDistanceMeters = 10_050.0,
        totalDurationSeconds = 4_084L,
        averagePaceSecondsPerKm = 406,
        stepResults = emptyList(),
        status = RunSessionStatus.FINISHED,
        elapsedDurationSeconds = 4_145L,
        pausedDurationSeconds = 61L,
        averageSpeedKmh = 8.86,
        maxSpeedKmh = 11.1,
        estimatedCalories = 704,
        kilometerSplits = listOf(
            RunKilometerSplit(1, 1000.0, 386L, 386, 9.3),
            RunKilometerSplit(2, 1000.0, 397L, 397, 9.1),
            RunKilometerSplit(3, 1000.0, 402L, 402, 9.0),
            RunKilometerSplit(4, 1000.0, 407L, 407, 8.8),
            RunKilometerSplit(5, 1000.0, 405L, 405, 8.9),
            RunKilometerSplit(6, 1000.0, 411L, 411, 8.8),
            RunKilometerSplit(7, 1000.0, 408L, 408, 8.8),
            RunKilometerSplit(8, 1000.0, 399L, 399, 9.0),
            RunKilometerSplit(9, 1000.0, 429L, 429, 8.4),
            RunKilometerSplit(10, 1000.0, 438L, 438, 8.2),
            RunKilometerSplit(11, 50.0, 22L, 440, 8.1)
        ),
        routePoints = listOf(
            RunRoutePoint(-36.0900, -57.8050, now - 4_084_000L, 0L, 0.0),
            RunRoutePoint(-36.0888, -57.8040, now - 3_700_000L, 384L, 900.0),
            RunRoutePoint(-36.0876, -57.8028, now - 3_300_000L, 784L, 1_900.0),
            RunRoutePoint(-36.0868, -57.8010, now - 2_900_000L, 1_184L, 2_900.0),
            RunRoutePoint(-36.0878, -57.7994, now - 2_500_000L, 1_584L, 3_900.0),
            RunRoutePoint(-36.0895, -57.7988, now - 2_100_000L, 1_984L, 4_900.0),
            RunRoutePoint(-36.0910, -57.7996, now - 1_700_000L, 2_384L, 5_900.0),
            RunRoutePoint(-36.0918, -57.8016, now - 1_300_000L, 2_784L, 6_900.0),
            RunRoutePoint(-36.0907, -57.8034, now - 900_000L, 3_184L, 7_900.0),
            RunRoutePoint(-36.0892, -57.8048, now - 500_000L, 3_584L, 8_900.0),
            RunRoutePoint(-36.0900, -57.8050, now, 4_084L, 10_050.0)
        )
    )
}
