package com.otero.runningvoicecoach.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.otero.runningvoicecoach.data.appDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserSettingsRepository(
    private val context: Context
) {
    val settings: Flow<UserSettings> = context.appDataStore.data.map { preferences ->
        UserSettings(
            voiceEnabled = preferences[VOICE_ENABLED] ?: true,
            openAiEnabled = preferences[OPEN_AI_ENABLED] ?: false,
            minAlertIntervalSeconds = preferences[MIN_ALERT_INTERVAL_SECONDS] ?: 30,
            generalPaceToleranceSeconds = preferences[GENERAL_PACE_TOLERANCE_SECONDS] ?: 42,
            developmentOpenAiApiKey = preferences[DEVELOPMENT_OPEN_AI_API_KEY].orEmpty()
        )
    }

    suspend fun setVoiceEnabled(enabled: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[VOICE_ENABLED] = enabled
        }
    }

    suspend fun setOpenAiEnabled(enabled: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[OPEN_AI_ENABLED] = enabled
        }
    }

    suspend fun setMinAlertIntervalSeconds(seconds: Int) {
        context.appDataStore.edit { preferences ->
            preferences[MIN_ALERT_INTERVAL_SECONDS] = seconds.coerceAtLeast(5)
        }
    }

    suspend fun setGeneralPaceToleranceSeconds(seconds: Int) {
        context.appDataStore.edit { preferences ->
            preferences[GENERAL_PACE_TOLERANCE_SECONDS] = seconds.coerceAtLeast(0)
        }
    }

    suspend fun setDevelopmentOpenAiApiKey(apiKey: String) {
        context.appDataStore.edit { preferences ->
            val cleanedApiKey = apiKey.trim()
            if (cleanedApiKey.isBlank()) {
                preferences.remove(DEVELOPMENT_OPEN_AI_API_KEY)
            } else {
                preferences[DEVELOPMENT_OPEN_AI_API_KEY] = cleanedApiKey
            }
        }
    }

    private companion object {
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val OPEN_AI_ENABLED = booleanPreferencesKey("open_ai_enabled")
        val MIN_ALERT_INTERVAL_SECONDS = intPreferencesKey("min_alert_interval_seconds")
        val GENERAL_PACE_TOLERANCE_SECONDS = intPreferencesKey("general_pace_tolerance_seconds")
        val DEVELOPMENT_OPEN_AI_API_KEY = stringPreferencesKey("development_open_ai_api_key")
    }
}
