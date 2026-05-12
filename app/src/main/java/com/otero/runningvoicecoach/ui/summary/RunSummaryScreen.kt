package com.otero.runningvoicecoach.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.otero.runningvoicecoach.data.session.RunHistoryRepository
import com.otero.runningvoicecoach.data.session.RunSessionSummary
import com.otero.runningvoicecoach.domain.pace.PaceCalculator
import com.otero.runningvoicecoach.ui.components.AppScaffold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RunSummaryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { RunHistoryRepository(context.applicationContext) }
    val sessions by repository.sessions.collectAsState(initial = emptyList())

    AppScaffold(
        title = "Historial",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Historial",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            if (sessions.isEmpty()) {
                Text(
                    text = "Todavia no hay carreras finalizadas.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
                )
            } else {
                sessions.forEach { session ->
                    SessionCard(session = session)
                }
            }
        }
    }
}

@Composable
private fun SessionCard(session: RunSessionSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = session.workoutName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatDate(session.finishedAtMillis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryMetric("Distancia", "%.2f km".format(session.totalDistanceMeters / 1000.0))
                SummaryMetric("Tiempo", formatDuration(session.totalDurationSeconds))
                SummaryMetric("Ritmo", PaceCalculator.formatPace(session.averagePaceSecondsPerKm))
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}

private fun formatDate(timestampMillis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestampMillis))
}
