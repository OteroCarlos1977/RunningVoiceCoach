# PLAN PARA CODEX — App Android “RunningVoiceCoach”

## 0. Objetivo general

Crear una app Android nativa llamada **RunningVoiceCoach** que funcione como asistente de voz para running.

La app debe:

1. Leer GPS desde el teléfono.
2. Calcular distancia, tiempo, ritmo actual y ritmo promedio.
3. Permitir cargar entrenamientos estructurados por bloques.
4. Comparar el ritmo real contra el ritmo objetivo de cada bloque.
5. Emitir avisos de voz por auriculares.
6. Usar OpenAI solo para redactar mensajes breves y naturales.
7. Usar mensajes locales si falla la conexión o la API.
8. Guardar el resumen final de la carrera.

La app NO debe inventar datos deportivos. Los cálculos deben hacerse localmente.

---

## 1. Alcance de la primera versión MVP

### MVP 1 — Sin IA

Primero se debe construir una versión funcional sin OpenAI.

Debe incluir:

- Proyecto Android en Kotlin.
- Jetpack Compose.
- Navegación básica.
- Modelos de entrenamiento.
- Rutinas estructuradas.
- GPS.
- Cálculo de ritmo.
- Motor de bloques.
- Alertas por voz usando Android TextToSpeech.
- Pantalla de carrera activa.
- Resumen final.

Los mensajes de voz iniciales pueden ser fijos:

```text
Buen ritmo.
Vas más rápido que el objetivo.
Vas más lento que el objetivo.
Terminó el bloque. Pasá al siguiente.
Faltan 200 metros para terminar este bloque.
Completaste un kilómetro.
```

### MVP 2 — Con OpenAI

Luego se agrega OpenAI para convertir alertas técnicas en mensajes más naturales.

Ejemplo:

```text
Vas un poco rápido. Aflojá apenas y sostené controlado hasta terminar el bloque.
```

### MVP 3 — Conversación avanzada

Más adelante se podrá agregar modo conversación:

```text
Usuario: ¿Cómo voy?
App: Vas bien. Estás dentro del margen objetivo del bloque actual.
```

---

## 2. Stack técnico recomendado

Usar:

- Kotlin.
- Android nativo.
- Jetpack Compose.
- FusedLocationProviderClient para ubicación.
- Foreground Service para carrera activa con pantalla apagada.
- Android TextToSpeech para voz local.
- OkHttp para conexión con OpenAI.
- Kotlin Coroutines.
- Kotlin Flow.
- DataStore o Room para persistencia local.

Para la primera versión, priorizar **estabilidad, bajo consumo y simplicidad**.

---

## 3. Arquitectura general

```text
RunningVoiceCoach
│
├── app
│   ├── MainActivity.kt
│   ├── navigation
│   └── di
│
├── domain
│   ├── model
│   ├── workout
│   ├── pace
│   └── alert
│
├── data
│   ├── workout
│   ├── session
│   └── settings
│
├── location
│   ├── LocationModule
│   ├── LocationTracker
│   └── RunForegroundService
│
├── voice
│   ├── VoiceCoach
│   └── LocalMessageProvider
│
├── openai
│   ├── OpenAIClient
│   ├── RunningAlertContext
│   └── OpenAIMessageMapper
│
└── ui
    ├── home
    ├── workouts
    ├── activeRun
    ├── summary
    └── settings
```

---

## 4. Modelos de dominio

Crear estos modelos:

```kotlin
data class WorkoutPlan(
    val id: String,
    val name: String,
    val description: String? = null,
    val steps: List<WorkoutStep>
)

data class WorkoutStep(
    val id: String,
    val name: String,
    val type: StepType,
    val targetType: TargetType,
    val targetValue: Double,
    val targetPaceSecondsPerKm: Int?,
    val paceToleranceSeconds: Int = 15
)

enum class StepType {
    WARMUP,
    EASY,
    INTERVAL,
    RECOVERY,
    TEMPO,
    COOLDOWN
}

enum class TargetType {
    TIME_SECONDS,
    DISTANCE_METERS
}
```

Crear también:

