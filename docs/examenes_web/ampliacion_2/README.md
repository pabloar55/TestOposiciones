# Segunda ampliación de exámenes oficiales

Revisión del 25 de octubre de 2026 contra el anexo II de FA.13/24-26 aportado por el usuario. Se añaden **118 preguntas**, distribuidas en seis bancos, a las 592 anteriores. Total CAR activo: **710**, con **467** en Administrativo, **185** en Informática y **58** en la categoría independiente de Supuestos prácticos CAR. Las otras seis categorías de la app no cambian.

## Fuentes oficiales y selección

- [Auxiliar de Administración General CAR, FA.34/21-22](https://ias1.larioja.org/emp/portal/desarrollo.jsp?_estructura=04024&_proceso=S&_idClave=2021%7CCAR%7CEMPLE%7CFA34). Prueba celebrada el **15 de noviembre de 2025**, aunque la convocatoria es de 2024 y la oferta de 2021–2022. Se utilizan el cuestionario y la plantilla publicada de la fase 1, el cuestionario de Word, su fe de errores y la **plantilla modificada** de la fase 2, que sustituye expresamente la inicial.
- [Universitat de València, Administrativo C1, TL2021_C1 43](https://www.uv.es/uvweb/servicio-recursos-humanos-pas/es/gestion-convocatorias-plantilla/pruebas-selectivas/turno-libre/ref-tl2021-c1-43-pruebas-selectivas-del-grupo-subgrupo-c1-escala-administrativa-turno-libre-sector-administracion-general-1285919201515/ConvPAS.html?id=1286214655123). Primer ejercicio del **14 de mayo de 2022** y segundo ejercicio de Office 2016 del **16 de julio de 2022**. Se utilizan las plantillas enlazadas en la ficha oficial. No se les atribuye carácter definitivo: la ficha las denomina «Plantillas de respuestas». El archivo del primer cuestionario contiene por error «14-05-21» en su nombre; la portada y la plantilla permiten identificar la prueba de 2022.

| Prueba | Preguntas de origen | Añadidas | Categoría |
| --- | ---: | ---: | --- |
| Auxiliar CAR, fase 1 | 80 | 42 | Administrativo |
| UV, primer ejercicio | 100 | 34 | Administrativo |
| Auxiliar CAR, Word 2016 | 25 | 23 | Informática |
| UV, Office 2016, caso 1 | 20 | 7 | Informática, solo preguntas autónomas |
| UV, Office 2016, caso 2 | 20 | 6 | Informática, solo preguntas autónomas |
| UV, Office 2016, caso 3 | 20 | 6 | Informática, solo preguntas autónomas |
| **Total** | **265** | **118** | |

Se conserva la numeración original, incluidos sus huecos. En Office UV cada caso reinicia los números del 1 al 20 y tiene un banco distinto; su plantilla común usa los números consecutivos 1–60. El importador cruza el caso 1 con 1–20, el caso 2 con 21–40 y el caso 3 con 41–60.

## Filtro y correcciones

Las **147 preguntas no incorporadas** se desglosan así:

- **CAR fase 1, 38:** los 30 psicotécnicos; 46, sobre detalles del libro de Quejas y Sugerencias; 47, sobre el Sistema de Archivos; 60–63, sobre leyes específicas de igualdad y violencia de género; 64–65, sobre transparencia. Se conservan derechos en el procedimiento y funciones de registro de las preguntas 45 y 48, la organización ministerial de la 44 y el régimen propio de convenios de la Ley 4/2005 en la 78.
- **CAR Word, 2:** preguntas **6 y 12**, anuladas en la plantilla modificada. En la **17** se aplica la fe de errores: «la sección actual termina en una página **impar**». Se mantiene la respuesta oficial C.
- **UV teoría, 66:** legislación y organización universitarias, normativa valenciana, PRL y legislación específica de violencia de género ajenas al programa. También se excluye la 62, ligada a un umbral de contratación de 2022 cuya traslación al ejercicio actual no se ha validado.
- **UV Office, 41:** no se importa el supuesto completo. Se realiza una selección conservadora de **19 preguntas autónomas revisadas**, que no necesitan tablas, documentos ni respuestas anteriores. Las 41 restantes quedan fuera de esta ampliación; esto no afirma que todas ellas dependan de un contexto anterior.

Las preguntas autónomas UV son: caso 1, **2, 5, 6, 9, 11, 15 y 20**; caso 2, **11, 12, 13, 14, 15 y 20**; caso 3, **3, 5, 7, 8, 10 y 13**. Los nombres de archivos como «PERSONAS» o «PROCEDIMIENTOS» son parte del enunciado original; estas preguntas se resuelven con los datos de su propio enunciado y las funciones de Office 2016.

Los **58 supuestos CAR anteriores siguen exclusivamente en su categoría separada**. No se añaden preguntas que necesiten un caso anterior al aleatorio de Administrativo o Informática. Los nuevos bancos se agregan al final de sus catálogos y mantienen las posiciones previas y las versiones de progreso.

No se han encontrado duplicados exactos tras normalizar enunciado y opciones, eliminar numeración y marcas de imagen, y comparar con todos los bancos CAR existentes. Se conservan preguntas diferentes sobre un mismo artículo. Las claves corresponden a los exámenes originales; la revisión verifica correspondencia con el temario y fidelidad de transcripción, sin constituir una revisión completa de cambios legislativos posteriores.

## Contenidos reforzados

La teoría amplía Constitución, Cortes, Corona, Tribunal Constitucional, Defensor del Pueblo, CGPJ, organización ministerial, instituciones europeas, Estatuto de La Rioja, organización propia de la CAR, procedimiento, recursos, contratos, función pública y protección de datos.

| Aplicación, contando Administrativo e Informática | Antes | Añadidas | Total |
| --- | ---: | ---: | ---: |
| Access 2016 | 18 | 10 | **28** |
| Excel 2016 | 20 | 7 | **27** |
| Word 2016 | 163 | 25 | **188** |

Access amplía consultas de anexado, vistas, formularios, informes, relaciones y expresiones; Excel, CONTARA, libros, impresión, relleno, efectos de fuente y guardado; Word, estilos, selección, formato, secciones, correspondencia, impresión, recuperación y restricciones de edición. Siguen pendientes más preguntas de importación/vinculación/exportación en Access, mayor variedad de fórmulas y análisis de datos en Excel y formularios de Word. El aumento del banco no acredita cobertura exhaustiva del programa.

## Archivos y reproducción

- [fuentes.json](fuentes.json): título, URL exacta y SHA-256 de los **nueve PDF oficiales** conservados en `fuentes/`. Los PDF se guardan como documentación, fuera de los recursos de la app.
- [transcripcion.json](transcripcion.json): texto digital CAR y transcripción OCR revisada de las preguntas UV seleccionadas, con página del PDF. Se corrigen errores de reconocimiento y saltos de línea sin cambiar el significado. En Word 8 se describe el formato tipográfico original y se adjunta el panel original de opciones; en Word 17 se incorpora la corrección oficial.
- [plantillas_origen.json](plantillas_origen.json): claves completas. Las CAR y UV de teoría proceden de texto digital; las 60 claves de UV Office se han leído de los círculos rellenos de la plantilla oficial, con atención al cambio de columna entre 36 y 37.
- [revision.json](revision.json): las **265 decisiones**, clave oficial y tema o motivo de exclusión; las preguntas transcritas incluyen texto, opciones y página. Los códigos G-I, G-II, E-I, E-II, E-III e INF siguen la numeración del anexo II.
- [Importador](../../../scripts/importar_examenes_adicionales.py): valida las huellas de las fuentes, cruza las claves por número, filtra, evita duplicados y genera los TXT, **siete recortes PNG originales** de Word y las referencias de prueba. Se ejecuta sin red ni OCR adicional usando Python con `pdfplumber` y Pillow. Repetirlo produce el mismo resultado.
- [Pruebas](../../../shared/src/jvmTest/kotlin/com/pablo/testoposiciones/AdditionalOfficialExamsDesktopTest.kt) y [referencias](../../../shared/src/jvmTest/resources/examenes_adicionales): comprueban las claves, números originales, exclusiones, enunciado corregido, selección autónoma y recursos gráficos.

Validación: pruebas JVM y Android host, compilación de Android y escritorio, inspección visual de los recortes y comprobación de los recursos nuevos en el APK. El catálogo mantiene los 58 supuestos separados.
