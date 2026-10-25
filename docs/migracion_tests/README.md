# Migración de los tests Java a KMP

> Este documento registra la importación histórica íntegra, anterior al filtro de temario. La selección inicial para la convocatoria CAR FA.13/24-26 está descrita en [temario_car/README.md](../temario_car/README.md): conserva 365 de las 661 preguntas importadas. La [ampliación con exámenes oficiales de Internet](../examenes_web/car_2025/README.md) añade otras 227 y eleva el total activo a 592. Las plantillas y la procedencia de esta carpeta siguen siendo referencias del original.

Revisión del 25 de octubre de 2026. Se incorporan las parejas TXT antiguas y todas las preguntas válidas de los PDF aportados en `test advo` y `Test informatica`. Los recursos se comparten entre Android y escritorio mediante Compose Resources.

| Categoría / examen | Antes en Java | Ahora en KMP | Anuladas excluidas |
| --- | ---: | ---: | --- |
| Administrativo, promoción interna, 17/06/2023 | 46 | 60 | Ninguna |
| Auxiliar administrativo, 07/09/2024 | 96 | 97 | 39, 82, 100 |
| Auxiliar administrativo, 29/10/2022 | 143 | 143 | 81, 98, 113, 115, 132, 137, 144 |
| Administrativo, 18/05/2024 | 98 | 98 | 42, 96 |
| Auxiliar administrativo, 24/01/2026 | 0 | 120 | Ninguna en la plantilla aportada |
| Internet, banco antiguo | 28 | 28 | Sin PDF de origen |
| Word, introducción y entorno | 35 | 35 | Ninguna |
| Word 2016, formatos de fuente | 5 | 20 | Ninguna |
| Word 2016, atajos de teclado | 20 | 20 | Ninguna |
| Word 2016, botones de la cinta | 0 | 20 | Ninguna |
| Word 2016, tablas | 0 | 20 | Ninguna |
| **Total** | **471** | **661** | **12** |

## Comprobación y correspondencia

- **633 preguntas** contrastadas con los enunciados, opciones y plantillas de los PDF locales. Se extrajo el texto de los PDF digitales y se utilizó OCR local de macOS para los escaneos. Las divergencias del OCR se contrastaron con las páginas renderizadas; las plantillas escaneadas se leyeron visualmente.
- Las letras de respuesta de las preguntas que ya estaban en los TXT y tenían PDF **coinciden con sus plantillas**. Se conservan las claves aportadas, incluidas las plantillas modificadas tras impugnaciones; no se ha sustituido su criterio por una revisión jurídica actual ni se han buscado plantillas posteriores.
- Las **28 preguntas de Internet** solo se han comprobado estructuralmente contra la pareja TXT antigua. No existe un PDF correspondiente en las carpetas recibidas. Se conservan con un nombre de examen que indica esta limitación.
- Se recuperan **190 preguntas**: 14 de administrativo 2023 (23, 24 y 49–60), la 90 de auxiliar 2024, las 120 de auxiliar 2026, las 15 primeras de formatos de fuente y los 40 ejercicios de botones y tablas.
- Se conserva la numeración original de cada examen, incluidos los huecos de las anuladas. Cada examen tiene su propia pareja de archivos; las respuestas se emparejan por posición y se verifica también el número.
- Los nombres de archivo que indican «2020 y 2021» corresponden al ejercicio fechado **29/10/2022**. Se utiliza la fecha del examen para su nombre en la app.

## Incidencias de los originales y ajustes

1. **Auxiliar septiembre 2024, pregunta 4, opción d**: la frase ya termina en «a propuesta del» en el PDF. Se conserva y se señala explícitamente como truncada; no se inventa una continuación. La respuesta de la plantilla es b.
2. **Auxiliar septiembre 2024, pregunta 90**: el PDF muestra cuatro opciones con la primera sin letra y las siguientes rotuladas a, b, c. Se normalizan como a–d siguiendo el orden de las cuatro líneas. La plantilla señala d: «Inspección de Trabajo y Seguridad Social». Esta pregunta faltaba en el TXT.
3. **Word, introducción, preguntas 20–24**: se recuperan los iconos del PDF y se eliminan las descripciones entre paréntesis que se habían añadido a mano. En la 24, el TXT describía el icono como «Pi», pero el PDF muestra el signo de párrafo ¶.
4. **Administrativo mayo 2024, pregunta 92**: se recupera el icono original, antes sustituido por «icono líneas».
5. **Word, tablas, pregunta 18**: las cuatro opciones son imágenes. Se conserva el panel con sus etiquetas a–d y se identifican las opciones seleccionables como Tabla A–D.
6. **Word, tablas, pregunta 5**: el PDF alude a una celda X pero no incluye esa figura. Se mantiene el enunciado y su clave b, con una nota de la ausencia. No se utiliza la tabla de otra pregunta como sustituto.
7. Se normalizan UTF-8, espacios, saltos de línea y marcadores de pregunta/opción. El caso de Telnet, cuyas opciones b y a estaban invertidas en el TXT, se carga correctamente por letra.

Se incluyen **35 PNG** extraídos de las páginas originales, utilizados por **49 preguntas**. Las quince preguntas de formatos de fuente comparten la misma imagen de países. Las ilustraciones aparecen tanto en el test como en la revisión de resultados y pueden ampliarse.

## Trazabilidad y validación

- [procedencia.json](procedencia.json) registra por examen los números incluidos, las anulaciones y los archivos de origen, con SHA-256 de todos los PDF y TXT utilizados. Los PDF originales permanecen en Downloads; las imágenes necesarias quedan empaquetadas en la app.
- [plantillas.tsv](../../shared/src/jvmTest/resources/migracion/plantillas.tsv) conserva las claves de referencia independientes de los archivos de respuestas de producción (`x` significa anulada). La fila de Internet procede exclusivamente de su TXT.
- Las pruebas de escritorio cargan todos los exámenes y comparan **cada número y letra** con esas claves, comprobando también las exclusiones y los 35 recursos PNG. Las pruebas comunes cubren la lectura de imágenes y el rechazo de rutas inválidas, imágenes duplicadas y numeraciones desajustadas.
- La validación incluye `:shared:jvmTest`, `:shared:testAndroidHostTest`, `:androidApp:assembleDebug` y `:desktopApp:classes`.
- Resultado: **16 pruebas superadas** (9 JVM y 7 Android host), compilación correcta en ambos destinos. Se ha comprobado que el APK contiene los 59 archivos nuevos de los bancos, incluidos los 35 PNG. La interacción visual con la ventana Java no pudo verificarse mediante la herramienta de control de aplicaciones; los recortes se revisaron por separado.

Los JAR antiguos y `numeroPregunta.txt` no son contenido de examen y no se importan. El avance antiguo pertenece a bancos con distinto tamaño; las nuevas categorías usan su propio almacenamiento KMP. Las categorías que ya existían mantienen sus identificadores y recursos.
