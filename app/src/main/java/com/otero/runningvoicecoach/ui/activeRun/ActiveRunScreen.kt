package com.otero.runningvoicecoach.ui.activeRun

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.otero.runningvoicecoach.data.session.RunHistoryRepository
import com.otero.runningvoicecoach.data.session.RunSessionSummary
import com.otero.runningvoicecoach.data.settings.UserSettingsRepository
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
    val workoutPlan = remember(workoutPlanId) { ExampleWorkouts.findById(workoutPlanId) }
    val workoutEngine = remember { WorkoutEngine() }
    val alertEngine = remember { AlertEngine() }
    val voiceCoach = remember { AndroidVoiceCoach(context) }
    val locationTracker = remember { LocationTracker(context.applicationContext) }
    val historyRepository = remember { RunHistoryRepository(context.applicationContext) }
    val settingsRepository = remember { UserSettingsRepository(context.applicationContext) }
    val userSettings by settingsRepository.settings.collectAsState(
        initial = com.otero.runningvoicecoach.data.settings.UserSettings()
    )
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
    var engineState by remember {
        mutableStateOf(
            workoutEngine.evaluate(
                workoutPlan = workoutPlan,
                currentStepIndex = currentStepIndex,
                totalDistanceMeters = totalDistanceMeters,
                totalDurationSeconds = totalDurationSeconds,
                stepDistanceMeters = stepDistanceMeters,
                stepDurationSeconds = stepDurationSeconds,
                currentPaceSecondsPerKm = currentPaceSecondsPerKm
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
                currentPaceSecondsPerKm = currentPaceSecondsPerKm
            )
            engineState = nextState

            val alerts = alertEngine.evaluate(
                state = nextState,
                totalDistanceMeters = totalDistanceMeters,
                nowMillis = totalDurationSeconds * 1_000L
            )
            alerts.forEachIndexed { index, alert ->
                if (userSettings.voiceEnabled) {
                    voiceCoach.speak(
                        message = LocalMessageProvider.messageFor(alert),
                        flush = index == 0 && alert.priority == AlertPriority.HIGH
                    )
                }
            }

            if (nextState.isWorkoutFinished) {
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
                        )
                    )
                )
                isFinished = true
                isRunning = false
            } else if (nextState.shouldMoveToNextStep) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                                )
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

private const val METERS_PER_KILOMETER = 1000.0
private const val DEFAULT_SIMULATED_PACE_SECONDS_PER_KM = 420
