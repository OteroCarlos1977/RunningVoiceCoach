package com.otero.runningvoicecoach.ui.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.otero.runningvoicecoach.data.workout.CustomWorkoutRepository
import com.otero.runningvoicecoach.domain.model.StepType
import com.otero.runningvoicecoach.domain.model.TargetType
import com.otero.runningvoicecoach.domain.model.WorkoutPlan
import com.otero.runningvoicecoach.domain.model.WorkoutStep
import com.otero.runningvoicecoach.ui.components.AppScaffold
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun WorkoutEditorScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { CustomWorkoutRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val steps = remember { mutableStateListOf(EditorStepDraft()) }

    AppScaffold(
        title = "Crear rutina",
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
                text = "Rutina especial",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Nombre") }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = description,
                onValueChange = { description = it },
                label = { Text("Objetivo o descripcion") }
            )

            steps.forEachIndexed { index, step ->
                StepEditorCard(
                    index = index,
                    step = step,
                    canRemove = steps.size > 1,
                    onUpdate = { updated -> steps[index] = updated },
                    onRemove = { steps.removeAt(index) }
                )
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { steps += EditorStepDraft(name = "Bloque ${steps.size + 1}") }
            ) {
                Text("Agregar bloque")
            }

            error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val workout = buildWorkoutOrNull(
                        name = name,
                        description = description,
                        steps = steps,
                        onError = { error = it }
                    )
                    if (workout != null) {
                        scope.launch {
                            repository.saveWorkout(workout)
                            onSaved()
                        }
                    }
                }
            ) {
                Text("Guardar rutina")
            }
        }
    }
}

@Composable
private fun StepEditorCard(
    index: Int,
    step: EditorStepDraft,
    canRemove: Boolean,
    onUpdate: (EditorStepDraft) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Bloque ${index + 1}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = step.name,
                onValueChange = { onUpdate(step.copy(name = it)) },
                singleLine = true,
                label = { Text("Nombre del bloque") }
            )
            ChoiceRow(
                label = "Tipo",
                value = step.stepType.displayName(),
                onPrevious = { onUpdate(step.copy(stepType = step.stepType.previous())) },
                onNext = { onUpdate(step.copy(stepType = step.stepType.next())) }
            )
            ChoiceRow(
                label = "Objetivo",
                value = step.targetType.displayName(),
                onPrevious = { onUpdate(step.copy(targetType = step.targetType.toggle())) },
                onNext = { onUpdate(step.copy(targetType = step.targetType.toggle())) }
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = step.targetValue,
                onValueChange = { onUpdate(step.copy(targetValue = it.onlyDigits())) },
                singleLine = true,
                label = {
                    Text(if (step.targetType == TargetType.TIME_SECONDS) "Duracion en minutos" else "Distancia en kilometros")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = step.paceMinutes,
                onValueChange = { onUpdate(step.copy(paceMinutes = it.onlyDigits())) },
                singleLine = true,
                label = { Text("Ritmo objetivo minutos por km") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = step.paceSeconds,
                onValueChange = { onUpdate(step.copy(paceSeconds = it.onlyDigits().take(2))) },
                singleLine = true,
                label = { Text("Ritmo objetivo segundos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = step.toleranceSeconds,
                onValueChange = { onUpdate(step.copy(toleranceSeconds = it.onlyDigits())) },
                singleLine = true,
                label = { Text("Tolerancia segundos por km") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if (canRemove) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRemove
                ) {
                    Text("Quitar bloque")
                }
            }
        }
    }
}

@Composable
private fun ChoiceRow(
    label: String,
    value: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onPrevious
            ) {
                Text("<")
            }
            Text(
                modifier = Modifier
                    .weight(2f)
                    .padding(vertical = 10.dp),
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onNext
            ) {
                Text(">")
            }
        }
    }
}

private data class EditorStepDraft(
    val name: String = "Bloque 1",
    val stepType: StepType = StepType.EASY,
    val targetType: TargetType = TargetType.TIME_SECONDS,
    val targetValue: String = "5",
    val paceMinutes: String = "7",
    val paceSeconds: String = "00",
    val toleranceSeconds: String = "42"
)

private fun buildWorkoutOrNull(
    name: String,
    description: String,
    steps: List<EditorStepDraft>,
    onError: (String) -> Unit
): WorkoutPlan? {
    val cleanName = name.trim()
    if (cleanName.isBlank()) {
        onError("La rutina necesita un nombre.")
        return null
    }

    val workoutSteps = steps.mapIndexedNotNull { index, draft ->
        draft.toWorkoutStepOrNull(index)
    }
    if (workoutSteps.size != steps.size) {
        onError("Revisa bloques: objetivo, ritmo y tolerancia deben ser numeros validos.")
        return null
    }

    return WorkoutPlan(
        id = "custom-${UUID.randomUUID()}",
        name = cleanName,
        description = description.trim().takeIf { it.isNotBlank() },
        steps = workoutSteps
    )
}

private fun EditorStepDraft.toWorkoutStepOrNull(index: Int): WorkoutStep? {
    val rawTarget = targetValue.toDoubleOrNull()?.takeIf { it > 0.0 } ?: return null
    val target = when (targetType) {
        TargetType.TIME_SECONDS -> rawTarget * 60.0
        TargetType.DISTANCE_METERS -> rawTarget * 1000.0
    }
    val minutes = paceMinutes.toIntOrNull() ?: return null
    val seconds = paceSeconds.toIntOrNull() ?: return null
    if (seconds !in 0..59) {
        return null
    }

    return WorkoutStep(
        id = "step-${index + 1}-${UUID.randomUUID()}",
        name = name.trim().ifBlank { "Bloque ${index + 1}" },
        type = stepType,
        targetType = targetType,
        targetValue = target,
        targetPaceSecondsPerKm = minutes * 60 + seconds,
        paceToleranceSeconds = toleranceSeconds.toIntOrNull()?.coerceAtLeast(0) ?: return null
    )
}

private fun String.onlyDigits(): String {
    return filter { it.isDigit() }
}

private fun StepType.displayName(): String {
    return when (this) {
        StepType.WARMUP -> "Entrada en calor"
        StepType.EASY -> "Suave"
        StepType.INTERVAL -> "Intervalo"
        StepType.RECOVERY -> "Recuperacion"
        StepType.TEMPO -> "Tempo"
        StepType.COOLDOWN -> "Vuelta a la calma"
    }
}

private fun StepType.next(): StepType {
    val values = StepType.entries
    return values[(ordinal + 1) % values.size]
}

private fun StepType.previous(): StepType {
    val values = StepType.entries
    return values[(ordinal - 1 + values.size) % values.size]
}

private fun TargetType.displayName(): String {
    return when (this) {
        TargetType.TIME_SECONDS -> "Tiempo"
        TargetType.DISTANCE_METERS -> "Distancia"
    }
}

private fun TargetType.toggle(): TargetType {
    return when (this) {
        TargetType.TIME_SECONDS -> TargetType.DISTANCE_METERS
        TargetType.DISTANCE_METERS -> TargetType.TIME_SECONDS
    }
}
