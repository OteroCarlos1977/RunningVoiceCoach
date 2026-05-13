# Tareas por hacer

## Prioridad inmediata

- Revisar en dispositivo la ultima version de la pantalla Rutinas.
- Ajustar finamente tamanos, espaciados e iconos si las nuevas capturas muestran cortes.
- Continuar con funcionalidad del Home:
  - conectar resumen del dia con datos reales de sesiones;
  - calcular mejor tiempo/mejor ritmo desde historial;
  - mostrar proxima rutina real o ultima rutina seleccionada;
  - hacer navegables las acciones "Ver mas" y "Ver plan".

## Rutinas

- Revisar seleccion de rutina desde la pantalla redisenada.
- Mejorar iconos internos de rutinas con assets propios en vez de simbolos temporales.
- Evaluar filtros por nivel: Principiante, Intermedio, Avanzado, Suave.
- Agregar vista de detalle de rutina antes de iniciar entrenamiento.
- Permitir editar o borrar rutinas personalizadas guardadas.

## Entrenamiento activo

- Mejorar UI de carrera activa segun el mismo lenguaje visual.
- Mostrar bloque actual, siguiente bloque y progreso de rutina con mayor claridad.
- Revisar mensajes de voz en intervalos, descansos y finalizacion.
- Probar GPS real en exterior.
- Probar comportamiento con pantalla apagada durante varios minutos.

## Historial y progreso

- Convertir la pantalla de resumen/progreso en dashboard real.
- Agregar mejores marcas:
  - mejor ritmo promedio;
  - mejor 1K, 5K, 10K cuando existan datos suficientes;
  - mayor distancia;
  - mayor tiempo activo.
- Agregar graficos simples de evolucion semanal/mensual.
- Separar resumen de ultima carrera de progreso acumulado.

## Perfil y ajustes

- Redisenar pantalla de ajustes/perfil.
- Agregar configuracion de voz:
  - activar/desactivar voz;
  - frecuencia de avisos;
  - volumen o prioridad.
- Agregar configuracion de unidades si se requiere.
- Agregar configuracion de API key o estado de conexion OpenAI cuando corresponda.

## OpenAI

- Probar integracion real cuando exista API key.
- Revisar prompts para que la voz sea breve, util y segura durante carrera.
- Mantener fallback local como comportamiento por defecto si no hay red o clave.

## UI general

- Continuar pantallas con el maquetado visual:
  - Home;
  - Rutinas;
  - Editor de rutinas;
  - Carrera activa;
  - Progreso;
  - Perfil.
- Reemplazar simbolos temporales por iconos o assets definitivos.
- Homogeneizar barra inferior en todas las pantallas principales.
- Revisar contraste, tamanos de texto y cortes en telefonos reales.

## Calidad

- Mantener `assembleDebug` pasando antes de cada commit.
- Mantener `testDebugUnitTest` pasando antes de cada commit.
- Agregar pruebas sobre:
  - calculo de mejores marcas;
  - seleccion de proxima rutina;
  - persistencia de rutinas personalizadas;
  - historial de sesiones.

## Pendiente de decision

- Definir si el Home sera dashboard de datos reales o pantalla de inicio simplificada con llamada a iniciar rutina.
- Definir si Rutinas tendra detalle intermedio antes de iniciar.
- Definir iconografia final de cada rutina.
- Definir criterio exacto para "mejor tiempo": mejor ritmo promedio, mejor distancia por tiempo o mejores parciales.
