package com.otero.runningvoicecoach.ui.workouts

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.otero.runningvoicecoach.ui.components.AppScaffold
import com.otero.runningvoicecoach.ui.components.PlaceholderScreenBody

@Composable
fun WorkoutEditorScreen(onBack: () -> Unit) {
    AppScaffold(
        title = "Crear rutina",
        onBack = onBack
    ) { padding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(padding)
        ) {
            PlaceholderScreenBody(
                headline = "Editor de rutina",
                body = "En la siguiente fase se agregan los modelos de bloques por tiempo o distancia."
            )
        }
    }
}
