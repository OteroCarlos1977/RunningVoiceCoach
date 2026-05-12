# RunningVoiceCoach — Fase 00 — Contexto general y reglas del proyecto

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Crear una app Android nativa llamada RunningVoiceCoach que funcione como asistente de voz para running, leyendo GPS, calculando ritmo y emitiendo avisos por voz durante entrenamientos estructurados.

---

## Tareas de esta fase

- Usar Kotlin y Jetpack Compose.
- Priorizar estabilidad, bajo consumo de batería y claridad del código.
- No inventar datos deportivos: todos los cálculos deben salir de GPS, tiempo, distancia y rutina cargada.
- No enviar ubicación exacta ni datos personales a servicios externos.
- Primero desarrollar sin OpenAI; luego agregar IA como capa opcional.
- La app debe funcionar con pantalla apagada mediante Foreground Service en fases posteriores.

---

## Prompt para Codex

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
- Arquitectura modular simple
- Persistencia local con DataStore o Room

Restricciones:
- No inventar datos deportivos.
- No dar consejos médicos.
- Mensajes de voz breves, máximo 18 palabras.
- Priorizar estabilidad y bajo consumo de batería.
- La app debe funcionar aunque la pantalla esté apagada.
- La API key no debe estar hardcodeada.

Todavía no escribas código. Leé este contexto y esperá la fase específica que te voy a entregar.
```

---

## Criterios de aceptación

- Codex comprende el objetivo general.
- Codex no intenta crear toda la app en una sola respuesta.
- Codex espera instrucciones por fases.
- Queda claro que OpenAI es opcional y posterior al MVP sin IA.

---

## Notas

- Este archivo puede entregarse primero, antes de la Fase 01.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.
