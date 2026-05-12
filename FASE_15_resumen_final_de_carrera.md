# RunningVoiceCoach — Fase 15 — Resumen final de carrera

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Mostrar y guardar el resumen del entrenamiento realizado.

---

## Tareas de esta fase

- Crear RunSummaryScreen funcional.
- Mostrar distancia total, tiempo total y ritmo promedio.
- Mostrar resumen por bloque.
- Identificar bloques dentro del objetivo, rápidos y lentos.
- Guardar la sesión finalizada.
- Permitir volver al inicio o historial.

---

## Prompt para Codex

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

El resumen debe ser simple, claro y útil para revisar el entrenamiento después de correr.
```

---

## Criterios de aceptación

- El resumen se muestra al finalizar una carrera.
- La sesión se guarda en historial.
- Se ve cumplimiento por bloque.
- Los datos no dependen de OpenAI.
- La pantalla permite volver al inicio.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.

---

## Cierre de fase

Estado: completada.

### Archivos creados o modificados

- `app/src/main/java/com/otero/runningvoicecoach/data/session/RunSessionSummary.kt`
- `app/src/main/java/com/otero/runningvoicecoach/data/session/RunHistoryRepository.kt`
- `app/src/main/java/com/otero/runningvoicecoach/ui/activeRun/ActiveRunScreen.kt`
- `app/src/main/java/com/otero/runningvoicecoach/ui/summary/RunSummaryScreen.kt`
- `app/src/main/java/com/otero/runningvoicecoach/navigation/RunningVoiceCoachNavHost.kt`

### Cómo probarlo

1. Iniciar una rutina.
2. Finalizarla manualmente o dejar que termine.
3. Verificar que aparece `Resumen`.
4. Revisar distancia total, tiempo total, ritmo promedio, conteo de bloques y detalle por bloque.
5. Usar `Volver al inicio`.

### Errores conocidos o pendientes

- Las sesiones antiguas guardadas antes de esta fase pueden aparecer sin detalle por bloque.
- El resumen no depende de OpenAI.

### Validación

- `.\gradlew.bat assembleDebug --no-daemon --stacktrace`: correcto.
- `.\gradlew.bat testDebugUnitTest --no-daemon --stacktrace`: correcto.

La fase está lista para pasar a la siguiente.
