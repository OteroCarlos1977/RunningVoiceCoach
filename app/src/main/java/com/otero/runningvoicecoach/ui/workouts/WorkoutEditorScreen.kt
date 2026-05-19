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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var routineMode by remember { mutableStateOf(RoutineMode.COACH_PLAN) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTemplateId by remember { mutableStateOf<String?>(null) }
    var coachBlockCount by remember { mutableIntStateOf(3) }
    var continuousBlockCount by remember { mutableIntStateOf(2) }
    val continuousBlocks = remember {
        mutableStateListOf(
            ContinuousBlockDraft(name = "Fondo suave", distanceKm = "4", paceMinutes = "7", paceSeconds = "30"),
            ContinuousBlockDraft(name = "Ritmo medio", distanceKm = "2", paceMinutes = "6", paceSeconds = "30")
        )
    }
    var intervalDraft by remember { mutableStateOf(IntervalRoutineDraft()) }
    val coachPlanSteps = remember {
        mutableStateListOf<GuidedStepDraft>().apply {
            repeat(coachBlockCount) { index ->
                add(GuidedStepDraft(name = "Bloque ${index + 1}"))
            }
        }
    }

    fun applyTemplate(template: TrainingTemplateDraft) {
        selectedTemplateId = template.id
        name = template.name
        description = template.description
        routineMode = RoutineMode.COACH_PLAN
        coachBlockCount = template.steps.size
        coachPlanSteps.clear()
        coachPlanSteps.addAll(template.steps)
    }

    fun syncCoachPlanSteps(count: Int) {
        val safeCount = count.coerceIn(1, 30)
        selectedTemplateId = null
        coachBlockCount = safeCount
        while (coachPlanSteps.size < safeCount) {
            coachPlanSteps += GuidedStepDraft(name = "Bloque ${coachPlanSteps.size + 1}")
        }
        while (coachPlanSteps.size > safeCount) {
            coachPlanSteps.removeAt(coachPlanSteps.lastIndex)
        }
    }

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
                SectionCard {
                    RoutineModeDropdown(
                        selected = routineMode,
                        onChange = { routineMode = it }
                    )
                }

                when (routineMode) {
                    RoutineMode.COACH_PLAN -> CoachPlanEditor(
                        selectedTemplateId = selectedTemplateId,
                        templates = trainingTemplates,
                        blockCount = coachBlockCount,
                        steps = coachPlanSteps,
                        onApplyTemplate = ::applyTemplate,
                        onBlockCountChange = ::syncCoachPlanSteps,
                        onStepChange = { index, step -> coachPlanSteps[index] = step },
                        onAddStep = { syncCoachPlanSteps(coachBlockCount + 1) },
                        onRemoveStep = { index ->
                            if (coachPlanSteps.size > 1) {
                                coachPlanSteps.removeAt(index)
                                coachBlockCount = coachPlanSteps.size
                                selectedTemplateId = null
                            }
                        }
                    )

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
                    coachPlanSteps = coachPlanSteps,
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
                            coachPlanSteps = coachPlanSteps,
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
private fun CoachPlanEditor(
    selectedTemplateId: String?,
    templates: List<TrainingTemplateDraft>,
    blockCount: Int,
    steps: List<GuidedStepDraft>,
    onApplyTemplate: (TrainingTemplateDraft) -> Unit,
    onBlockCountChange: (Int) -> Unit,
    onStepChange: (Int, GuidedStepDraft) -> Unit,
    onAddStep: () -> Unit,
    onRemoveStep: (Int) -> Unit
) {
    SectionCard {
        Text(
            text = "Plantillas de entrenamiento",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Carga una base del entrenador y ajusta cada bloque antes de guardar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
        )
        TemplateDropdown(
            selectedTemplateId = selectedTemplateId,
            templates = templates,
            onApplyTemplate = onApplyTemplate
        )
        StepperRow(
            label = "Cantidad de bloques a editar",
            value = blockCount.toString(),
            onDecrease = { onBlockCountChange(blockCount - 1) },
            onIncrease = { onBlockCountChange(blockCount + 1) }
        )
    }

    steps.forEachIndexed { index, step ->
        GuidedStepCard(
            index = index,
            step = step,
            canRemove = steps.size > 1,
            onChange = { onStepChange(index, it) },
            onRemove = { onRemoveStep(index) }
        )
    }

    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        onClick = onAddStep
    ) {
        Text("Agregar bloque")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateDropdown(
    selectedTemplateId: String?,
    templates: List<TrainingTemplateDraft>,
    onApplyTemplate: (TrainingTemplateDraft) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = templates.firstOrNull { it.id == selectedTemplateId }?.name ?: "Elegir plantilla"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Plantilla") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            templates.forEach { template ->
                DropdownMenuItem(
                    text = { Text(template.name, fontSize = 14.sp) },
                    onClick = {
                        expanded = false
                        onApplyTemplate(template)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutineModeDropdown(
    selected: RoutineMode,
    onChange: (RoutineMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Tipo de rutina") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            RoutineMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(mode.label, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(mode.help, fontSize = 12.sp, color = EditorMuted)
                        }
                    },
                    onClick = {
                        expanded = false
                        onChange(mode)
                    }
                )
            }
        }
    }
    Text(
        text = selected.help,
        color = EditorMuted,
        fontSize = 13.sp,
        lineHeight = 17.sp
    )
}

@Composable
private fun GuidedStepCard(
    index: Int,
    step: GuidedStepDraft,
    canRemove: Boolean,
    onChange: (GuidedStepDraft) -> Unit,
    onRemove: () -> Unit
) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bloque ${index + 1}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (canRemove) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFFFECE6),
                    onClick = onRemove
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        text = "Quitar",
                        color = Color(0xFFB93815),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = step.name,
            onValueChange = { onChange(step.copy(name = it)) },
            singleLine = true,
            label = { Text("Nombre del bloque") }
        )
        TargetTypeDropdown(
            selected = step.targetType,
            onChange = { targetType ->
                val fallback = if (targetType == TargetType.TIME_SECONDS) "5" else "1"
                onChange(step.copy(targetType = targetType, targetValue = step.targetValue.ifBlank { fallback }))
            }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = step.targetValue,
            onValueChange = {
                val value = if (step.targetType == TargetType.TIME_SECONDS) it.onlyDigits() else it.onlyDecimal()
                onChange(step.copy(targetValue = value))
            },
            singleLine = true,
            label = { Text(if (step.targetType == TargetType.TIME_SECONDS) "Duracion en minutos" else "Distancia en km") },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (step.targetType == TargetType.TIME_SECONDS) KeyboardType.Number else KeyboardType.Decimal
            )
        )
        IntensityDropdown(
            selected = step.type,
            onChange = { onChange(step.copy(type = it)) }
        )
        PaceFields(
            paceMinutes = step.paceMinutes,
            paceSeconds = step.paceSeconds,
            toleranceSeconds = step.toleranceSeconds,
            onPaceMinutesChange = { onChange(step.copy(paceMinutes = it.onlyDigits())) },
            onPaceSecondsChange = { onChange(step.copy(paceSeconds = it.onlyDigits().take(2))) },
            onToleranceChange = { onChange(step.copy(toleranceSeconds = it.onlyDigits())) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TargetTypeDropdown(
    selected: TargetType,
    onChange: (TargetType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = if (selected == TargetType.TIME_SECONDS) "Tiempo" else "Distancia"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Tipo de bloque") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(TargetType.TIME_SECONDS, TargetType.DISTANCE_METERS).forEach { type ->
                DropdownMenuItem(
                    text = { Text(if (type == TargetType.TIME_SECONDS) "Tiempo" else "Distancia") },
                    onClick = {
                        expanded = false
                        onChange(type)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntensityDropdown(
    selected: StepType,
    onChange: (StepType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        "Suave" to StepType.EASY,
        "Medio" to StepType.TEMPO,
        "Fuerte" to StepType.INTERVAL,
        "Descanso" to StepType.RECOVERY
    )
    val selectedText = when (selected) {
        StepType.INTERVAL -> "Fuerte"
        StepType.TEMPO -> "Medio"
        StepType.RECOVERY -> "Descanso"
        StepType.COOLDOWN -> "Suave"
        StepType.WARMUP -> "Suave"
        StepType.EASY -> "Suave"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Intensidad") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.first) },
                    onClick = {
                        expanded = false
                        onChange(option.second)
                    }
                )
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
    coachPlanSteps: List<GuidedStepDraft>,
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
            RoutineMode.COACH_PLAN -> coachPlanSteps.mapIndexed { index, step ->
                val unit = if (step.targetType == TargetType.TIME_SECONDS) "min" else "km"
                "${index + 1}. ${step.name.ifBlank { "Bloque ${index + 1}" }} - ${step.targetValue.ifBlank { "0" }} $unit a ${step.paceMinutes}:${step.paceSeconds.padStart(2, '0')}"
            }

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
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                onClick = onDecrease
            ) {
                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                onClick = onIncrease
            ) {
                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = colors,
            onClick = onClick
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                lineHeight = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    } else {
        OutlinedButton(
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(14.dp),
            onClick = onClick
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                lineHeight = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
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

private enum class RoutineMode(
    val label: String,
    val help: String
) {
    COACH_PLAN(
        label = "Plan del entrenador",
        help = "Usa una plantilla o arma bloques mixtos por tiempo y distancia."
    ),
    CONTINUOUS(
        label = "Bloques continuos",
        help = "Ideal para fondos: por ejemplo 4 km suave + 2 km medio."
    ),
    INTERVALS(
        label = "Pasadas con descanso",
        help = "Ideal para series repetidas con pausa entre cada pasada."
    )
}

private data class TrainingTemplateDraft(
    val id: String,
    val name: String,
    val description: String,
    val steps: List<GuidedStepDraft>
)

private data class GuidedStepDraft(
    val name: String = "Bloque",
    val targetType: TargetType = TargetType.TIME_SECONDS,
    val targetValue: String = "5",
    val paceMinutes: String = "7",
    val paceSeconds: String = "00",
    val toleranceSeconds: String = "42",
    val type: StepType = StepType.EASY
)

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
    coachPlanSteps: List<GuidedStepDraft>,
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
        RoutineMode.COACH_PLAN -> buildCoachPlanSteps(coachPlanSteps)
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

private fun buildCoachPlanSteps(steps: List<GuidedStepDraft>): List<WorkoutStep>? {
    return steps.mapIndexed { index, step ->
        val pace = parsePace(step.paceMinutes, step.paceSeconds) ?: return null
        val tolerance = step.toleranceSeconds.toIntOrNull()?.coerceAtLeast(0) ?: return null
        val targetValue = when (step.targetType) {
            TargetType.TIME_SECONDS -> step.targetValue.toDoubleOrNull()
                ?.takeIf { it > 0.0 }
                ?.times(60.0)
                ?: return null

            TargetType.DISTANCE_METERS -> step.targetValue.toDoubleOrNull()
                ?.takeIf { it > 0.0 }
                ?.times(1000.0)
                ?: return null
        }

        WorkoutStep(
            id = "coach-${index + 1}-${UUID.randomUUID()}",
            name = step.name.trim().ifBlank { "Bloque ${index + 1}" },
            type = step.type,
            targetType = step.targetType,
            targetValue = targetValue,
            targetPaceSecondsPerKm = pace,
            paceToleranceSeconds = tolerance
        )
    }
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

private val trainingTemplates = listOf(
    TrainingTemplateDraft(
        id = "progresivo-10k",
        name = "Progresivo base tipo 10K",
        description = "Mejorar ritmo con bloques progresivos y cierre tecnico.",
        steps = buildList {
            add(timeDraft("10 min ritmo 1", 10, 7, 30, StepType.EASY))
            repeat(4) { index ->
                add(timeDraft("Bloque ${index + 1}: progresivo ritmo 1 a 3", 3, 6, 30, StepType.TEMPO))
                add(timeDraft("Regenerativo ${index + 1}", 1, 8, 0, StepType.RECOVERY))
            }
            add(timeDraft("10 min ritmo 1", 10, 7, 30, StepType.EASY))
            repeat(5) { index ->
                add(distanceDraft("100 m ritmo carrera ${index + 1}", 0.1, 5, 30, StepType.INTERVAL))
                add(distanceDraft("100 m regenerativo ${index + 1}", 0.1, 8, 0, StepType.RECOVERY))
            }
            repeat(5) { index -> add(distanceDraft("Progresivo 50 m ${index + 1}", 0.05, 5, 45, StepType.TEMPO)) }
        }
    ),
    TrainingTemplateDraft(
        id = "fondo-progresivo-10k",
        name = "Fondo progresivo para 10K",
        description = "Ganar resistencia y terminar mas fuerte.",
        steps = listOf(
            timeDraft("15 min ritmo 1", 15, 7, 30, StepType.EASY),
            timeDraft("20 min ritmo 1 estable", 20, 7, 20, StepType.EASY),
            timeDraft("10 min ritmo 2", 10, 6, 30, StepType.TEMPO),
            timeDraft("5 min ritmo 3 controlado", 5, 5, 45, StepType.TEMPO),
            timeDraft("5 min regenerativo", 5, 8, 0, StepType.RECOVERY),
            distanceDraft("4 progresivos de 80 m", 0.32, 5, 45, StepType.TEMPO)
        )
    ),
    TrainingTemplateDraft(
        id = "fondo-base-21k",
        name = "Fondo largo base para 21K",
        description = "Construir resistencia aerobica.",
        steps = listOf(
            timeDraft("15 min ritmo 1", 15, 7, 30, StepType.EASY),
            timeDraft("40 min ritmo 1 comodo", 40, 7, 30, StepType.EASY),
            timeDraft("10 min ritmo 2", 10, 6, 30, StepType.TEMPO),
            timeDraft("5 min ritmo 1", 5, 7, 30, StepType.EASY),
            distanceDraft("4 progresivos de 100 m", 0.4, 5, 45, StepType.TEMPO)
        )
    ),
    TrainingTemplateDraft(
        id = "fondo-42k",
        name = "Fondo largo para 42K",
        description = "Volumen para maraton sin buscar velocidad excesiva.",
        steps = listOf(
            timeDraft("15 min ritmo 1", 15, 7, 30, StepType.EASY),
            timeDraft("75 min ritmo 1", 75, 7, 30, StepType.EASY),
            timeDraft("15 min ritmo 2 suave", 15, 6, 45, StepType.TEMPO),
            timeDraft("10 min ritmo 1", 10, 7, 30, StepType.EASY),
            timeDraft("5 min caminata final", 5, 10, 0, StepType.COOLDOWN)
        )
    ),
    TrainingTemplateDraft(
        id = "intervalos-10k",
        name = "Intervalos cortos velocidad 10K",
        description = "Mejorar economia de carrera.",
        steps = buildList {
            add(timeDraft("12 min ritmo 1", 12, 7, 30, StepType.EASY))
            repeat(6) { index ->
                add(timeDraft("Bloque fuerte ${index + 1}", 2, 5, 45, StepType.INTERVAL))
                add(timeDraft("Regenerativo ${index + 1}", 1, 8, 0, StepType.RECOVERY))
            }
            add(timeDraft("8 min ritmo 1", 8, 7, 30, StepType.EASY))
            repeat(6) { index ->
                add(distanceDraft("100 m rapido ${index + 1}", 0.1, 5, 15, StepType.INTERVAL))
                add(distanceDraft("100 m suave ${index + 1}", 0.1, 8, 0, StepType.RECOVERY))
            }
        }
    ),
    TrainingTemplateDraft(
        id = "bloques-progresivos-largos",
        name = "Bloques progresivos largos",
        description = "Progresivos sostenidos para 10K y 21K.",
        steps = buildList {
            add(timeDraft("10 min ritmo 1", 10, 7, 30, StepType.EASY))
            repeat(3) { index ->
                add(timeDraft("Progresivo largo ${index + 1}", 6, 6, 30, StepType.TEMPO))
                add(timeDraft("Regenerativo ${index + 1}", 2, 8, 0, StepType.RECOVERY))
            }
            add(timeDraft("10 min ritmo 1", 10, 7, 30, StepType.EASY))
            repeat(5) { index -> add(distanceDraft("Progresivo 60 m ${index + 1}", 0.06, 5, 45, StepType.TEMPO)) }
        }
    ),
    TrainingTemplateDraft(
        id = "tempo-10k",
        name = "Tempo controlado para 10K",
        description = "Correr alegre sin ir al maximo.",
        steps = buildList {
            add(timeDraft("12 min ritmo 1", 12, 7, 30, StepType.EASY))
            add(timeDraft("20 min ritmo 2/3 controlado", 20, 6, 10, StepType.TEMPO))
            add(timeDraft("8 min ritmo 1", 8, 7, 30, StepType.EASY))
            repeat(4) { index ->
                add(distanceDraft("100 m ritmo carrera ${index + 1}", 0.1, 5, 30, StepType.INTERVAL))
                add(distanceDraft("100 m regenerativo ${index + 1}", 0.1, 8, 0, StepType.RECOVERY))
            }
        }
    ),
    TrainingTemplateDraft(
        id = "tempo-partido-21k",
        name = "Tempo partido para 21K",
        description = "Tempo tolerable para media maraton.",
        steps = buildList {
            add(timeDraft("15 min ritmo 1", 15, 7, 30, StepType.EASY))
            repeat(3) { index ->
                add(timeDraft("Tempo 21K ${index + 1}", 10, 6, 15, StepType.TEMPO))
                add(timeDraft("Ritmo 1 ${index + 1}", 3, 7, 30, StepType.RECOVERY))
            }
            add(timeDraft("10 min regenerativo", 10, 8, 0, StepType.RECOVERY))
            distanceDraft("4 progresivos de 80 m", 0.32, 5, 45, StepType.TEMPO).also { add(it) }
        }
    ),
    TrainingTemplateDraft(
        id = "fondo-final-fuerte",
        name = "Fondo con final fuerte",
        description = "Resistencia y capacidad de remate.",
        steps = listOf(
            timeDraft("20 min ritmo 1", 20, 7, 30, StepType.EASY),
            timeDraft("25 min ritmo 1 estable", 25, 7, 20, StepType.EASY),
            timeDraft("15 min ritmo 2", 15, 6, 30, StepType.TEMPO),
            timeDraft("5 min ritmo 3 controlado", 5, 5, 45, StepType.TEMPO),
            timeDraft("10 min ritmo 1", 10, 7, 30, StepType.EASY)
        )
    ),
    TrainingTemplateDraft(
        id = "fartlek-piramidal",
        name = "Fartlek piramidal",
        description = "Entrenamiento variado para 10K, 21K y mejora general.",
        steps = buildList {
            add(timeDraft("12 min ritmo 1", 12, 7, 30, StepType.EASY))
            listOf(1 to 1, 2 to 1, 3 to 2, 4 to 2, 3 to 2, 2 to 1, 1 to 1).forEachIndexed { index, block ->
                add(timeDraft("Fuerte ${index + 1}", block.first, 5, 45, StepType.INTERVAL))
                add(timeDraft("Suave ${index + 1}", block.second, 8, 0, StepType.RECOVERY))
            }
            add(timeDraft("10 min ritmo 1", 10, 7, 30, StepType.EASY))
            repeat(4) { index -> add(distanceDraft("Progresivo 50 m ${index + 1}", 0.05, 5, 45, StepType.TEMPO)) }
        }
    ),
    TrainingTemplateDraft(
        id = "fondo-regenerativo",
        name = "Fondo regenerativo",
        description = "Sumar kilometros sin cargar demasiado.",
        steps = listOf(
            timeDraft("40 min ritmo 1 muy comodo", 40, 7, 45, StepType.EASY),
            timeDraft("5 min caminata rapida", 5, 10, 0, StepType.RECOVERY),
            distanceDraft("5 progresivos de 50 m suaves", 0.25, 6, 0, StepType.TEMPO)
        )
    ),
    TrainingTemplateDraft(
        id = "maraton-especifico",
        name = "Fondo especifico para maraton",
        description = "Preparacion exigente por duracion para 42K.",
        steps = buildList {
            add(timeDraft("20 min ritmo 1", 20, 7, 30, StepType.EASY))
            add(timeDraft("40 min ritmo 1 estable", 40, 7, 20, StepType.EASY))
            repeat(3) { index ->
                add(timeDraft("Ritmo maraton ${index + 1}", 15, 6, 45, StepType.TEMPO))
                add(timeDraft("Ritmo 1 ${index + 1}", 5, 7, 30, StepType.RECOVERY))
            }
            add(timeDraft("10 min ritmo 1 final", 10, 7, 30, StepType.COOLDOWN))
        }
    )
)

private fun timeDraft(name: String, minutes: Int, paceMinutes: Int, paceSeconds: Int, type: StepType): GuidedStepDraft {
    return GuidedStepDraft(
        name = name,
        targetType = TargetType.TIME_SECONDS,
        targetValue = minutes.toString(),
        paceMinutes = paceMinutes.toString(),
        paceSeconds = paceSeconds.toString().padStart(2, '0'),
        toleranceSeconds = if (type == StepType.RECOVERY || type == StepType.COOLDOWN) "60" else "35",
        type = type
    )
}

private fun distanceDraft(name: String, kilometers: Double, paceMinutes: Int, paceSeconds: Int, type: StepType): GuidedStepDraft {
    return GuidedStepDraft(
        name = name,
        targetType = TargetType.DISTANCE_METERS,
        targetValue = kilometers.toString(),
        paceMinutes = paceMinutes.toString(),
        paceSeconds = paceSeconds.toString().padStart(2, '0'),
        toleranceSeconds = if (type == StepType.RECOVERY || type == StepType.COOLDOWN) "60" else "30",
        type = type
    )
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
