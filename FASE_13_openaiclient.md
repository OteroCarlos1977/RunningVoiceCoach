# RunningVoiceCoach — Fase 13 — OpenAIClient

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Agregar integración opcional con OpenAI para generar mensajes de voz más naturales.

---

## Tareas de esta fase

- Crear OpenAIClient usando OkHttp.
- Crear RunningAlertContext.
- Enviar solo datos mínimos.
- No enviar ubicación exacta ni datos personales.
- Usar timeout corto.
- Leer API key desde BuildConfig o configuración segura.
- No bloquear la carrera si falla la red.

---

## Prompt para Codex

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

Prompt de sistema sugerido:
Sos un asistente de running. Generá mensajes breves, claros y útiles para un corredor durante un entrenamiento. No inventes datos. Usá solamente los datos recibidos. Respondé en español rioplatense. No des consejos médicos. Máximo 18 palabras.
```

---

## Criterios de aceptación

- OpenAIClient compila.
- La API key no está hardcodeada.
- No se envía ubicación exacta.
- Los timeouts están configurados.
- Si OpenAI falla, se informa error controlado sin romper la app.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.
