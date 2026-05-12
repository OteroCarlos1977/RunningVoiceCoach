package com.otero.runningvoicecoach.ui.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.otero.runningvoicecoach.data.workout.CustomWorkoutRepository
import com.otero.runningvoicecoach.domain.model.TargetType
import com.otero.runningvoicecoach.domain.model.WorkoutPlan
import com.otero.runningvoicecoach.domain.workout.ExampleWorkouts
import com.otero.runningvoicecoach.domain.workout.RoutinePreset
import com.otero.runningvoicecoach.ui.components.AppScaffold

@Composable
fun WorkoutListScreen(
    onBack: () -> Unit,
    onCreateWorkout: () -> Unit,
    onSelectWorkout: (String) -> Unit,
    routines: List<RoutinePreset> = ExampleWorkouts.presets
) {
    val context = LocalContext.current
    val repository = remember { CustomWorkoutRepository(context.applicationContext) }
    val customWorkouts by repository.workouts.collectAsState(initial = emptyList())

    AppScaffold(
        title = "Rutinas",
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
                text = "Rutinas",
                style = MaterialTheme.typography.displaySmall,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Elegi tu entrenamiento y segui avanzando.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                ),
                onClick = onCreateWorkout
            ) {
                Text("Programar rutina especial")
            }

            if (customWorkouts.isNotEmpty()) {
                Text(
                    text = "Tus rutinas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                customWorkouts.forEach { workout ->
                    CustomWorkoutCard(
                        workout = workout,
                        onSelectWorkout = onSelectWorkout
                    )
                }
            }

            Text(
                text = "Rutinas base",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            routines.forEach { routine ->
                RoutinePresetCard(
                    routine = routine,
                    onSelectWorkout = onSelectWorkout
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomWorkoutCard(
    workout: WorkoutPlan,
    onSelectWorkout: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = workout.name,
                style = MaterialTheme.typography.titleLarge,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold
            )
            workout.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoPill(text = "${workout.steps.size} bloques")
                InfoPill(text = workout.estimatedDurationLabel())
                InfoPill(text = "Personalizada", color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f), contentColor = MaterialTheme.colorScheme.tertiary)
            }
            workout.steps.take(4).forEach { step ->
                Text(
                    text = "• ${step.name}: ${step.targetLabel()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                onClick = { onSelectWorkout(workout.id) }
            ) {
                Text("Seleccionar")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoutinePresetCard(
    routine: RoutinePreset,
    onSelectWorkout: (String) -> Unit
) {
    val accent = routine.nivel.accentColor()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = accent
                ) {}
            }
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = routine.workoutPlan.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = routine.objetivo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoPill(text = routine.duracion)
                    InfoPill(text = routine.nivel, color = accent.copy(alpha = 0.13f), contentColor = accent)
                    InfoPill(text = "${routine.workoutPlan.steps.size} bloques")
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    routine.detalle.take(3).forEach { item ->
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
                        )
                    }
                }

                Text(
                    text = "Ideal para: ${routine.idealPara}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    onClick = { onSelectWorkout(routine.workoutPlan.id) }
                ) {
                    Text("Seleccionar")
                }
            }
        }
    }
}

@Composable
private fun InfoPill(
    text: String,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun String.accentColor(): Color {
    return when (this) {
        "Avanzado" -> Color(0xFFFF6A00)
        "Intermedio" -> Color(0xFF1565C0)
        "Suave" -> Color(0xFF00A8A8)
        else -> Color(0xFF1565C0)
    }
}

private fun WorkoutPlan.estimatedDurationLabel(): String {
    val seconds = steps
        .filter { it.targetType == TargetType.TIME_SECONDS }
        .sumOf { it.targetValue }
        .toLong()

    return if (seconds > 0L) {
        "${seconds / 60L} min"
    } else {
        "Por distancia"
    }
}

private fun com.otero.runningvoicecoach.domain.model.WorkoutStep.targetLabel(): String {
    return when (targetType) {
        TargetType.TIME_SECONDS -> "${targetValue.toLong() / 60L} min"
        TargetType.DISTANCE_METERS -> "%.2f km".format(targetValue / 1000.0)
    }
}