```kotlin
data class RunSession(
    val id: String,
    val workoutPlanId: String?,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val totalDistanceMeters: Double,
    val totalDurationSeconds: Long,
    val averagePaceSecondsPerKm: Int?,
    val stepResults: List<RunStepResult>
)

data class RunStepResult(
    val stepId: String,
    val stepName: String,
    val distanceMeters: Double,
    val durationSeconds: Long,
    val averagePaceSecondsPerKm: Int?,
    val targetPaceSecondsPerKm: Int?,
    val complianceStatus: PaceStatus
)

enum class PaceStatus {
    WITHIN_TARGET,
    TOO_FAST,
    TOO_SLOW,
    NO_TARGET
}
```

---

## 5. Formato de rutina de ejemplo

Crear una rutina de ejemplo dentro del repositorio local:

```json
{
  "name": "Intervalos 5 x 1000",
  "description": "Entrenamiento de intervalos para 10K",
  "steps": [
    {
      "name": "Entrada en calor",
      "type": "WARMUP",
      "targetType": "TIME_SECONDS",
      "targetValue": 600,
      "targetPaceSecondsPerKm": null,
      "paceToleranceSeconds": 20
    },
    {
      "name": "Intervalo 1",
      "type": "INTERVAL",
      "targetType": "DISTANCE_METERS",
      "targetValue": 1000,
      "targetPaceSecondsPerKm": 330,
      "paceToleranceSeconds": 10
    },
    {
      "name": "Recuperación 1",
      "type": "RECOVERY",
      "targetType": "TIME_SECONDS",
      "targetValue": 120,
      "targetPaceSecondsPerKm": 420,
      "paceToleranceSeconds": 25
    },
    {
      "name": "Vuelta a la calma",
      "type": "COOLDOWN",
      "targetType": "TIME_SECONDS",
      "targetValue": 600,
      "targetPaceSecondsPerKm": null,
      "paceToleranceSeconds": 20
    }
  ]
}
```

Aclaración:

- 330 segundos por km = 5:30 min/km.
- 420 segundos por km = 7:00 min/km.

---

## 6. Motor de entrenamiento

Crear `WorkoutEngine`.

Debe recibir:

- `WorkoutPlan`.
- Distancia total recorrida.
- Tiempo total transcurrido.
- Ritmo actual.
- Distancia recorrida dentro del bloque.
- Tiempo transcurrido dentro del bloque.

Debe devolver:

```kotlin
data class WorkoutEngineState(
    val currentStep: WorkoutStep?,
    val currentStepIndex: Int,
    val totalSteps: Int,
    val stepProgressPercent: Float,
    val remainingDistanceMeters: Double?,
    val remainingTimeSeconds: Long?,
    val paceStatus: PaceStatus,
    val paceDifferenceSeconds: Int?,
    val shouldMoveToNextStep: Boolean,
    val isWorkoutFinished: Boolean
)
```

Reglas:

1. Si el bloque es por tiempo, termina cuando se alcanza `targetValue` en segundos.
2. Si el bloque es por distancia, termina cuando se alcanza `targetValue` en metros.
3. Si el bloque no tiene ritmo objetivo, `paceStatus = NO_TARGET`.
4. Si el ritmo actual está dentro de la tolerancia, `paceStatus = WITHIN_TARGET`.
5. Si el ritmo actual es menor en segundos por km que el objetivo, el corredor va más rápido.
6. Si el ritmo actual es mayor en segundos por km que el objetivo, el corredor va más lento.

---

## 7. Cálculo de ritmo

Crear `PaceCalculator`.

Funciones necesarias:

```kotlin
fun calculatePaceSecondsPerKm(speedMetersPerSecond: Double): Int?
fun calculateAveragePaceSecondsPerKm(distanceMeters: Double, durationSeconds: Long): Int?
fun formatPace(secondsPerKm: Int?): String
fun comparePace(current: Int?, target: Int?, tolerance: Int): PaceComparison
```

Reglas:

- Si la velocidad es cero o inválida, devolver `null`.
- El ritmo debe expresarse como segundos por kilómetro.
- La UI debe formatear el ritmo como `5:30 /km`.

---

## 8. GPS y ubicación

Crear `LocationTracker` usando `FusedLocationProviderClient`.

Debe:

