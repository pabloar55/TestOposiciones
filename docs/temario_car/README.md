# Selección para Administrativo de Administración General de la CAR

Convocatoria **FA.13/24-26**, anexo II del BOR de 10 de junio de 2026, número 108. Referencia: páginas **16–18 del PDF aportado** (10268–10270 del boletín). Revisión realizada el 25 de octubre de 2026.

Se han revisado las **661 preguntas** de las categorías de administrativo e informática. La aplicación ofrece ahora **365**: **251** en «Administrativo CAR» y **114** en «Informática CAR (Office 2016)». Se descartan **296**. Las otras seis categorías no se modifican.

| Examen de origen | Antes | Conservadas | Descartadas |
| --- | ---: | ---: | ---: |
| Administrativo 2023, promoción interna | 60 | 41 | 19 |
| Auxiliar administrativo septiembre 2024 | 97 | 35 | 62 |
| Auxiliar administrativo octubre 2022 | 143 | 46 | 97 |
| Administrativo mayo 2024 | 98 | 51 | 47 |
| Auxiliar administrativo enero 2026 | 120 | 78 | 42 |
| Internet, banco antiguo | 28 | 0 | 28 |
| Word, introducción y entorno | 35 | 34 | 1 |
| Word 2016, formatos de fuente | 20 | 20 | 0 |
| Word 2016, atajos | 20 | 20 | 0 |
| Word 2016, botones | 20 | 20 | 0 |
| Word 2016, tablas | 20 | 20 | 0 |
| **Total** | **661** | **365** | **296** |

## Criterio de correspondencia

La decisión se basa en el asunto de cada pregunta y, en los casos limítrofes, sus opciones. No se aplica un filtro automático por palabras o por número de ley. Los códigos de referencia son: **G-I** organización del Estado; **G-II** organización de la CAR; **E-I** derecho administrativo; **E-II** gestión de personal; **E-III** gestión financiera; **INF** informática. Los números tras esos códigos corresponden a los temas del anexo II.

Se conservan Constitución y Estatuto de La Rioja, régimen jurídico de los órganos, actos, procedimiento y recursos, contratación, protección de datos, presupuestos y Seguridad Social. También el empleo público general y sus retribuciones, que encajan en gestión de personal y financiera, aunque la pregunta proceda de un examen sanitario. Por ejemplo, el concepto general de complemento de destino de administrativo 2024, pregunta 80, se conserva.

Las preguntas del RGPD sobre principios y derechos se consideran relacionadas con E-I.10 (protección de datos); no se descartan simplemente porque el tema mencione la LOPDGDD. Las de la Ley 5/2014 se revisan individualmente: se mantienen las de sede o firma ligadas a la relación electrónica (auxiliar 2022, 94 y 97; auxiliar 2026, 45), pero se excluyen su estructura, evaluación, planificación y otros detalles que no desarrollan el contenido concreto de derechos y obligaciones de relación electrónica de E-I.3.

Se excluyen estos contenidos:

| Motivo | Preguntas |
| --- | ---: |
| Legislación sanitaria, organización sanitaria y autonomía del paciente | 58 |
| Régimen específico, selección, provisión y retribuciones del personal estatutario del SERIS | 48 |
| Naturaleza y códigos presupuestarios específicos del SERIS | 3 |
| Prevención de riesgos laborales como normativa específica | 16 |
| Transparencia y buen gobierno de la Ley 3/2014 | 22 |
| Detalles de administración electrónica ajenos al contenido delimitado de relación electrónica | 10 |
| Convenios, consorcios y relaciones interadministrativas fuera del tema de órganos | 6 |
| Windows, hardware, redes, correo e informática general | 46 |
| Banco de Internet | 28 |
| Bloques expresamente formulados para Office 2010 | 58 |
| Interfaz de Word cuya correspondencia con 2016 no queda acreditada | 1 |
| **Total** | **296** |

**Office se ha tratado con un criterio conservador de versión.** Se exigen Access, Excel y Word **2016**. Se separan las 58 preguntas de bloques de Office 2010, aunque algunas funciones también existan en 2016; no se ha validado ni reescrito cada una para convertirla en una pregunta de 2016. En auxiliar 2022, las preguntas 106 y 109 también pertenecen al bloque de Word 2010 por su contexto. Se mantienen las preguntas sin versión expresa de Access y Excel que tratan las operaciones incluidas en los temas INF.1–2.

La pregunta 8 de «Word, introducción» pregunta por el nuevo cuadro Microsoft Search en la barra de título. Se separa por no quedar acreditada esa ubicación para la interfaz de Word 2016, que presenta Dígame en la cinta. La [documentación de Microsoft sobre Dígame](https://support.microsoft.com/en-us/office/do-things-quickly-with-tell-me) distingue esa presentación y los cambios de nombre de la herramienta; la exclusión es un criterio de compatibilidad conservador.

Los ejercicios de fuente, atajos, botones y tablas de Word se incluyen como manejo de las herramientas de las pestañas de la cinta del tema INF.3. La selección comprueba **correspondencia temática**, no la vigencia jurídica de cada respuesta histórica ni la corrección material completa de los bancos. Tampoco completa los temas que aún carecen de preguntas.

## Trazabilidad y recuperación

- [revision.json](revision.json) registra **cada una de las 661 decisiones**, con número, enunciado, referencia al tema o motivo de exclusión, y las huellas SHA-256 del PDF y de las parejas TXT originales.
- [descartadas.json](descartadas.json) conserva las **296 preguntas descartadas completas**, con sus opciones, respuesta, examen y motivo. Se pueden recuperar sin reconstruirlas desde un PDF.
- [seleccion.tsv](../../shared/src/jvmTest/resources/temario_car/seleccion.tsv) es la selección explícita usada por las pruebas. Los números originales se conservan; no se renumeran los ejercicios ni se modifican las claves históricas de [plantillas.tsv](../../shared/src/jvmTest/resources/migracion/plantillas.tsv).
- El banco íntegro de Internet deja de figurar en el catálogo activo. Sus TXT permanecen como origen; no se cargan en los tests de la aplicación. La imagen de la pregunta 92 de administrativo 2024 también permanece disponible para recuperar el original.
- Los nuevos bancos usan la versión de progreso `CAR_FA13_2026_v1`. Empiezan desde la primera pregunta; el avance de los bancos anteriores queda separado. Las demás categorías conservan su almacenamiento.

## Comprobaciones

Las pruebas contrastan la selección completa contra las plantillas originales, comprueban que no se reintroduzcan anuladas, y verifican el número y letra de todas las respuestas activas. Comprueban también las 48 referencias a imágenes (34 PNG distintos), la carga de las categorías y la separación del progreso en Android y escritorio.

Validación ejecutada: `:shared:jvmTest`, `:shared:testAndroidHostTest`, `:androidApp:assembleDebug` y `:desktopApp:classes`, con resultado correcto. Se ha cotejado además la selección con Git: los 661 originales se conservan entre los bancos activos y el archivo de descartes, sin alterar enunciados ni opciones. El APK generado contiene las 251 preguntas de administrativo y las 114 de informática en sus catálogos activos.
