# RunningVoiceCoach — Fase 14 — Integración OpenAI con fallback local

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Conectar AlertEngine, OpenAIClient, LocalMessageProvider y VoiceCoach sin comprometer estabilidad.

---

## Tareas de esta fase

- Cuando AlertEngine genere alerta, crear RunningAlertContext.
- Si OpenAI está habilitado, pedir mensaje a OpenAI.
- Si OpenAI falla, usar LocalMessageProvider.
- Reproducir mensaje con VoiceCoach.
- La carrera nunca debe detenerse por error externo.
- Agregar configuración para activar/desactivar IA.

---

## Prompt para Codex

```text
Integra OpenAIClient con AlertEngine y VoiceCoach.

Cuando AlertEngine genere una alerta:
1. Crear RunningAlertContext.
2. Si OpenAI está habilitado, pedir mensaje a OpenAI.
3. Si OpenAI falla, usar LocalMessageProvider.
4. Reproducir mensaje con VoiceCoach.

La carrera nunca debe detenerse por error de OpenAI.

Agregar una opción en configuración para activar/desactivar OpenAI.
Si OpenAI está desactivado, usar siempre mensajes locales.
```

---

## Criterios de aceptación

- La app habla con mensajes IA cuando OpenAI está habilitado.
- La app usa mensajes locales cuando OpenAI falla.
- La carrera nunca se detiene por errores de red.
- La configuración permite desactivar IA.

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

- `app/src/main/java/com/otero/runningvoicecoach/ui/activeRun/ActiveRunScreen.kt`

### Cómo probarlo

- Con OpenAI desactivado en Ajustes: iniciar una carrera y verificar que las alertas usen mensajes locales.
- Con OpenAI activado pero sin API key: iniciar una carrera y verificar que la carrera no se detiene y vuelve a mensajes locales.
- Con API key configurada en `OPENAI_API_KEY`: las alertas pueden solicitar mensajes IA.

### Errores conocidos o pendientes

- Sin API key real no se puede validar respuesta remota, pero el fallback local queda activo.
- La llamada a OpenAI se ejecuta fuera del ciclo principal para no bloquear el cronometro.

### Validación

- `.\gradlew.bat assembleDebug --no-daemon --stacktrace`: correcto.
- `.\gradlew.bat testDebugUnitTest --no-daemon --stacktrace`: correcto.

La fase está lista para pasar a la siguiente.
