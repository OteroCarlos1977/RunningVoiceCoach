package com.otero.runningvoicecoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.otero.runningvoicecoach.navigation.RunningVoiceCoachNavHost
import com.otero.runningvoicecoach.ui.theme.RunningVoiceCoachTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RunningVoiceCoachTheme {
                RunningVoiceCoachNavHost()
            }
        }
    }
}
