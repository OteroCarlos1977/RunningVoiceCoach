package com.otero.runningvoicecoach.data.settings

data class UserSettings(
    val voiceEnabled: Boolean = true,
    val openAiEnabled: Boolean = false,
    val minAlertIntervalSeconds: Int = 30,
    val generalPaceToleranceSeconds: Int = 42,
    val developmentOpenAiApiKey: String = ""
)
