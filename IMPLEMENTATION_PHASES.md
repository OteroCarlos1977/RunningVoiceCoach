# RunningVoiceCoach - Fases de implementacion

Este archivo resume el orden de trabajo definido en `PLAN_CODEX_RUNNING_VOICE_COACH.md`.

## Estado actual

Fase 16 completada:

- Proyecto Android base creado.
- Modulo `app` configurado con Kotlin y Jetpack Compose.
- Navegacion Compose agregada.
- Pantallas placeholder agregadas:
  - `HomeScreen`
  - `WorkoutListScreen`
  - `WorkoutEditorScreen`
  - `ActiveRunScreen`
  - `RunSummaryScreen`
  - `SettingsScreen`
- Paquetes base creados:
  - `domain`
  - `data`
  - `location`
  - `voice`
  - `openai`
  - `ui`
- Modelos de dominio agregados para rutinas, bloques, sesiones y resultados por bloque.
- Rutina de ejemplo `Intervalos 5 x 1000` agregada y visible desde `WorkoutListScreen`.
- `PaceCalculator` agregado para calcular, formatear y comparar ritmos.
- `WorkoutEngine` agregado para calcular bloque actual, progreso, restantes, cambio de bloque y fin de entrenamiento.
- `AlertEngine` agregado para generar alertas de bloque, ritmo, proximidad de fin, kilometros y finalizacion.
- `LocalMessageProvider` agregado para generar mensajes locales breves sin OpenAI.
- `VoiceCoach` agregado con implementacion Android basada en TextToSpeech.
- Simulador de carrera sin GPS integrado en `ActiveRunScreen`.
- La pantalla de carrera muestra tiempo, distancia, ritmo actual, ritmo promedio, bloque actual, ritmo objetivo, diferencia respecto del objetivo, estado y progreso.
- La simulacion permite iniciar, pausar, reanudar y finalizar.
- La pantalla activa usa metricas grandes y estado guardable para el simulador.
- Dependencia `play-services-location` agregada.
- Permisos `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION` declarados.
- `LocationTracker` agregado con `FusedLocationProviderClient`, `StateFlow`, filtro de precision y calculo de distancia/ritmo.
- `ActiveRunScreen` permite alternar entre simulador y GPS real, solicitando permisos en tiempo de ejecucion.
- `RunForegroundService` agregado con notificacion persistente para carreras con GPS.
- Permisos `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION` y `POST_NOTIFICATIONS` declarados.
- La notificacion muestra tiempo, distancia y ritmo, y permite finalizar el servicio.
- UI actualizada con identidad visual Runners: logo, paleta azul/cian/naranja, Home renovado y listado de rutinas en tarjetas.
- Catalogo hardcodeado con 10 rutinas base: Inicio 3K, Caminata + Trote, 5K, 10K, intervalos, cuestas, fondo, recuperacion y preparacion 21K.
- Dependencia `androidx.datastore:datastore-preferences` agregada para persistencia local simple.
- `UserSettingsRepository` agregado para persistir voz activada/desactivada, OpenAI activado/desactivado, frecuencia minima entre avisos y tolerancia general de ritmo.
- `SettingsScreen` conectada a DataStore para guardar y recuperar configuracion al reiniciar la app.
- `RunHistoryRepository` agregado para persistir sesiones finalizadas.
- `RunSummaryScreen` muestra historial local de carreras finalizadas.
- `ActiveRunScreen` guarda la sesion al finalizar manualmente o al completar la rutina.
- La navegacion permite iniciar una rutina seleccionada desde el catalogo y recuperarla por `workoutPlanId`.
- Dependencia OkHttp agregada para llamadas HTTP.
- `OpenAIClient` agregado usando `POST /v1/responses`, timeout corto y manejo de error controlado.
- `RunningAlertContext` agregado para enviar solo datos minimos de la alerta: tipo, bloque, ritmos, diferencia y restante.
- API key leida desde `BuildConfig.OPENAI_API_KEY`, configurada por propiedad Gradle o variable de entorno `OPENAI_API_KEY`; no esta hardcodeada.
- Permiso `INTERNET` agregado para futura integracion de red.
- Prueba unitaria agregada para validar el armado del contexto de alerta.
- Fase 14: `ActiveRunScreen` integra `OpenAIClient` con `AlertEngine` y `VoiceCoach`.
- Si OpenAI esta habilitado, los mensajes de alerta se intentan generar con IA.
- Si OpenAI falla o no hay API key, se usa `LocalMessageProvider` sin detener la carrera.
- Las llamadas a OpenAI se ejecutan fuera del ciclo principal del cronometro para no bloquear la actividad.
- Fase 15: las sesiones guardadas incluyen resumen por bloque.
- `RunSummaryScreen` muestra ultima carrera, distancia, tiempo, ritmo promedio, conteo de bloques en objetivo/rapidos/lentos y detalle por bloque.
- Al finalizar una carrera, la navegacion lleva al resumen y permite volver al inicio.
- Se agregaron fondos `fondo1` a `fondo6` para seleccionar imagen de fondo en la actividad actual.
- Fase 16: `SettingsScreen` permite activar/desactivar voz, activar/desactivar OpenAI, configurar frecuencia minima entre avisos, configurar tolerancia general de ritmo y guardar/borrar una API key de desarrollo.
- La frecuencia minima de avisos configurada se aplica al `AlertEngine`.
- La tolerancia general de ritmo configurada se aplica al `WorkoutEngine`.
- La API key de desarrollo se usa para `OpenAIClient` si esta guardada; si no, se usa `BuildConfig.OPENAI_API_KEY`.
- El campo de API key se muestra oculto y no se hardcodea en el codigo.
- Pruebas unitarias agregadas para calculo de ritmo, motor de entrenamiento, motor de alertas y mensajes locales.
- Validacion: `.\gradlew.bat testDebugUnitTest --no-daemon` pasa correctamente.
- Validacion: `.\gradlew.bat assembleDebug --no-daemon` compila correctamente.

Pendiente conocido:

- Persistencia de rutinas creadas por el usuario: el almacenamiento base esta listo, pero falta implementar el editor funcional de rutinas personalizadas.
- La seleccion de fondo de actividad aun no persiste al reiniciar la app.
- La API key guardada en DataStore es aceptable para desarrollo local; para produccion conviene usar almacenamiento cifrado o backend propio.

## Proximas fases

1. Editor de rutinas personalizadas y persistencia de rutinas creadas.
2. Persistir seleccion de fondo de actividad.
3. Pulido visual profundo con dashboards finales.
4. Prueba fisica larga de GPS, voz, pantalla apagada y bateria.

## Regla de continuidad

Al acercarnos al 5% restante del limite semanal, cerrar la tarea activa en un punto consistente y dejar anotado el siguiente paso concreto.
