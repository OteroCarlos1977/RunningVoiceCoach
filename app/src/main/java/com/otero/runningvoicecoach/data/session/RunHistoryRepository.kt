package com.otero.runningvoicecoach.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.otero.runningvoicecoach.data.appDataStore
import com.otero.runningvoicecoach.domain.model.PaceStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class RunHistoryRepository(
    private val context: Context
) {
    val sessions: Flow<List<RunSessionSummary>> = context.appDataStore.data.map { preferences ->
        preferences[SESSIONS]
            .orEmpty()
            .mapNotNull { encoded -> encoded.decodeSessionOrNull() }
            .sortedByDescending { it.finishedAtMillis }
    }

    suspend fun saveSession(summary: RunSessionSummary) {
        context.appDataStore.edit { preferences ->
            preferences[SESSIONS] = preferences[SESSIONS].orEmpty() + summary.encode()
        }
    }

    private fun RunSessionSummary.encode(): String {
        return JSONObject()
            .put("id", id)
            .put("workoutName", workoutName)
            .put("finishedAtMillis", finishedAtMillis)
            .put("totalDistanceMeters", totalDistanceMeters)
            .put("totalDurationSeconds", totalDurationSeconds)
            .put("averagePaceSecondsPerKm", averagePaceSecondsPerKm ?: JSONObject.NULL)
            .put(
                "steps",
                JSONArray().also { steps ->
                    stepSummaries.forEach { step ->
                        steps.put(
                            JSONObject()
                                .put("stepName", step.stepName)
                                .put("distanceMeters", step.distanceMeters)
                                .put("durationSeconds", step.durationSeconds)
                                .put("averagePaceSecondsPerKm", step.averagePaceSecondsPerKm ?: JSONObject.NULL)
                                .put("paceStatus", step.paceStatus.name)
                        )
                    }
                }
            )
            .toString()
    }

    private fun String.decodeSessionOrNull(): RunSessionSummary? {
        if (trimStart().startsWith("{")) {
            return decodeJsonSessionOrNull()
        }

        return decodeLegacySessionOrNull()
    }

    private fun String.decodeJsonSessionOrNull(): RunSessionSummary? {
        return runCatching {
            val json = JSONObject(this)
            val stepsJson = json.optJSONArray("steps") ?: JSONArray()
            val stepSummaries = buildList {
                for (index in 0 until stepsJson.length()) {
                    val step = stepsJson.optJSONObject(index) ?: continue
                    add(
                        RunStepSummary(
                            stepName = step.optString("stepName", "Bloque"),
                            distanceMeters = step.optDouble("distanceMeters", 0.0),
                            durationSeconds = step.optLong("durationSeconds", 0L),
                            averagePaceSecondsPerKm = step.optionalInt("averagePaceSecondsPerKm"),
                            paceStatus = step.optPaceStatus()
                        )
                    )
                }
            }

            RunSessionSummary(
                id = json.getString("id"),
                workoutName = json.getString("workoutName"),
                finishedAtMillis = json.getLong("finishedAtMillis"),
                totalDistanceMeters = json.getDouble("totalDistanceMeters"),
                totalDurationSeconds = json.getLong("totalDurationSeconds"),
                averagePaceSecondsPerKm = json.optionalInt("averagePaceSecondsPerKm"),
                stepSummaries = stepSummaries
            )
        }.getOrNull()
    }

    private fun String.decodeLegacySessionOrNull(): RunSessionSummary? {
        val parts = split(SEPARATOR)
        if (parts.size != 6) {
            return null
        }

        return RunSessionSummary(
            id = parts[0],
            workoutName = parts[1],
            finishedAtMillis = parts[2].toLongOrNull() ?: return null,
            totalDistanceMeters = parts[3].toDoubleOrNull() ?: return null,
            totalDurationSeconds = parts[4].toLongOrNull() ?: return null,
            averagePaceSecondsPerKm = parts[5].takeIf { it.isNotBlank() }?.toIntOrNull()
        )
    }

    private fun JSONObject.optionalInt(key: String): Int? {
        return if (isNull(key)) null else optInt(key)
    }

    private fun JSONObject.optPaceStatus(): PaceStatus {
        return runCatching { PaceStatus.valueOf(optString("paceStatus")) }
            .getOrDefault(PaceStatus.NO_TARGET)
    }

    private companion object {
        const val SEPARATOR = "|"
        val SESSIONS = stringSetPreferencesKey("run_sessions")
    }
}