1. Solicitar permisos de ubicación en tiempo de ejecución.
2. Usar ubicación precisa cuando el usuario la conceda.
3. Iniciar actualizaciones durante una carrera.
4. Detener actualizaciones al finalizar.
5. Exponer estado con `StateFlow`.
6. Calcular distancia acumulada entre puntos válidos.
7. Ignorar puntos con mala precisión.

Estado sugerido:

```kotlin
data class RunLocationState(
    val latitude: Double?,
    val longitude: Double?,
    val accuracyMeters: Float?,
    val speedMetersPerSecond: Float?,
    val totalDistanceMeters: Double,
    val currentPaceSecondsPerKm: Int?,
    val timestampMillis: Long
)
```

Criterios:

- No sumar distancia si la precisión del punto es mala.
- Evitar saltos imposibles de GPS.
- Mantener el código preparado para pantalla apagada usando servicio en primer plano.

---

## 9. Foreground Service

Crear `RunForegroundService`.

Debe:

1. Mantener activa la carrera aunque la pantalla esté apagada.
2. Mostrar notificación persistente.
3. Indicar tiempo, distancia y ritmo actual.
4. Permitir pausar o finalizar desde la notificación.
5. Usar el tipo de foreground service correspondiente para ubicación.
6. Declarar permisos en `AndroidManifest.xml`.

Permisos esperados:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

Revisar compatibilidad por versión de Android.

---

## 10. AlertEngine

Crear `AlertEngine`.

Debe decidir cuándo corresponde hablar.

Eventos posibles:

```kotlin
enum class AlertType {
    STEP_STARTED,
    STEP_ENDING_SOON,
    STEP_COMPLETED,
    TOO_FAST,
    TOO_SLOW,
    WITHIN_TARGET,
    KILOMETER_COMPLETED,
    WORKOUT_FINISHED
}
```

Objeto de alerta:

```kotlin
data class AlertEvent(
    val type: AlertType,
    val priority: AlertPriority,
    val context: RunningAlertContext,
    val createdAtMillis: Long
)

enum class AlertPriority {
    LOW,
    NORMAL,
    HIGH
}
```

Reglas:

1. Avisar siempre al comenzar un bloque.
2. Avisar siempre al finalizar un bloque.
3. Avisar cuando falten 200 metros en un bloque por distancia.
4. Avisar cuando falten 60 segundos en un bloque por tiempo.
5. Avisar si el ritmo se desvía más que la tolerancia.
6. No repetir alertas de ritmo con menos de 30 segundos de diferencia.
7. Avisar cada kilómetro completado.
8. No superponer mensajes de voz.
9. Priorizar alertas de cambio de bloque sobre alertas de ritmo.

---

## 11. VoiceCoach con Android TextToSpeech

Crear `VoiceCoach`.

Debe:

1. Inicializar `TextToSpeech`.
2. Configurar idioma español.
3. Reproducir frases breves.
4. Evitar superposición de audios.
5. Permitir cortar o limpiar cola de mensajes.
6. Liberar recursos con `shutdown()`.

Interfaz sugerida:

```kotlin
interface VoiceCoach {
    fun speak(message: String, flush: Boolean = false)
    fun stop()
    fun shutdown()
}
```

Implementación:

```kotlin
class AndroidVoiceCoach(
    private val context: Context
) : VoiceCoach {
    // Implementar TextToSpeech
}
```

---

## 12. Mensajes locales de respaldo

Crear `LocalMessageProvider`.

Debe generar mensajes sin IA:

```kotlin
fun messageFor(event: AlertEvent): String
```

Ejemplos:

```text
Empezás el bloque de intervalo.
Vas más rápido que el objetivo. Bajá un poco.
Vas más lento que el objetivo. Subí apenas el ritmo.
Buen ritmo. Mantené esta velocidad.
Terminó el bloque. Pasá a recuperación.
Faltan 200 metros.
Completaste un kilómetro.
Entrenamiento finalizado.
```

---

## 13. Integración con OpenAI

Crear `OpenAIClient` recién después de que la app funcione sin IA.

Función principal:

```kotlin
suspend fun generateRunningMessage(context: RunningAlertContext): String
```

Contexto:

```kotlin
data class RunningAlertContext(
    val alertType: AlertType,
    val stepName: String,
    val stepIndex: Int,
    val totalSteps: Int,
    val targetPace: String?,
    val currentPace: String?,
    val paceDifferenceSeconds: Int?,
    val remainingDistanceMeters: Double?,
    val remainingTimeSeconds: Long?,
    val totalDistanceMeters: Double,
    val totalDurationSeconds: Long
)
```

