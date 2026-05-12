package com.otero.runningvoicecoach.data.workout

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.otero.runningvoicecoach.data.appDataStore
import com.otero.runningvoicecoach.domain.model.StepType
import com.otero.runningvoicecoach.domain.model.TargetType
import com.otero.runningvoicecoach.domain.model.WorkoutPlan
import com.otero.runningvoicecoach.domain.model.WorkoutStep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class CustomWorkoutRepository(
    private val context: Context
) {
    val workouts: Flow<List<WorkoutPlan>> = context.appDataStore.data.map { preferences ->
        preferences[CUSTOM_WORKOUTS]
            .orEmpty()
            .mapNotNull { encoded -> encoded.decodeWorkoutOrNull() }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun saveWorkout(workoutPlan: WorkoutPlan) {
        context.appDataStore.edit { preferences ->
            val current = preferences[CUSTOM_WORKOUTS]
                .orEmpty()
                .mapNotNull { it.decodeWorkoutOrNull() }
                .filterNot { it.id == workoutPlan.id }

            preferences[CUSTOM_WORKOUTS] = (current + workoutPlan)
                .map { it.encode() }
                .toSet()
        }
    }

    private fun WorkoutPlan.encode(): String {
        return JSONObject()
            .put("id", id)
            .put("name", name)
            .put("description", description ?: JSONObject.NULL)
            .put(
                "steps",
                JSONArray().also { stepsJson ->
                    steps.forEach { step ->
                        stepsJson.put(
                            JSONObject()
                                .put("id", step.id)
                                .put("name", step.name)
                                .put("type", step.type.name)
                                .put("targetType", step.targetType.name)
                                .put("targetValue", step.targetValue)
                                .put("targetPaceSecondsPerKm", step.targetPaceSecondsPerKm ?: JSONObject.NULL)
                                .put("paceToleranceSeconds", step.paceToleranceSeconds)
                        )
                    }
                }
            )
            .toString()
    }

    private fun String.decodeWorkoutOrNull(): WorkoutPlan? {
        return runCatching {
            val json = JSONObject(this)
            val stepsJson = json.getJSONArray("steps")
            val steps = buildList {
                for (index in 0 until stepsJson.length()) {
                    val step = stepsJson.getJSONObject(index)
                    add(
                        WorkoutStep(
                            id = step.getString("id"),
                            name = step.getString("name"),
                            type = StepType.valueOf(step.getString("type")),
                            targetType = TargetType.valueOf(step.getString("targetType")),
                            targetValue = step.getDouble("targetValue"),
                            targetPaceSecondsPerKm = step.optionalInt("targetPaceSecondsPerKm"),
                            paceToleranceSeconds = step.optInt("paceToleranceSeconds", 15)
                        )
                    )
                }
            }

            WorkoutPlan(
                id = json.getString("id"),
                name = json.getString("name"),
                description = json.optString("description").takeIf { it.isNotBlank() && it != "null" },
                steps = steps
            )
        }.getOrNull()
    }

    private fun JSONObject.optionalInt(key: String): Int? {
        return if (isNull(key)) null else optInt(key)
    }

    private companion object {
        val CUSTOM_WORKOUTS = stringSetPreferencesKey("custom_workouts")
    }
}
