# RunningVoiceCoach — Fase 16 — Configuración, ajustes finales y criterios MVP

Este documento corresponde a una fase independiente para trabajar en Codex.

**Regla general:** no avanzar a la fase siguiente hasta que esta compile, funcione y cumpla sus criterios de aceptación.

---

## Objetivo

Completar SettingsScreen y revisar que la app cumpla los criterios de MVP funcional.

---

## Tareas de esta fase

- Permitir activar/desactivar voz.
- Permitir activar/desactivar OpenAI.
- Configurar frecuencia mínima entre avisos.
- Configurar tolerancia general de ritmo.
- Elegir mensajes locales o IA.
- Cargar API key de forma segura para desarrollo.
- Revisar permisos, batería, GPS y flujo completo.
- Corregir errores finales.

---

## Prompt para Codex

```text
Implementa SettingsScreen.

Debe permitir:
- activar o desactivar voz
- activar o desactivar OpenAI
- configurar frecuencia mínima entre avisos
- configurar tolerancia general de ritmo
- elegir si usar mensajes locales o IA
- cargar API key de forma segura para desarrollo

Luego revisá el flujo completo:
1. seleccionar rutina
2. iniciar carrera
3. leer GPS
4. calcular ritmo
5. detectar bloque actual
6. emitir alertas
7. funcionar con pantalla apagada
8. finalizar carrera
9. guardar resumen
10. mostrar historial

Corregí errores de compilación, permisos y estado.
```

---

## Criterios de aceptación

- La configuración funciona y persiste.
- La app permite correr con voz local sin OpenAI.
- OpenAI es opcional.
- La carrera funciona con pantalla apagada.
- El resumen se guarda correctamente.
- El proyecto compila limpio.
- La app cumple el MVP funcional.

---

## Notas

- Criterio central: que la app sea confiable antes de hacerla más sofisticada.

---

## Instrucción de cierre para Codex

Al finalizar esta fase, indicá:

1. Archivos creados o modificados.
2. Cómo probarlo.
3. Errores conocidos o pendientes.
4. Si la fase está lista para pasar a la siguiente.
