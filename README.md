# RunningVoiceCoach

Aplicacion Android para corredores que combina rutinas configurables, seguimiento de ritmo, alertas por voz y una interfaz visual orientada a entrenamiento.

## Estado actual

El proyecto se encuentra en etapa MVP avanzada. Ya cuenta con estructura Android nativa, navegacion principal, rutinas base, editor de rutinas personalizadas, simulacion de carrera, GPS real, servicio en primer plano, persistencia local, resumen final y preparacion para respuestas con OpenAI usando fallback local cuando no hay API key.

## Funcionalidades implementadas

- Pantalla de inicio con identidad visual Runners, resumen del dia, mejor tiempo y proxima rutina.
- Pantalla de rutinas con listado de rutinas base y rutinas personalizadas.
- Editor real de rutinas:
  - nombre de rutina;
  - bloques continuos;
  - intervalos con repeticiones;
  - descansos;
  - objetivos por distancia o tiempo.
- Rutinas base hardcodeadas:
  - Inicio 3K;
  - Caminata + Trote;
  - 5K Principiante;
  - 5K Intermedio;
  - 10K Base;
  - Intervalos de Velocidad;
  - Series en Cuesta;
  - Fondo Largo;
  - Recuperacion Activa;
  - Preparacion 21K.
- Simulador de carrera para pruebas sin GPS.
- Control de ritmo objetivo con tolerancia, incluyendo avisos para subir o bajar el ritmo.
- Calculo de ritmo promedio.
- Motor de entrenamiento por pasos.
- Alertas locales de respaldo.
- Voz con Android TextToSpeech.
- GPS real mediante FusedLocationProviderClient.
- Foreground service para seguimiento con pantalla apagada.
- Persistencia local con DataStore.
- Historial/resumen final de carrera.
- Cliente OpenAI preparado por configuracion, con fallback local.
- Splash y launcher personalizados.
- Rediseño inicial de Home y Rutinas segun maquetado provisto.

## Tecnologias

- Kotlin.
- Android nativo.
- Jetpack Compose.
- Material 3.
- Navigation Compose.
- DataStore Preferences.
- Google Play Services Location.
- OkHttp.
- TextToSpeech Android.
- Gradle Kotlin DSL.
- JUnit para pruebas unitarias.

## Requisitos

- Android Studio.
- JDK 17.
- Android SDK con compile SDK 35.
- Dispositivo o emulador Android.

## Configuracion opcional de OpenAI

La app puede compilar sin clave de OpenAI. Para habilitar llamadas reales, definir una de estas opciones:

```properties
OPENAI_API_KEY=tu_api_key
```

Tambien puede leerse desde variable de entorno `OPENAI_API_KEY`.

El modelo configurado actualmente es:

```kotlin
OPENAI_MODEL = "gpt-5-mini"
```

Si no hay clave, la app usa mensajes locales de respaldo.

## Comandos utiles

Compilar APK debug:

```powershell
.\gradlew.bat assembleDebug
```

Ejecutar pruebas unitarias:

```powershell
.\gradlew.bat testDebugUnitTest
```

Detener daemons de Gradle si alguna ejecucion queda colgada:

```powershell
.\gradlew.bat --stop
```

## Estructura principal

- `app/src/main/java/com/otero/runningvoicecoach/domain`: modelos, calculos, motor de entrenamiento y alertas.
- `app/src/main/java/com/otero/runningvoicecoach/ui`: pantallas Compose.
- `app/src/main/java/com/otero/runningvoicecoach/data`: persistencia local e historial.
- `app/src/main/java/com/otero/runningvoicecoach/location`: GPS y servicio en primer plano.
- `app/src/main/res/drawable`: imagenes, fondos, logo e iconos.
- `documentacion`: material de maquetado y referencia.
- `capturas_app`: capturas locales de comparacion, no versionadas.

## Notas de desarrollo

- Los commits se redactan en espanol.
- `capturas_app/` se usa como carpeta local de referencia visual y no debe subirse al repositorio.
- La prioridad inmediata es consolidar funcionalidad y luego profundizar UI pantalla por pantalla.
