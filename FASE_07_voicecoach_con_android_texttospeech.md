# RunningVoiceCoach — Fase 07 — VoiceCoach con Android TextToSpeech

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Implementar la reproducción de mensajes de voz usando el motor nativo de Android.

---

## Tareas de esta fase

- Crear interfaz VoiceCoach.
- Crear AndroidVoiceCoach usando TextToSpeech.
- Inicializar idioma español.
- Evitar superponer mensajes.
- Permitir detener audio.
- Liberar recursos con shutdown.
- Integrar con mensajes locales en una pantalla de prueba.

---

## Prompt para Codex

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

Integralo inicialmente con LocalMessageProvider para probar mensajes sin GPS y sin OpenAI.

Agregar manejo de errores si el idioma español no está disponible en el dispositivo.
```

---

## Criterios de aceptación

- La app reproduce mensajes por audio.
- No se superponen mensajes.
- Se puede detener el audio.
- TextToSpeech libera recursos correctamente.
- La app sigue funcionando si el idioma español no está disponible.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.