Prompt de sistema:

```text
Sos un asistente de running.
Tu tarea es generar mensajes breves, claros y útiles para un corredor durante un entrenamiento.
No inventes datos.
Usá solamente los datos recibidos.
Respondé en español rioplatense.
No des consejos médicos.
No des mensajes motivacionales exagerados.
Máximo 18 palabras.
```

Ejemplo de entrada:

```json
{
  "alertType": "TOO_FAST",
  "stepName": "Intervalo 2",
  "targetPace": "5:30 /km",
  "currentPace": "5:05 /km",
  "paceDifferenceSeconds": -25,
  "remainingDistanceMeters": 300
}
```

Respuesta esperada:

```text
Vas demasiado rápido. Aflojá un poco y sostené controlado estos 300 metros.
```

Reglas:

1. Timeout corto.
2. Si OpenAI falla, usar `LocalMessageProvider`.
3. No bloquear el motor de carrera por una respuesta de red.
4. No enviar ubicación exacta a OpenAI.
5. Enviar solo datos deportivos mínimos.
6. No hardcodear API key.

La API key debe leerse desde `BuildConfig` o configuración local no versionada.

---

## 14. Pantallas de UI

### 14.1 HomeScreen

Botones:

```text
Nueva carrera
Rutinas
Historial
Configuración
```

### 14.2 WorkoutListScreen

Debe mostrar rutinas guardadas.

Acciones:

```text
Crear rutina
Editar rutina
Eliminar rutina
Seleccionar rutina para correr
```

### 14.3 WorkoutEditorScreen

Debe permitir crear bloques:

- Nombre del bloque.
- Tipo de bloque.
- Objetivo por tiempo o distancia.
- Valor objetivo.
- Ritmo objetivo opcional.
- Tolerancia en segundos por km.

### 14.4 ActiveRunScreen

Debe mostrar en grande:

```text
Tiempo total
Distancia total
Ritmo actual
Ritmo promedio
Bloque actual
Ritmo objetivo
Progreso del bloque
Diferencia respecto del objetivo
```

Botones:

```text
Pausar
Reanudar
Finalizar
```

### 14.5 RunSummaryScreen

Debe mostrar:

```text
Distancia total
Tiempo total
Ritmo promedio
Cantidad de bloques cumplidos
Bloques fuera de ritmo
Mejor km
Peor km
Resumen por bloque
```

### 14.6 SettingsScreen

Debe permitir:

```text
Activar/desactivar voz
Activar/desactivar OpenAI
Frecuencia mínima de avisos
Tolerancia general de ritmo
Unidad: min/km
Configurar API key de forma segura
```

---

## 15. Orden de trabajo para Codex

No construir todo junto.

Seguir este orden:

1. Crear proyecto base.
2. Crear navegación y pantallas placeholder.
3. Crear modelos de datos.
4. Crear rutina de ejemplo.
5. Crear `PaceCalculator`.
6. Crear `WorkoutEngine`.
7. Crear tests unitarios de `PaceCalculator` y `WorkoutEngine`.
8. Crear `AlertEngine`.
9. Crear `LocalMessageProvider`.
10. Crear `VoiceCoach` con TextToSpeech.
11. Integrar simulador de carrera sin GPS.
12. Crear `ActiveRunScreen` con datos simulados.
13. Agregar GPS real.
14. Agregar Foreground Service.
15. Guardar resumen final.
16. Agregar OpenAIClient.
17. Integrar OpenAI con fallback local.
18. Pulir UI.
19. Probar en carrera corta.
20. Corregir errores de batería, permisos y GPS.

---

## 16. Prompts paso por paso para Codex

### Prompt 1 — Crear proyecto base

```text
Crea una app Android nativa en Kotlin con Jetpack Compose llamada RunningVoiceCoach.

La app debe tener navegación básica y estas pantallas placeholder:
- HomeScreen
- WorkoutListScreen
- WorkoutEditorScreen
- ActiveRunScreen
- RunSummaryScreen
- SettingsScreen

Usá una arquitectura simple por paquetes:
- domain
- data
- location
- voice
- openai
- ui

No implementes GPS ni OpenAI todavía.
Solo crea la estructura del proyecto, navegación y pantallas básicas.
```

