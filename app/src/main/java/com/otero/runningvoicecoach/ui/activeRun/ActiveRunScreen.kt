package com.otero.runningvoicecoach.ui.activeRun

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.otero.runningvoicecoach.R
import com.otero.runningvoicecoach.data.session.RunHistoryRepository
import com.otero.runningvoicecoach.data.session.RunSessionSummary
import com.otero.runningvoicecoach.data.session.RunStepSummary
import com.otero.runningvoicecoach.data.settings.UserSettingsRepository
import com.otero.runningvoicecoach.data.workout.CustomWorkoutRepository
import com.otero.runningvoicecoach.domain.alert.AlertEngine
import com.otero.runningvoicecoach.domain.alert.AlertPriority
import com.otero.runningvoicecoach.domain.alert.LocalMessageProvider
import com.otero.runningvoicecoach.domain.model.PaceStatus
import com.otero.runningvoicecoach.domain.pace.PaceCalculator
import com.otero.runningvoicecoach.domain.workout.ExampleWorkouts
import com.otero.runningvoicecoach.domain.workout.WorkoutEngine
import com.otero.runningvoicecoach.domain.workout.WorkoutEngineState
import com.otero.runningvoicecoach.location.LocationTracker
import com.otero.runningvoicecoach.location.RunForegroundService
import com.otero.runningvoicecoach.openai.OpenAIClient
import com.otero.runningvoicecoach.openai.RunningAlertContext
import com.otero.runningvoicecoach.ui.components.AppScaffold
import com.otero.runningvoicecoach.voice.AndroidVoiceCoach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ActiveRunScreen(
    workoutPlanId: String?,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val customWorkoutRepository = remember { CustomWorkoutRepository(context.applicationContext) }
    val customWorkouts by customWorkoutRepository.workouts.collectAsState(initial = emptyList())
    val workoutPlan = remember(workoutPlanId, customWorkouts) {
        customWorkouts.firstOrNull { it.id == workoutPlanId } ?: ExampleWorkouts.findById(workoutPlanId)
    }
    val workoutEngine = remember { WorkoutEngine() }
    val alertEngine = remember { AlertEngine() }
    val voiceCoach = remember { AndroidVoiceCoach(context) }
    val locationTracker = remember { LocationTracker(context.applicationContext) }
    val historyRepository = remember { RunHistoryRepository(context.applicationContext) }
    val settingsRepository = remember { UserSettingsRepository(context.applicationContext) }
    val userSettings by settingsRepository.settings.collectAsState(
        initial = com.otero.runningvoicecoach.data.settings.UserSettings()
    )
    val openAIClient = remember(userSettings.developmentOpenAiApiKey) {
        OpenAIClient(apiKey = userSettings.developmentOpenAiApiKey.ifBlank { com.otero.runningvoicecoach.BuildConfig.OPENAI_API_KEY })
    }
    val coroutineScope = rememberCoroutineScope()
    val locationState by locationTracker.state.collectAsState()
    val latestLocationState by rememberUpdatedState(locationState)

    var useGpsMode by rememberSaveable { mutableStateOf(false) }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var isPaused by rememberSaveable { mutableStateOf(false) }
    var isFinished by rememberSaveable { mutableStateOf(false) }
    var currentStepIndex by rememberSaveable { mutableIntStateOf(0) }
    var totalDurationSeconds by rememberSaveable { mutableLongStateOf(0L) }
    var stepDurationSeconds by rememberSaveable { mutableLongStateOf(0L) }
    var totalDistanceMeters by rememberSaveable { mutableDoubleStateOf(0.0) }
    var stepDistanceMeters by rememberSaveable { mutableDoubleStateOf(0.0) }
    var lastGpsTotalDistanceMeters by rememberSaveable { mutableDoubleStateOf(0.0) }
    var currentPaceSecondsPerKm by rememberSaveable { mutableStateOf<Int?>(330) }
    var selectedBackgroundIndex by rememberSaveable { mutableIntStateOf(0) }
    val completedStepSummaries = remember { mutableStateListOf<RunStepSummary>() }
    var engineState by remember(workoutPlan.id) {
        mutableStateOf(
            workoutEngine.evaluate(
                workoutPlan = workoutPlan,
                currentStepIndex = currentStepIndex,
                totalDistanceMeters = totalDistanceMeters,
                totalDurationSeconds = totalDurationSeconds,
                stepDistanceMeters = stepDistanceMeters,
                stepDurationSeconds = stepDurationSeconds,
                currentPaceSecondsPerKm = currentPaceSecondsPerKm,
                paceToleranceSeconds = userSettings.generalPaceToleranceSeconds
            )
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locationTracker.reset()
            lastGpsTotalDistanceMeters = 0.0
            isRunning = true
            isPaused = false
            isFinished = false
            locationTracker.start()
            RunForegroundService.start(context)
        }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        // Notification permission is best-effort; the foreground service still owns location tracking.
    }

    DisposableEffect(Unit) {
        onDispose {
            locationTracker.stop()
            RunForegroundService.stop(context)
            voiceCoach.shutdown()
        }
    }

    LaunchedEffect(isRunning, isPaused, isFinished, currentStepIndex) {
        while (isRunning && !isPaused && !isFinished) {
            delay(1_000L)

            val nextPace = if (useGpsMode) {
                latestLocationState.currentPaceSecondsPerKm
            } else {
                simulatedPaceFor(totalDurationSeconds)
            }
            val metersThisSecond = if (useGpsMode) {
                val gpsDistance = latestLocationState.totalDistanceMeters
                val gpsDelta = (gpsDistance - lastGpsTotalDistanceMeters)
                    .coerceAtLeast(0.0)
                lastGpsTotalDistanceMeters = gpsDistance
                gpsDelta
            } else {
                METERS_PER_KILOMETER / (nextPace ?: DEFAULT_SIMULATED_PACE_SECONDS_PER_KM)
            }

            totalDurationSeconds += 1L
            stepDurationSeconds += 1L
            totalDistanceMeters += metersThisSecond
            stepDistanceMeters += metersThisSecond
            currentPaceSecondsPerKm = nextPace

            val nextState = workoutEngine.evaluate(
                workoutPlan = workoutPlan,
                currentStepIndex = currentStepIndex,
                totalDistanceMeters = totalDistanceMeters,
                totalDurationSeconds = totalDurationSeconds,
                stepDistanceMeters = stepDistanceMeters,
                stepDurationSeconds = stepDurationSeconds,
                currentPaceSecondsPerKm = currentPaceSecondsPerKm,
                paceToleranceSeconds = userSettings.generalPaceToleranceSeconds
            )
            engineState = nextState

            val alerts = alertEngine.evaluate(
                state = nextState,
                totalDistanceMeters = totalDistanceMeters,
                nowMillis = totalDurationSeconds * 1_000L,
                minPaceAlertIntervalMillisOverride = userSettings.minAlertIntervalSeconds * 1_000L
            )
            alerts.forEachIndexed { index, alert ->
                if (userSettings.voiceEnabled) {
                    coroutineScope.launch {
                        val message = if (userSettings.openAiEnabled) {
                            runCatching {
                                openAIClient.generateRunningMessage(
                                    RunningAlertContext.fromAlertEvent(
                                        alertEvent = alert,
                                        targetPaceSecondsPerKm = nextState.currentStep?.targetPaceSecondsPerKm,
                                        currentPaceSecondsPerKm = currentPaceSecondsPerKm
                                    )
                                )
                            }.getOrElse {
                                LocalMessageProvider.messageFor(alert)
                            }
                        } else {
                            LocalMessageProvider.messageFor(alert)
                        }

                        voiceCoach.speak(
                            message = message,
                            flush = index == 0 && alert.priority == AlertPriority.HIGH
                        )
                    }
                }
            }

            if (nextState.isWorkoutFinished) {
                appendStepSummaryIfNeeded(
                    summaries = completedStepSummaries,
                    state = nextState,
                    stepDistanceMeters = stepDistanceMeters,
                    stepDurationSeconds = stepDurationSeconds
                )
                historyRepository.saveSession(
                    RunSessionSummary(
                        id = UUID.randomUUID().toString(),
                        workoutName = workoutPlan.name,
                        finishedAtMillis = System.currentTimeMillis(),
                        totalDistanceMeters = totalDistanceMeters,
                        totalDurationSeconds = totalDurationSeconds,
                        averagePaceSecondsPerKm = PaceCalculator.calculateAveragePaceSecondsPerKm(
                            distanceMeters = totalDistanceMeters,
                            durationSeconds = totalDurationSeconds
                        ),
                        stepSummaries = completedStepSummaries.toList()
                    )
                )
                isFinished = true
                isRunning = false
            } else if (nextState.shouldMoveToNextStep) {
                appendStepSummaryIfNeeded(
                    summaries = completedStepSummaries,
                    state = nextState,
                    stepDistanceMeters = stepDistanceMeters,
                    stepDurationSeconds = stepDurationSeconds
                )
                currentStepIndex += 1
                stepDurationSeconds = 0L
                stepDistanceMeters = 0.0
            }
        }
    }

    AppScaffold(
        title = "Carrera activa",
        onBack = onBack
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Image(
                painter = painterResource(id = activityBackgrounds[selectedBackgroundIndex].resId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.34f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.72f))
            )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SourceModeRow(
                useGpsMode = useGpsMode,
                isRunning = isRunning,
                onUseGpsModeChange = { enabled ->
                    useGpsMode = enabled
                    locationTracker.stop()
                    locationTracker.reset()
                    lastGpsTotalDistanceMeters = 0.0
                }
            )
            ActivityBackgroundSelector(
                selectedIndex = selectedBackgroundIndex,
                onSelect = { selectedBackgroundIndex = it }
            )
            if (useGpsMode && locationState.lastError != null) {
                Text(
                    text = locationState.lastError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Tiempo",
                    value = formatDuration(totalDurationSeconds)
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Distancia",
                    value = formatDistance(totalDistanceMeters)
                )
            }

            MetricCard(
                label = "Ritmo actual",
                value = PaceCalculator.formatPace(currentPaceSecondsPerKm),
                large = true
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = engineState.currentStep?.name ?: "Finalizado",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    LinearProgressIndicator(
                        progress = { engineState.stepProgressPercent / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Progreso ${engineState.stepProgressPercent.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    MetricRow(
                        label = "Ritmo objetivo",
                        value = PaceCalculator.formatPace(engineState.currentStep?.targetPaceSecondsPerKm)
                    )
                    MetricRow(
                        label = "Diferencia",
                        value = formatPaceDifference(engineState.paceDifferenceSeconds)
                    )
                    MetricRow(label = "Estado", value = engineState.paceStatus.displayName())
                    MetricRow(
                        label = "Ritmo promedio",
                        value = PaceCalculator.formatPace(
                            PaceCalculator.calculateAveragePaceSecondsPerKm(
                                distanceMeters = totalDistanceMeters,
                                durationSeconds = totalDurationSeconds
                            )
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (isFinished) {
                            currentStepIndex = 0
                            totalDurationSeconds = 0L
                            stepDurationSeconds = 0L
                            totalDistanceMeters = 0.0
                            stepDistanceMeters = 0.0
                            lastGpsTotalDistanceMeters = 0.0
                            currentPaceSecondsPerKm = DEFAULT_SIMULATED_PACE_SECONDS_PER_KM
                            completedStepSummaries.clear()
                            alertEngine.reset()
                            locationTracker.reset()
                            isFinished = false
                        }
                        if (useGpsMode) {
                            if (locationTracker.hasLocationPermission()) {
                                locationTracker.start()
                                RunForegroundService.start(context)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                isRunning = true
                                isPaused = false
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        } else {
                            isRunning = true
                            isPaused = false
                        }
                    }
                ) {
                    Text(startButtonLabel(isRunning, isPaused, isFinished))
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (isRunning) {
                            isPaused = !isPaused
                            if (isPaused) {
                                voiceCoach.stop()
                                if (useGpsMode) {
                                    locationTracker.stop()
                                    RunForegroundService.stop(context)
                                }
                            } else if (useGpsMode && locationTracker.hasLocationPermission()) {
                                locationTracker.start()
                                RunForegroundService.start(context)
                            }
                        }
                    }
                ) {
                    Text(if (isPaused) "Reanudar" else "Pausar")
                }
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    voiceCoach.stop()
                    locationTracker.stop()
                    RunForegroundService.stop(context)
                    isRunning = false
                    isFinished = true
                    coroutineScope.launch {
                        appendStepSummaryIfNeeded(
                            summaries = completedStepSummaries,
                            state = engineState,
                            stepDistanceMeters = stepDistanceMeters,
                            stepDurationSeconds = stepDurationSeconds
                        )
                        historyRepository.saveSession(
                            RunSessionSummary(
                                id = UUID.randomUUID().toString(),
                                workoutName = workoutPlan.name,
                                finishedAtMillis = System.currentTimeMillis(),
                                totalDistanceMeters = totalDistanceMeters,
                                totalDurationSeconds = totalDurationSeconds,
                                averagePaceSecondsPerKm = PaceCalculator.calculateAveragePaceSecondsPerKm(
                                    distanceMeters = totalDistanceMeters,
                                    durationSeconds = totalDurationSeconds
                                ),
                                stepSummaries = completedStepSummaries.toList()
                            )
                        )
                        onFinish()
                    }
                }
            ) {
                Text("Finalizar")
            }
        }
        }
    }
}

