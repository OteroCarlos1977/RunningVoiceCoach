package com.otero.runningvoicecoach.ui.workouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.otero.runningvoicecoach.R
import com.otero.runningvoicecoach.data.workout.CustomWorkoutRepository
import com.otero.runningvoicecoach.domain.model.StepType
import com.otero.runningvoicecoach.domain.model.TargetType
import com.otero.runningvoicecoach.domain.model.WorkoutPlan
import com.otero.runningvoicecoach.domain.model.WorkoutStep
import com.otero.runningvoicecoach.ui.components.AppScaffold
import kotlinx.coroutines.launch
import java.util.UUID

private val EditorNavy = Color(0xFF06245A)
private val EditorBlue = Color(0xFF006DE5)
private val EditorSoft = Color(0xFFF7FAFF)
private val EditorMuted = Color(0xFF577095)

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
    var routineMode by remember { mutableStateOf(RoutineMode.CONTINUOUS) }
    var error by remember { mutableStateOf<String?>(null) }
    var continuousBlockCount by remember { mutableIntStateOf(2) }
    val continuousBlocks = remember {
        mutableStateListOf(
            ContinuousBlockDraft(name = "Fondo suave", distanceKm = "4", paceMinutes = "7", paceSeconds = "30"),
            ContinuousBlockDraft(name = "Ritmo medio", distanceKm = "2", paceMinutes = "6", paceSeconds = "30")
        )
    }
    var intervalDraft by remember { mutableStateOf(IntervalRoutineDraft()) }

    fun syncContinuousBlocks(count: Int) {
        val safeCount = count.coerceIn(1, 6)
        continuousBlockCount = safeCount
        while (continuousBlocks.size < safeCount) {
            continuousBlocks += ContinuousBlockDraft(name = "Bloque ${continuousBlocks.size + 1}")
        }
        while (continuousBlocks.size > safeCount) {
            continuousBlocks.removeAt(continuousBlocks.lastIndex)
        }
    }

    AppScaffold(title = "Crear rutina", showTopBar = false) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EditorSoft)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 34.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_home),
                        contentDescription = "Runners",
                        modifier = Modifier
                            .width(220.dp)
                            .height(76.dp),
                        contentScale = ContentScale.Fit
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        onClick = onBack
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            text = "Volver",
                            color = EditorBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Rutina especial",
                    color = EditorNavy,
                    fontSize = 40.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Define bloques, pasadas y descansos con una estructura clara.",
                    color = EditorMuted,
                    fontSize = 18.sp,
                    lineHeight = 22.sp
                )

                SectionCard {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        label = { Text("Nombre de la rutina") }
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Objetivo o descripcion") }
                    )
                }

                Text(
                    text = "Estructura",
                    color = EditorNavy,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModeButton(
                        modifier = Modifier.weight(1f),
                        text = "Bloques continuos",
                        selected = routineMode == RoutineMode.CONTINUOUS,
                        onClick = { routineMode = RoutineMode.CONTINUOUS }
                    )
                    ModeButton(
                        modifier = Modifier.weight(1f),
                        text = "Pasadas con descanso",
                        selected = routineMode == RoutineMode.INTERVALS,
                        onClick = { routineMode = RoutineMode.INTERVALS }
                    )
                }

                when (routineMode) {
                    RoutineMode.CONTINUOUS -> ContinuousRoutineEditor(
                        blockCount = continuousBlockCount,
                        blocks = continuousBlocks,
                        onBlockCountChange = ::syncContinuousBlocks,
                        onBlockChange = { index, block -> continuousBlocks[index] = block }
                    )

                    RoutineMode.INTERVALS -> IntervalRoutineEditor(
                        draft = intervalDraft,
                        onDraftChange = { intervalDraft = it }
                    )
                }

                RoutinePreview(
                    mode = routineMode,
                    continuousBlocks = continuousBlocks,
                    intervalDraft = intervalDraft
                )

                error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    onClick = {
                        val workout = buildWorkoutOrNull(
                            name = name,
                            description = description,
                            mode = routineMode,
                            continuousBlocks = continuousBlocks,
                            intervalDraft = intervalDraft,
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
                    Text("Guardar rutina", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ContinuousRoutineEditor(
    blockCount: Int,
    blocks: List<ContinuousBlockDraft>,
    onBlockCountChange: (Int) -> Unit,
    onBlockChange: (Int, ContinuousBlockDraft) -> Unit
) {
    SectionCard {
        StepperRow(
            label = "Cantidad de bloques continuos",
            value = blockCount.toString(),
            onDecrease = { onBlockCountChange(blockCount - 1) },
            onIncrease = { onBlockCountChange(blockCount + 1) }
        )
        Text(
            text = "Ejemplo: 4 km suave + 2 km ritmo medio.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
        )
    }

    blocks.forEachIndexed { index, block ->
        ContinuousBlockCard(
            index = index,
            block = block,
            onChange = { onBlockChange(index, it) }
        )
    }
}

@Composable
private fun ContinuousBlockCard(
    index: Int,
    block: ContinuousBlockDraft,
    onChange: (ContinuousBlockDraft) -> Unit
) {
    SectionCard {
        Text(
            text = "Bloque ${index + 1}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = block.name,
            onValueChange = { onChange(block.copy(name = it)) },
            singleLine = true,
            label = { Text("Nombre del bloque") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = block.distanceKm,
            onValueChange = { onChange(block.copy(distanceKm = it.onlyDecimal())) },
            singleLine = true,
            label = { Text("Distancia en km") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        PaceFields(
            paceMinutes = block.paceMinutes,
            paceSeconds = block.paceSeconds,
            toleranceSeconds = block.toleranceSeconds,
            onPaceMinutesChange = { onChange(block.copy(paceMinutes = it.onlyDigits())) },
            onPaceSecondsChange = { onChange(block.copy(paceSeconds = it.onlyDigits().take(2))) },
            onToleranceChange = { onChange(block.copy(toleranceSeconds = it.onlyDigits())) }
        )
    }
}

@Composable
private fun IntervalRoutineEditor(
    draft: IntervalRoutineDraft,
    onDraftChange: (IntervalRoutineDraft) -> Unit
) {
    SectionCard {
        StepperRow(
            label = "Cantidad de pasadas",
            value = draft.repetitions,
            onDecrease = { onDraftChange(draft.copy(repetitions = draft.repetitions.decrementText(1))) },
            onIncrease = { onDraftChange(draft.copy(repetitions = draft.repetitions.incrementText())) }
        )
        Text(
            text = "Ejemplo: 4 pasadas de 1 km con 3 min de descanso.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
        )
    }

    SectionCard {
        Text(
            text = "Pasada",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = draft.intervalDistanceKm,
            onValueChange = { onDraftChange(draft.copy(intervalDistanceKm = it.onlyDecimal())) },
            singleLine = true,
            label = { Text("Distancia de cada pasada en km") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        PaceFields(
            paceMinutes = draft.intervalPaceMinutes,
            paceSeconds = draft.intervalPaceSeconds,
            toleranceSeconds = draft.intervalToleranceSeconds,
            onPaceMinutesChange = { onDraftChange(draft.copy(intervalPaceMinutes = it.onlyDigits())) },
            onPaceSecondsChange = { onDraftChange(draft.copy(intervalPaceSeconds = it.onlyDigits().take(2))) },
            onToleranceChange = { onDraftChange(draft.copy(intervalToleranceSeconds = it.onlyDigits())) }
        )
    }

    SectionCard {
        Text(
            text = "Descanso",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModeButton(
                modifier = Modifier.weight(1f),
                text = "Con descanso",
                selected = draft.hasRecovery,
                onClick = { onDraftChange(draft.copy(hasRecovery = true)) }
            )
            ModeButton(
                modifier = Modifier.weight(1f),
                text = "Sin descanso",
                selected = !draft.hasRecovery,
                onClick = { onDraftChange(draft.copy(hasRecovery = false)) }
            )
        }
        if (draft.hasRecovery) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft.recoveryMinutes,
                onValueChange = { onDraftChange(draft.copy(recoveryMinutes = it.onlyDigits())) },
                singleLine = true,
                label = { Text("Descanso en minutos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            PaceFields(
                paceMinutes = draft.recoveryPaceMinutes,
                paceSeconds = draft.recoveryPaceSeconds,
                toleranceSeconds = draft.recoveryToleranceSeconds,
                onPaceMinutesChange = { onDraftChange(draft.copy(recoveryPaceMinutes = it.onlyDigits())) },
                onPaceSecondsChange = { onDraftChange(draft.copy(recoveryPaceSeconds = it.onlyDigits().take(2))) },
                onToleranceChange = { onDraftChange(draft.copy(recoveryToleranceSeconds = it.onlyDigits())) }
            )
        }
    }
}

@Composable
private fun PaceFields(
    paceMinutes: String,
    paceSeconds: String,
    toleranceSeconds: String,
    onPaceMinutesChange: (String) -> Unit,
    onPaceSecondsChange: (String) -> Unit,
    onToleranceChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = paceMinutes,
            onValueChange = onPaceMinutesChange,
            singleLine = true,
            label = { Text("Min/km") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = paceSeconds,
            onValueChange = onPaceSecondsChange,
            singleLine = true,
            label = { Text("Seg") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = toleranceSeconds,
        onValueChange = onToleranceChange,
        singleLine = true,
        label = { Text("Tolerancia segundos por km") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun RoutinePreview(
    mode: RoutineMode,
    continuousBlocks: List<ContinuousBlockDraft>,
    intervalDraft: IntervalRoutineDraft
) {
    SectionCard {
        Text(
            text = "Vista previa",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        val lines: List<String> = when (mode) {
            RoutineMode.CONTINUOUS -> continuousBlocks.mapIndexed { index, block ->
                "${index + 1}. ${block.name.ifBlank { "Bloque ${index + 1}" }} - ${block.distanceKm.ifBlank { "0" }} km a ${block.paceMinutes}:${block.paceSeconds.padStart(2, '0')} /km"
            }

            RoutineMode.INTERVALS -> buildList {
                val repetitions = intervalDraft.repetitions.toIntOrNull()?.coerceAtLeast(1) ?: 1
                repeat(repetitions) { index ->
                    add("${index + 1}. Pasada ${index + 1} - ${intervalDraft.intervalDistanceKm.ifBlank { "0" }} km a ${intervalDraft.intervalPaceMinutes}:${intervalDraft.intervalPaceSeconds.padStart(2, '0')} /km")
                    if (intervalDraft.hasRecovery && index < repetitions - 1) {
                        add("   Descanso ${intervalDraft.recoveryMinutes.ifBlank { "0" }} min")
                    }
                }
            }
        }
        lines.take(10).forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.76f)
            )
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onDecrease
            ) {
                Text("-")
            }
            Surface(
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 12.dp),
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onIncrease
            ) {
                Text("+")
            }
        }
    }
}

@Composable
private fun ModeButton(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors(containerColor = EditorBlue)
    } else {
        ButtonDefaults.outlinedButtonColors()
    }

    if (selected) {
        Button(
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
            colors = colors,
            onClick = onClick
        ) {
            Text(text)
        }
    } else {
        OutlinedButton(
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
            onClick = onClick
        ) {
            Text(text)
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

private enum class RoutineMode {
    CONTINUOUS,
    INTERVALS
}

private data class ContinuousBlockDraft(
    val name: String = "Bloque",
    val distanceKm: String = "1",
    val paceMinutes: String = "7",
    val paceSeconds: String = "00",
    val toleranceSeconds: String = "42"
)

private data class IntervalRoutineDraft(
    val repetitions: String = "4",
    val intervalDistanceKm: String = "1",
    val intervalPaceMinutes: String = "6",
    val intervalPaceSeconds: String = "00",
    val intervalToleranceSeconds: String = "30",
    val hasRecovery: Boolean = true,
    val recoveryMinutes: String = "3",
    val recoveryPaceMinutes: String = "8",
    val recoveryPaceSeconds: String = "00",
    val recoveryToleranceSeconds: String = "60"
)

private fun buildWorkoutOrNull(
    name: String,
    description: String,
    mode: RoutineMode,
    continuousBlocks: List<ContinuousBlockDraft>,
    intervalDraft: IntervalRoutineDraft,
    onError: (String) -> Unit
): WorkoutPlan? {
    val cleanName = name.trim()
    if (cleanName.isBlank()) {
        onError("La rutina necesita un nombre.")
        return null
    }

    val steps = when (mode) {
        RoutineMode.CONTINUOUS -> buildContinuousSteps(continuousBlocks)
        RoutineMode.INTERVALS -> buildIntervalSteps(intervalDraft)
    }

    if (steps == null || steps.isEmpty()) {
        onError("Revisa los datos: distancia, ritmo y tolerancia deben ser validos.")
        return null
    }

    return WorkoutPlan(
        id = "custom-${UUID.randomUUID()}",
        name = cleanName,
        description = description.trim().takeIf { it.isNotBlank() },
        steps = steps
    )
}

private fun buildContinuousSteps(blocks: List<ContinuousBlockDraft>): List<WorkoutStep>? {
    return blocks.mapIndexed { index, block ->
        val pace = parsePace(block.paceMinutes, block.paceSeconds) ?: return null
        val distanceMeters = block.distanceKm.toDoubleOrNull()?.takeIf { it > 0.0 }?.times(1000.0) ?: return null
        val tolerance = block.toleranceSeconds.toIntOrNull()?.coerceAtLeast(0) ?: return null

        WorkoutStep(
            id = "continuous-${index + 1}-${UUID.randomUUID()}",
            name = block.name.trim().ifBlank { "Bloque ${index + 1}" },
            type = if (index == 0) StepType.EASY else StepType.TEMPO,
            targetType = TargetType.DISTANCE_METERS,
            targetValue = distanceMeters,
            targetPaceSecondsPerKm = pace,
            paceToleranceSeconds = tolerance
        )
    }
}

private fun buildIntervalSteps(draft: IntervalRoutineDraft): List<WorkoutStep>? {
    val repetitions = draft.repetitions.toIntOrNull()?.coerceIn(1, 20) ?: return null
    val intervalDistanceMeters = draft.intervalDistanceKm.toDoubleOrNull()
        ?.takeIf { it > 0.0 }
        ?.times(1000.0)
        ?: return null
    val intervalPace = parsePace(draft.intervalPaceMinutes, draft.intervalPaceSeconds) ?: return null
    val intervalTolerance = draft.intervalToleranceSeconds.toIntOrNull()?.coerceAtLeast(0) ?: return null
    val recoverySeconds = draft.recoveryMinutes.toDoubleOrNull()?.takeIf { it > 0.0 }?.times(60.0)
    val recoveryPace = parsePace(draft.recoveryPaceMinutes, draft.recoveryPaceSeconds) ?: return null
    val recoveryTolerance = draft.recoveryToleranceSeconds.toIntOrNull()?.coerceAtLeast(0) ?: return null

    return buildList {
        repeat(repetitions) { index ->
            val number = index + 1
            add(
                WorkoutStep(
                    id = "interval-$number-${UUID.randomUUID()}",
                    name = "Pasada $number",
                    type = StepType.INTERVAL,
                    targetType = TargetType.DISTANCE_METERS,
                    targetValue = intervalDistanceMeters,
                    targetPaceSecondsPerKm = intervalPace,
                    paceToleranceSeconds = intervalTolerance
                )
            )
            if (draft.hasRecovery && index < repetitions - 1) {
                add(
                    WorkoutStep(
                        id = "recovery-$number-${UUID.randomUUID()}",
                        name = "Descanso $number",
                        type = StepType.RECOVERY,
                        targetType = TargetType.TIME_SECONDS,
                        targetValue = recoverySeconds ?: return null,
                        targetPaceSecondsPerKm = recoveryPace,
                        paceToleranceSeconds = recoveryTolerance
                    )
                )
            }
        }
    }
}

private fun parsePace(minutes: String, seconds: String): Int? {
    val min = minutes.toIntOrNull() ?: return null
    val sec = seconds.toIntOrNull() ?: return null
    if (min < 0 || sec !in 0..59) {
        return null
    }
    return min * 60 + sec
}

private fun String.onlyDigits(): String {
    return filter { it.isDigit() }
}

private fun String.onlyDecimal(): String {
    return filterIndexed { index, char ->
        char.isDigit() || (char == '.' && !take(index).contains('.'))
    }
}

private fun String.incrementText(): String {
    return ((toIntOrNull() ?: 0) + 1).toString()
}

private fun String.decrementText(minimum: Int): String {
    return ((toIntOrNull() ?: minimum) - 1).coerceAtLeast(minimum).toString()
}