### Prompt 2 — Modelos de dominio

```text
Agrega los modelos de datos para representar entrenamientos estructurados de running.

Crear:
- WorkoutPlan
- WorkoutStep
- StepType
- TargetType
- RunSession
- RunStepResult
- PaceStatus

Cada WorkoutStep debe permitir:
- nombre del bloque
- tipo de bloque
- objetivo por tiempo o distancia
- valor objetivo
- ritmo objetivo opcional en segundos por kilómetro
- tolerancia en segundos por kilómetro

Agrega una rutina de ejemplo llamada "Intervalos 5 x 1000".
```

### Prompt 3 — PaceCalculator

```text
Implementa PaceCalculator.

Debe incluir:
- calculatePaceSecondsPerKm(speedMetersPerSecond: Double): Int?
- calculateAveragePaceSecondsPerKm(distanceMeters: Double, durationSeconds: Long): Int?
- formatPace(secondsPerKm: Int?): String
- comparePace(current: Int?, target: Int?, tolerance: Int): PaceStatus

Reglas:
- Si velocidad o distancia son inválidas, devolver null.
- El formato debe ser mm:ss /km.
- Si current es menor que target por más de tolerance, el corredor va más rápido.
- Si current es mayor que target por más de tolerance, el corredor va más lento.

Incluye pruebas unitarias.
```

### Prompt 4 — WorkoutEngine

```text
Implementa WorkoutEngine.

Debe recibir un WorkoutPlan y el estado actual de la carrera:
- distancia total
- tiempo total
- distancia dentro del bloque
- tiempo dentro del bloque
- ritmo actual

Debe devolver WorkoutEngineState con:
- bloque actual
- índice del bloque actual
- progreso del bloque
- distancia restante o tiempo restante
- estado de ritmo
- diferencia de ritmo en segundos
- si debe pasar al siguiente bloque
- si el entrenamiento terminó

Incluye pruebas unitarias para bloques por tiempo y por distancia.
```

### Prompt 5 — AlertEngine

```text
Implementa AlertEngine.

Debe generar alertas para:
- inicio de bloque
- fin de bloque
- ritmo demasiado rápido
- ritmo demasiado lento
- ritmo correcto
- faltan 200 metros
- faltan 60 segundos
- kilómetro completado
- entrenamiento finalizado

Reglas:
- No repetir alertas de ritmo antes de 30 segundos.
- Las alertas de cambio de bloque tienen prioridad alta.
- Las alertas de ritmo tienen prioridad normal.
- Las alertas informativas tienen prioridad baja.

Crear AlertEvent, AlertType y AlertPriority.
```

### Prompt 6 — Mensajes locales

```text
Crea LocalMessageProvider.

Debe recibir un AlertEvent y devolver un mensaje breve en español rioplatense para reproducir por voz.

Los mensajes deben ser claros, útiles y de máximo 18 palabras.
No deben dar consejos médicos.
No deben inventar datos.
```

### Prompt 7 — VoiceCoach

```text
Implementa VoiceCoach usando Android TextToSpeech.

Debe:
- inicializar TextToSpeech
- usar idioma español
- reproducir mensajes breves
- evitar superponer mensajes
- permitir detener audio
- liberar recursos con shutdown

Crear una interfaz VoiceCoach y una implementación AndroidVoiceCoach.
```

### Prompt 8 — Simulador de carrera

```text
Antes de usar GPS real, crea un simulador de carrera.

Debe generar datos simulados de:
- tiempo
- distancia
- ritmo actual
- bloque actual

Integrar WorkoutEngine, AlertEngine y VoiceCoach.

La pantalla ActiveRunScreen debe mostrar datos simulados actualizados cada segundo.
Esto permite probar la lógica sin salir a correr.
```

### Prompt 9 — ActiveRunScreen real

```text
Mejora ActiveRunScreen.

Debe mostrar en grande:
- tiempo total
- distancia total
- ritmo actual
- ritmo promedio
- bloque actual
- ritmo objetivo
- progreso del bloque
- diferencia respecto del objetivo

Debe tener botones:
- iniciar
- pausar
- reanudar
- finalizar

Debe funcionar primero con el simulador de carrera.
```