@Composable
private fun ActivityBackgroundSelector(
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Fondo de actividad", style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            activityBackgrounds.forEachIndexed { index, background ->
                val borderColor = if (index == selectedIndex) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Transparent
                }
                Image(
                    painter = painterResource(id = background.resId),
                    contentDescription = background.label,
                    modifier = Modifier
                        .size(width = 82.dp, height = 54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable { onSelect(index) },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun SourceModeRow(
    useGpsMode: Boolean,
    isRunning: Boolean,
    onUseGpsModeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "Fuente de datos", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (useGpsMode) "GPS real" else "Simulador",
                style = MaterialTheme.typography.titleMedium
            )
        }
        Switch(
            checked = useGpsMode,
            onCheckedChange = onUseGpsModeChange,
            enabled = !isRunning
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = value,
                style = if (large) {
                    MaterialTheme.typography.displaySmall
                } else {
                    MaterialTheme.typography.headlineMedium
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

private fun simulatedPaceFor(totalDurationSeconds: Long): Int {
    return when ((totalDurationSeconds / 30L) % 3L) {
        0L -> 420
        1L -> 360
        else -> 480
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}

private fun formatDistance(distanceMeters: Double): String {
    return "%.2f km".format(distanceMeters / METERS_PER_KILOMETER)
}

private fun formatPaceDifference(differenceSeconds: Int?): String {
    if (differenceSeconds == null) {
        return "Sin objetivo"
    }

    return when {
        differenceSeconds < 0 -> "${-differenceSeconds}s rapido"
        differenceSeconds > 0 -> "${differenceSeconds}s lento"
        else -> "En objetivo"
    }
}

private fun startButtonLabel(
    isRunning: Boolean,
    isPaused: Boolean,
    isFinished: Boolean
): String {
    return when {
        isFinished -> "Reiniciar"
        isRunning && !isPaused -> "Corriendo"
        isPaused -> "Reanudar"
        else -> "Iniciar"
    }
}

private fun PaceStatus.displayName(): String {
    return when (this) {
        PaceStatus.WITHIN_TARGET -> "En objetivo"
        PaceStatus.TOO_FAST -> "Rapido"
        PaceStatus.TOO_SLOW -> "Lento"
        PaceStatus.NO_TARGET -> "Sin objetivo"
    }
}

private fun appendStepSummaryIfNeeded(
    summaries: MutableList<RunStepSummary>,
    state: WorkoutEngineState,
    stepDistanceMeters: Double,
    stepDurationSeconds: Long
) {
    val step = state.currentStep ?: return
    if (stepDurationSeconds <= 0L && stepDistanceMeters <= 0.0) {
        return
    }
    if (summaries.size > state.currentStepIndex) {
        return
    }

    val averagePace = PaceCalculator.calculateAveragePaceSecondsPerKm(
        distanceMeters = stepDistanceMeters,
        durationSeconds = stepDurationSeconds
    )

    summaries.add(
        RunStepSummary(
            stepName = step.name,
            distanceMeters = stepDistanceMeters,
            durationSeconds = stepDurationSeconds,
            averagePaceSecondsPerKm = averagePace,
            paceStatus = PaceCalculator.comparePace(
                current = averagePace,
                target = step.targetPaceSecondsPerKm,
                tolerance = step.paceToleranceSeconds
            )
        )
    )
}

private data class ActivityBackground(
    val label: String,
    val resId: Int
)

private val activityBackgrounds = listOf(
    ActivityBackground("Fondo 1", R.drawable.fondo1),
    ActivityBackground("Fondo 2", R.drawable.fondo2),
    ActivityBackground("Fondo 3", R.drawable.fondo3),
    ActivityBackground("Fondo 4", R.drawable.fondo4),
    ActivityBackground("Fondo 5", R.drawable.fondo5),
    ActivityBackground("Fondo 6", R.drawable.fondo6)
)

private const val METERS_PER_KILOMETER = 1000.0
private const val DEFAULT_SIMULATED_PACE_SECONDS_PER_KM = 420
