package com.otero.runningvoicecoach.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.appDataStore by preferencesDataStore(name = "running_voice_coach")