### Prompt 10 — GPS real

```text
Implementa LocationTracker usando FusedLocationProviderClient.

Debe:
- pedir permisos de ubicación en tiempo de ejecución
- iniciar seguimiento de ubicación
- detener seguimiento
- calcular distancia acumulada
- calcular velocidad actual
- calcular ritmo actual
- exponer los datos mediante StateFlow
- ignorar puntos de baja precisión

Integralo con ActiveRunScreen reemplazando el simulador cuando GPS esté activado.
```

### Prompt 11 — Foreground Service

```text
Agrega RunForegroundService.

Debe:
- mantener activa la carrera con pantalla apagada
- mostrar notificación persistente
- actualizar tiempo, distancia y ritmo
- permitir finalizar carrera desde la notificación
- declarar permisos necesarios en AndroidManifest
- usar foregroundServiceType="location"

Asegurate de manejar diferencias entre versiones recientes de Android.
```

### Prompt 12 — Persistencia local

```text
Agrega persistencia local para:
- rutinas creadas
- sesiones de carrera finalizadas
- configuración de usuario

Usá DataStore si querés algo simple o Room si conviene para historial más estructurado.

La app debe poder mostrar el historial de carreras finalizadas.
```

### Prompt 13 — OpenAIClient

```text
Implementa OpenAIClient usando OkHttp.

Debe tener:
- generateRunningMessage(context: RunningAlertContext): String

Debe enviar a OpenAI datos mínimos:
- tipo de alerta
- bloque actual
- ritmo objetivo
- ritmo actual
- diferencia de ritmo
- distancia o tiempo restante

No enviar ubicación exacta.
No enviar datos personales.
No bloquear la carrera si falla la red.
Usar timeout corto.
La API key debe leerse desde BuildConfig o configuración segura, nunca hardcodeada.
```

### Prompt 14 — Integrar OpenAI con fallback

```text
Integra OpenAIClient con AlertEngine y VoiceCoach.

Cuando AlertEngine genere una alerta:
1. Crear RunningAlertContext.
2. Si OpenAI está habilitado, pedir mensaje a OpenAI.
3. Si OpenAI falla, usar LocalMessageProvider.
4. Reproducir mensaje con VoiceCoach.

La carrera nunca debe detenerse por error de OpenAI.
```

### Prompt 15 — Resumen final

```text
Implementa RunSummaryScreen.

Debe mostrar:
- distancia total
- tiempo total
- ritmo promedio
- resumen por bloque
- bloques dentro del objetivo
- bloques demasiado rápidos
- bloques demasiado lentos

Debe guardar la sesión finalizada en persistencia local.
```

### Prompt 16 — Configuración

```text
Implementa SettingsScreen.

Debe permitir:
- activar o desactivar voz
- activar o desactivar OpenAI
- configurar frecuencia mínima entre avisos
- configurar tolerancia general de ritmo
- elegir si usar mensajes locales o IA
- cargar API key de forma segura para desarrollo
```

---

## 17. Criterios de aceptación del MVP

La app se considera MVP funcional cuando:

1. Permite seleccionar una rutina.
2. Permite iniciar una carrera.
3. Muestra tiempo, distancia y ritmo.
4. Identifica correctamente el bloque actual.
5. Cambia de bloque automáticamente.
6. Detecta si el corredor va rápido, lento o dentro del objetivo.
7. Habla por auriculares usando TextToSpeech.
8. No repite alertas constantemente.
9. Funciona con pantalla apagada.
10. Guarda resumen final.
11. Funciona aunque OpenAI esté desactivado.
12. Si OpenAI falla, usa mensajes locales.

---

## 18. Reglas de seguridad y privacidad

1. No enviar ubicación exacta a OpenAI.
2. No enviar nombre, teléfono ni datos personales.
3. No dar consejos médicos.
4. No diagnosticar lesiones.
5. No empujar al usuario a sobreexigirse.
6. Permitir detener la carrera fácilmente.
7. Priorizar mensajes breves y seguros.
8. Evitar audio excesivo.

---

## 19. Reglas de voz

Los mensajes deben:

