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
import androidx.compose.material3.Button
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
import com.otero.runningvoicecoach.data.session.RunStepSummary
import com.otero.runningvoicecoach.domain.model.PaceStatus
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
                text = "Resumen",
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
                val latestSession = sessions.first()
                SessionCard(session = latestSession, title = "Ultima carrera")
                StepComplianceSummary(session = latestSession)
                StepSummaryList(steps = latestSession.stepSummaries)
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBack
                ) {
                    Text("Volver al inicio")
                }
                Text(
                    text = "Historial",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                sessions.forEach { session ->
                    SessionCard(session = session)
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: RunSessionSummary,
    title: String? = null
) {
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
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
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
private fun StepComplianceSummary(session: RunSessionSummary) {
    if (session.stepSummaries.isEmpty()) {
        return
    }

    val withinTarget = session.stepSummaries.count { it.paceStatus == PaceStatus.WITHIN_TARGET }
    val tooFast = session.stepSummaries.count { it.paceStatus == PaceStatus.TOO_FAST }
    val tooSlow = session.stepSummaries.count { it.paceStatus == PaceStatus.TOO_SLOW }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryMetric("En objetivo", withinTarget.toString())
            SummaryMetric("Rapidos", tooFast.toString())
            SummaryMetric("Lentos", tooSlow.toString())
        }
    }
}

@Composable
private fun StepSummaryList(steps: List<RunStepSummary>) {
    if (steps.isEmpty()) {
        Text(
            text = "Esta sesion no tiene detalle por bloque.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
        )
        return
    }

    Text(
        text = "Bloques",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    steps.forEachIndexed { index, step ->
        StepSummaryCard(index = index, step = step)
    }
}

@Composable
private fun StepSummaryCard(index: Int, step: RunStepSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "${index + 1}. ${step.stepName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryMetric("Distancia", "%.2f km".format(step.distanceMeters / 1000.0))
                SummaryMetric("Tiempo", formatDuration(step.durationSeconds))
                SummaryMetric("Ritmo", PaceCalculator.formatPace(step.averagePaceSecondsPerKm))
            }
            Text(
                text = step.paceStatus.summaryLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = when (step.paceStatus) {
                    PaceStatus.WITHIN_TARGET -> MaterialTheme.colorScheme.primary
                    PaceStatus.TOO_FAST -> MaterialTheme.colorScheme.tertiary
                    PaceStatus.TOO_SLOW -> MaterialTheme.colorScheme.error
                    PaceStatus.NO_TARGET -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                },
                fontWeight = FontWeight.SemiBold
            )
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

private fun PaceStatus.summaryLabel(): String {
    return when (this) {
        PaceStatus.WITHIN_TARGET -> "Dentro del objetivo"
        PaceStatus.TOO_FAST -> "Demasiado rapido"
        PaceStatus.TOO_SLOW -> "Demasiado lento"
        PaceStatus.NO_TARGET -> "Sin objetivo de ritmo"
    }
}
