package com.otero.runningvoicecoach.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.otero.runningvoicecoach.data.appDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
        return listOf(
            id,
            workoutName.sanitize(),
            finishedAtMillis.toString(),
            totalDistanceMeters.toString(),
            totalDurationSeconds.toString(),
            averagePaceSecondsPerKm?.toString().orEmpty()
        ).joinToString(SEPARATOR)
    }

    private fun String.decodeSessionOrNull(): RunSessionSummary? {
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

    private fun String.sanitize(): String {
        return replace(SEPARATOR, " ")
    }

    private companion object {
        const val SEPARATOR = "|"
        val SESSIONS = stringSetPreferencesKey("run_sessions")
    }
}