- Ser cortos.
- Ser claros.
- Estar en español rioplatense.
- No superar 18 palabras.
- Evitar tecnicismos innecesarios.
- No interrumpir constantemente.
- No superponerse.

Ejemplos correctos:

```text
Buen ritmo. Mantené esta velocidad.
Vas rápido. Aflojá apenas.
Vas lento. Subí un poco el ritmo.
Faltan 200 metros para terminar el bloque.
Pasá a recuperación.
Entrenamiento finalizado.
```

Ejemplos incorrectos:

```text
Aumentá mucho la intensidad aunque te sientas mal.
Tu frecuencia cardíaca indica un problema.
Estás lesionado.
Tenés que correr más fuerte sí o sí.
```

---

## 20. Notas técnicas importantes

- Android limita el acceso a ubicación en segundo plano, por eso la carrera debe ejecutarse como Foreground Service.
- El permiso de ubicación debe pedirse en contexto, cuando el usuario inicia una función que lo necesita.
- El GPS puede tener saltos; no confiar ciegamente en cada punto.
- Para carrera en vivo, la lógica principal debe ser local.
- OpenAI debe ser una capa de redacción, no el motor de cálculo.
- El entrenamiento no debe depender de internet.
- La app debe seguir funcionando sin OpenAI.

---

## 21. Documentación oficial de referencia

- OpenAI Realtime API: https://platform.openai.com/docs/guides/realtime
- OpenAI Audio: https://platform.openai.com/docs/guides/audio
- Android Location permissions: https://developer.android.com/develop/sensors-and-location/location/permissions/runtime
- Android Background location limits: https://developer.android.com/about/versions/oreo/background-location-limits
- Android battery and background location: https://developer.android.com/develop/sensors-and-location/location/battery
- Android TextToSpeech reference: https://developer.android.com/reference/android/speech/tts/TextToSpeech

---

## 22. Prompt maestro para Codex

Usar este prompt para iniciar el proyecto:

```text
Quiero desarrollar una app Android nativa llamada RunningVoiceCoach.

Objetivo:
Crear un asistente de voz para running que acompañe al corredor durante entrenamientos estructurados. La app debe leer GPS, calcular ritmo, comparar contra el ritmo objetivo de cada bloque y emitir avisos por voz cuando el corredor se desvíe del objetivo.

Stack:
- Kotlin
- Jetpack Compose
- FusedLocationProviderClient
- Foreground Service
- Android TextToSpeech
- OkHttp para OpenAI API
- Kotlin Coroutines
- Kotlin Flow
- Arquitectura modular simple
- Persistencia local con DataStore o Room

Primera versión MVP:
1. Crear rutinas estructuradas por tiempo o distancia.
2. Iniciar carrera.
3. Leer GPS.
4. Calcular distancia, tiempo y ritmo.
5. Detectar bloque actual.
6. Comparar ritmo actual con ritmo objetivo.
7. Emitir alertas por voz.
8. Funcionar sin OpenAI.
9. Usar OpenAI solo para redactar mensajes breves.
10. Si OpenAI falla, usar mensajes locales.
11. Guardar resumen final de la carrera.

Restricciones:
- No inventar datos deportivos.
- No enviar ubicación exacta a OpenAI.
- No dar consejos médicos.
- Mensajes de voz breves, máximo 18 palabras.
- Priorizar estabilidad y bajo consumo de batería.
- La app debe funcionar aunque la pantalla esté apagada.
- La API key no debe estar hardcodeada.

Empezá creando la estructura del proyecto, modelos de datos, pantallas básicas y navegación.
No implementes GPS ni OpenAI en el primer paso.
```

---

## 23. Primer paso concreto para ejecutar en Codex

Copiar este prompt como primera tarea real:

```text
Crea el proyecto base Android Kotlin Jetpack Compose llamado RunningVoiceCoach.

Incluí:
- estructura de paquetes limpia
- navegación básica
- HomeScreen
- WorkoutListScreen
- WorkoutEditorScreen
- ActiveRunScreen
- RunSummaryScreen
- SettingsScreen
- modelos iniciales vacíos si los necesitás

No implementes GPS, TextToSpeech ni OpenAI todavía.
Quiero primero una base estable y compilable.
Al finalizar, explicá qué archivos creaste y cuál debería ser el próximo paso.
```
