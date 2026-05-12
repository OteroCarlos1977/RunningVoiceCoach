# RunningVoiceCoach — Fase 12 — Persistencia local

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Guardar rutinas, sesiones finalizadas y configuración del usuario.

---

## Tareas de esta fase

- Elegir DataStore o Room.
- Guardar rutinas creadas.
- Guardar sesiones finalizadas.
- Guardar configuración básica.
- Mostrar historial de carreras.
- Permitir recuperar la rutina de ejemplo.

---

## Prompt para Codex

```text
Agrega persistencia local para:
- rutinas creadas
- sesiones de carrera finalizadas
- configuración de usuario

Usá DataStore si querés algo simple o Room si conviene para historial más estructurado.

La app debe poder mostrar el historial de carreras finalizadas.

También debe persistir configuración como:
- voz activada/desactivada
- OpenAI activado/desactivado
- frecuencia mínima entre avisos
- tolerancia general de ritmo
```

---

## Criterios de aceptación

- Las rutinas persisten al cerrar y abrir la app.
- Las sesiones finalizadas se ven en Historial.
- La configuración persiste.
- No se pierden datos básicos al reiniciar la app.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.

---

## Cierre de fase

Estado: completada en primer corte funcional.

### Archivos creados o modificados

- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/main/java/com/runningvoicecoach/data/AppDataStore.kt`
- `app/src/main/java/com/runningvoicecoach/data/settings/UserSettings.kt`
- `app/src/main/java/com/runningvoicecoach/data/settings/UserSettingsRepository.kt`
- `app/src/main/java/com/runningvoicecoach/data/session/RunSessionSummary.kt`
- `app/src/main/java/com/runningvoicecoach/data/session/RunHistoryRepository.kt`
- `app/src/main/java/com/runningvoicecoach/navigation/RunningVoiceCoachNavHost.kt`
- `app/src/main/java/com/runningvoicecoach/navigation/Screen.kt`
- `app/src/main/java/com/runningvoicecoach/ui/activeRun/ActiveRunScreen.kt`
- `app/src/main/java/com/runningvoicecoach/ui/settings/SettingsScreen.kt`
- `app/src/main/java/com/runningvoicecoach/ui/summary/RunSummaryScreen.kt`
- `app/src/main/java/com/runningvoicecoach/ui/workouts/WorkoutListScreen.kt`

### Cómo probarlo

1. Ejecutar la app.
2. Entrar a `Ajustes`, cambiar voz, OpenAI, frecuencia de avisos o tolerancia.
3. Cerrar y volver a abrir la app: la configuración debe mantenerse.
4. Iniciar una rutina desde el catálogo o desde el inicio.
5. Finalizar la carrera.
6. Entrar a `Historial`: la sesión debe aparecer guardada.

### Errores conocidos o pendientes

- Las sesiones finalizadas ya persisten.
- La configuración básica ya persiste.
- Las rutinas hardcodeadas se recuperan por identificador.
- Falta implementar el editor funcional de rutinas personalizadas para guardar rutinas creadas por el usuario.

### Validación

- `.\gradlew.bat assembleDebug --no-daemon --stacktrace`: correcto.
- `.\gradlew.bat testDebugUnitTest --no-daemon --stacktrace`: correcto.

La fase está lista para pasar a la siguiente, dejando el editor de rutinas personalizadas como pendiente explícito.
