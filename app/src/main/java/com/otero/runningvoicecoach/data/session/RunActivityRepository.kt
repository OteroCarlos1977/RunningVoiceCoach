package com.otero.runningvoicecoach.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.otero.runningvoicecoach.data.appDataStore
import com.otero.runningvoicecoach.domain.model.PaceStatus
import com.otero.runningvoicecoach.domain.model.RunKilometerSplit
import com.otero.runningvoicecoach.domain.model.RunPauseSegment
import com.otero.runningvoicecoach.domain.model.RunRoutePoint
import com.otero.runningvoicecoach.domain.model.RunSession
import com.otero.runningvoicecoach.domain.model.RunSessionStatus
import com.otero.runningvoicecoach.domain.model.RunStepResult
import com.otero.runningvoicecoach.domain.model.StepType
import com.otero.runningvoicecoach.domain.model.TargetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class RunActivityRepository(
    private val context: Context
) {
    val sessions: Flow<List<RunSession>> = context.appDataStore.data.map { preferences ->
        preferences[ACTIVITIES]
            .orEmpty()
            .mapNotNull { encoded -> encoded.decodeRunSessionOrNull() }
            .sortedByDescending { it.endTimeMillis ?: it.startTimeMillis }
    }

    suspend fun saveSession(session: RunSession) {
        context.appDataStore.edit { preferences ->
            preferences[ACTIVITIES] = preferences[ACTIVITIES].orEmpty() + session.encode()
        }
    }

    private fun RunSession.encode(): String {
        return JSONObject()
            .put("id", id)
            .put("workoutPlanId", workoutPlanId ?: JSONObject.NULL)
            .put("workoutName", workoutName)
            .put("startTimeMillis", startTimeMillis)
            .put("endTimeMillis", endTimeMillis ?: JSONObject.NULL)
            .put("status", status.name)
            .put("totalDistanceMeters", totalDistanceMeters)
            .put("totalDurationSeconds", totalDurationSeconds)
            .put("elapsedDurationSeconds", elapsedDurationSeconds)
            .put("pausedDurationSeconds", pausedDurationSeconds)
            .put("averagePaceSecondsPerKm", averagePaceSecondsPerKm ?: JSONObject.NULL)
            .put("averageSpeedKmh", averageSpeedKmh ?: JSONObject.NULL)
            .put("maxSpeedKmh", maxSpeedKmh ?: JSONObject.NULL)
            .put("estimatedCalories", estimatedCalories ?: JSONObject.NULL)
            .put("elevationGainMeters", elevationGainMeters ?: JSONObject.NULL)
            .put("elevationLossMeters", elevationLossMeters ?: JSONObject.NULL)
            .put("steps", JSONArray().also { array -> stepResults.forEach { array.put(it.encode()) } })
            .put("kilometerSplits", JSONArray().also { array -> kilometerSplits.forEach { array.put(it.encode()) } })
            .put("routePoints", JSONArray().also { array -> routePoints.forEach { array.put(it.encode()) } })
            .put("pauseSegments", JSONArray().also { array -> pauseSegments.forEach { array.put(it.encode()) } })
            .toString()
    }

    private fun String.decodeRunSessionOrNull(): RunSession? {
        return runCatching {
            val json = JSONObject(this)
            RunSession(
                id = json.getString("id"),
                workoutPlanId = json.optionalString("workoutPlanId"),
                workoutName = json.optString("workoutName"),
                startTimeMillis = json.getLong("startTimeMillis"),
                endTimeMillis = json.optionalLong("endTimeMillis"),
                totalDistanceMeters = json.getDouble("totalDistanceMeters"),
                totalDurationSeconds = json.getLong("totalDurationSeconds"),
                averagePaceSecondsPerKm = json.optionalInt("averagePaceSecondsPerKm"),
                stepResults = json.optJSONArray("steps").decodeList { decodeRunStepResult() },
                status = json.optEnum("status", RunSessionStatus.FINISHED),
                elapsedDurationSeconds = json.optLong("elapsedDurationSeconds", json.getLong("totalDurationSeconds")),
                pausedDurationSeconds = json.optLong("pausedDurationSeconds", 0L),
                averageSpeedKmh = json.optionalDouble("averageSpeedKmh"),
                maxSpeedKmh = json.optionalDouble("maxSpeedKmh"),
                estimatedCalories = json.optionalInt("estimatedCalories"),
                elevationGainMeters = json.optionalDouble("elevationGainMeters"),
                elevationLossMeters = json.optionalDouble("elevationLossMeters"),
                kilometerSplits = json.optJSONArray("kilometerSplits").decodeList { decodeKilometerSplit() },
                routePoints = json.optJSONArray("routePoints").decodeList { decodeRoutePoint() },
                pauseSegments = json.optJSONArray("pauseSegments").decodeList { decodePauseSegment() }
            )
        }.getOrNull()
    }

    private fun RunStepResult.encode(): JSONObject {
        return JSONObject()
            .put("stepId", stepId)
            .put("stepName", stepName)
            .put("stepIndex", stepIndex ?: JSONObject.NULL)
            .put("stepType", stepType?.name ?: JSONObject.NULL)
            .put("targetType", targetType?.name ?: JSONObject.NULL)
            .put("targetValue", targetValue ?: JSONObject.NULL)
            .put("distanceMeters", distanceMeters)
            .put("durationSeconds", durationSeconds)
            .put("averagePaceSecondsPerKm", averagePaceSecondsPerKm ?: JSONObject.NULL)
            .put("targetPaceSecondsPerKm", targetPaceSecondsPerKm ?: JSONObject.NULL)
            .put("complianceStatus", complianceStatus.name)
            .put("startedAtElapsedSeconds", startedAtElapsedSeconds ?: JSONObject.NULL)
            .put("endedAtElapsedSeconds", endedAtElapsedSeconds ?: JSONObject.NULL)
    }

    private fun JSONObject.decodeRunStepResult(): RunStepResult {
        return RunStepResult(
            stepId = optString("stepId"),
            stepName = optString("stepName"),
            distanceMeters = optDouble("distanceMeters", 0.0),
            durationSeconds = optLong("durationSeconds", 0L),
            averagePaceSecondsPerKm = optionalInt("averagePaceSecondsPerKm"),
            targetPaceSecondsPerKm = optionalInt("targetPaceSecondsPerKm"),
            complianceStatus = optEnum("complianceStatus", PaceStatus.NO_TARGET),
            stepIndex = optionalInt("stepIndex"),
            stepType = optionalEnum<StepType>("stepType"),
            targetType = optionalEnum<TargetType>("targetType"),
            targetValue = optionalDouble("targetValue"),
            startedAtElapsedSeconds = optionalLong("startedAtElapsedSeconds"),
            endedAtElapsedSeconds = optionalLong("endedAtElapsedSeconds")
        )
    }

    private fun RunKilometerSplit.encode(): JSONObject {
        return JSONObject()
            .put("kilometer", kilometer)
            .put("distanceMeters", distanceMeters)
            .put("durationSeconds", durationSeconds)
            .put("averagePaceSecondsPerKm", averagePaceSecondsPerKm ?: JSONObject.NULL)
            .put("averageSpeedKmh", averageSpeedKmh ?: JSONObject.NULL)
            .put("elevationGainMeters", elevationGainMeters ?: JSONObject.NULL)
            .put("elevationLossMeters", elevationLossMeters ?: JSONObject.NULL)
    }

    private fun JSONObject.decodeKilometerSplit(): RunKilometerSplit {
        return RunKilometerSplit(
            kilometer = optInt("kilometer"),
            distanceMeters = optDouble("distanceMeters", 0.0),
            durationSeconds = optLong("durationSeconds", 0L),
            averagePaceSecondsPerKm = optionalInt("averagePaceSecondsPerKm"),
            averageSpeedKmh = optionalDouble("averageSpeedKmh"),
            elevationGainMeters = optionalDouble("elevationGainMeters"),
            elevationLossMeters = optionalDouble("elevationLossMeters")
        )
    }

    private fun RunRoutePoint.encode(): JSONObject {
        return JSONObject()
            .put("latitude", latitude)
            .put("longitude", longitude)
            .put("recordedAtMillis", recordedAtMillis)
            .put("elapsedSeconds", elapsedSeconds)
            .put("distanceMeters", distanceMeters)
            .put("accuracyMeters", accuracyMeters ?: JSONObject.NULL)
            .put("altitudeMeters", altitudeMeters ?: JSONObject.NULL)
            .put("speedMetersPerSecond", speedMetersPerSecond ?: JSONObject.NULL)
    }

    private fun JSONObject.decodeRoutePoint(): RunRoutePoint {
        return RunRoutePoint(
            latitude = getDouble("latitude"),
            longitude = getDouble("longitude"),
            recordedAtMillis = getLong("recordedAtMillis"),
            elapsedSeconds = getLong("elapsedSeconds"),
            distanceMeters = getDouble("distanceMeters"),
            accuracyMeters = optionalDouble("accuracyMeters")?.toFloat(),
            altitudeMeters = optionalDouble("altitudeMeters"),
            speedMetersPerSecond = optionalDouble("speedMetersPerSecond")?.toFloat()
        )
    }

    private fun RunPauseSegment.encode(): JSONObject {
        return JSONObject()
            .put("startedAtMillis", startedAtMillis)
            .put("endedAtMillis", endedAtMillis ?: JSONObject.NULL)
            .put("startedAtElapsedSeconds", startedAtElapsedSeconds)
            .put("endedAtElapsedSeconds", endedAtElapsedSeconds ?: JSONObject.NULL)
    }

    private fun JSONObject.decodePauseSegment(): RunPauseSegment {
        return RunPauseSegment(
            startedAtMillis = getLong("startedAtMillis"),
            endedAtMillis = optionalLong("endedAtMillis"),
            startedAtElapsedSeconds = getLong("startedAtElapsedSeconds"),
            endedAtElapsedSeconds = optionalLong("endedAtElapsedSeconds")
        )
    }

    private fun <T> JSONArray?.decodeList(decode: JSONObject.() -> T): List<T> {
        if (this == null) {
            return emptyList()
        }
        return buildList {
            for (index in 0 until length()) {
                val item = optJSONObject(index) ?: continue
                add(item.decode())
            }
        }
    }

    private fun JSONObject.optionalString(key: String): String? {
        return if (isNull(key)) null else optString(key)
    }

    private fun JSONObject.optionalInt(key: String): Int? {
        return if (isNull(key)) null else optInt(key)
    }

    private fun JSONObject.optionalLong(key: String): Long? {
        return if (isNull(key)) null else optLong(key)
    }

    private fun JSONObject.optionalDouble(key: String): Double? {
        return if (isNull(key)) null else optDouble(key)
    }

    private inline fun <reified T : Enum<T>> JSONObject.optEnum(key: String, fallback: T): T {
        return runCatching { enumValueOf<T>(optString(key)) }.getOrDefault(fallback)
    }

    private inline fun <reified T : Enum<T>> JSONObject.optionalEnum(key: String): T? {
        return if (isNull(key)) null else runCatching { enumValueOf<T>(optString(key)) }.getOrNull()
    }

    private companion object {
        val ACTIVITIES = stringSetPreferencesKey("run_activity_sessions")
    }
}
