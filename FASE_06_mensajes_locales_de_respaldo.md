# RunningVoiceCoach — Fase 06 — Mensajes locales de respaldo

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Crear mensajes breves en español para usar sin OpenAI o cuando falle la conexión.

---

## Tareas de esta fase

- Crear LocalMessageProvider.
- Recibir AlertEvent y devolver un mensaje breve.
- Usar español rioplatense neutro.
- No superar 18 palabras.
- No dar consejos médicos.
- No inventar datos no incluidos en AlertEvent.

---

## Prompt para Codex

```text
Crea LocalMessageProvider.

Debe recibir un AlertEvent y devolver un mensaje breve en español rioplatense para reproducir por voz.

Los mensajes deben ser claros, útiles y de máximo 18 palabras.
No deben dar consejos médicos.
No deben inventar datos.

Ejemplos válidos:
- Buen ritmo. Mantené esta velocidad.
- Vas rápido. Aflojá un poco.
- Vas lento. Aumentá apenas el ritmo.
- Cambio de bloque. Pasá a recuperación.
- Faltan 200 metros para terminar este bloque.
- Entrenamiento finalizado. Buen trabajo.

Agrega tests simples para verificar que cada AlertType tenga un mensaje disponible.
```

---

## Criterios de aceptación

- Cada AlertType tiene un mensaje local.
- Los mensajes son breves y claros.
- La app puede funcionar sin OpenAI.
- No hay consejos médicos ni mensajes agresivos de sobreexigencia.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.
