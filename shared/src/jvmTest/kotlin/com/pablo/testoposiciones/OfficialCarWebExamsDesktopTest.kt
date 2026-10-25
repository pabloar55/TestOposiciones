package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.ExtraerPreguntas
import com.pablo.testoposiciones.logic.TestCategories
import java.security.MessageDigest
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import testoposiciones.shared.generated.resources.Res
import kotlin.test.*

class OfficialCarWebExamsDesktopTest {
    private fun fixture(path: String): List<List<String>> =
        checkNotNull(javaClass.getResourceAsStream(path)).bufferedReader().use { reader ->
            reader.readLines().filter { it.isNotBlank() && !it.startsWith("#") }.map { it.split('\t') }
        }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) }

    @Test
    fun importedExamsPreserveOfficialKeysAndExcludeAnnulledOrOutOfSyllabusQuestions() = runBlocking {
        val selection = fixture("/examenes_web/seleccion.tsv")
        val templates = fixture("/examenes_web/plantillas.tsv").associate { it[0] to it[1] }
        assertEquals(240, selection.size)
        assertEquals(240, selection.map { it[0] to it[1] }.toSet().size)
        assertEquals(4, selection.count { it[3] == "x" })
        assertEquals(13, selection.count { it[2] == "descartar" })
        val expectedCounts = mapOf(
            "CAR_2025_LIBRE_TEORICO" to 89, "CAR_2025_LIBRE_SUPUESTOS" to 38,
            "CAR_2025_PI_TEORICO" to 51, "CAR_2025_PI_SUPUESTOS" to 20,
            "CAR_2025_OFIMATICA" to 19, "CAR_2025_PI_OFIMATICA" to 10
        )
        val categories = TestCategories.load()
        val actualIds = categories.flatMap { it.exams }.filter { it.id.startsWith("CAR_2025_") }.map { it.id }.toSet()
        assertEquals(expectedCounts.keys, actualIds)
        for (category in categories) {
            for (exam in category.exams.filter { it.id in expectedCounts }) {
                if (exam.id.endsWith("SUPUESTOS")) {
                    assertEquals("SUPUESTOS_ADMINISTRATIVO_CAR", category.id)
                } else {
                    assertNotEquals("SUPUESTOS_ADMINISTRATIVO_CAR", category.id)
                }
                val rows = selection.filter { it[0] == exam.id }
                for (row in rows) assertEquals(templates.getValue(exam.id)[row[1].toInt()-1], row[3].single())
                val retained = rows.filter { it[2] == "añadir" }
                val questions = ExtraerPreguntas.extraerPreguntas(category.copy(exams = listOf(exam)))
                assertEquals(expectedCounts.getValue(exam.id), questions.size)
                assertEquals(retained.map { it[1].toInt() }, questions.map { it.texto.substringBefore(')').toInt() })
                for ((q, row) in questions.zip(retained)) {
                    assertEquals(row[3].single(), q.respuestaCorrecta, "${exam.id}: ${row[1]}")
                    assertEquals(row[4], sha256((q.texto + "\n" + q.opciones.joinToString("\n")).toByteArray()))
                    assertTrue(q.opciones.all { it.length > 3 })
                    if (exam.id.endsWith("SUPUESTOS")) {
                        assertTrue(q.texto.contains("[Contexto íntegro del supuesto"))
                    } else {
                        assertFalse(q.texto.contains("[Contexto íntegro del supuesto"))
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun allTablesChartsAndImageOptionsAreAvailableInsideTheApp() = runBlocking {
        val selection = fixture("/examenes_web/seleccion.tsv")
        var illustrated = 0
        for (category in TestCategories.load()) {
            for (exam in category.exams.filter { it.id.startsWith("CAR_2025_") }) {
                val rows = selection.filter { it[0] == exam.id && it[2] == "añadir" }
                val questions = ExtraerPreguntas.extraerPreguntas(category.copy(exams = listOf(exam)))
                for ((q, row) in questions.zip(rows)) {
                    if (row[5] == "-") assertNull(q.imagenPath)
                    else {
                        val bytes = Res.readBytes(assertNotNull(q.imagenPath))
                        assertEquals(row[5], sha256(bytes))
                        assertEquals(listOf(137,80,78,71,13,10,26,10), bytes.take(8).map { it.toInt() and 255 })
                        illustrated++
                    }
                }
                if (exam.id == "CAR_2025_OFIMATICA") {
                    val queryOptions = questions.single { it.texto.startsWith("11)") }
                    assertEquals('b', queryOptions.respuestaCorrecta)
                    assertTrue(queryOptions.opcionB().contains("Consulta B"))
                    assertNotNull(queryOptions.imagenPath)
                }
            }
        }
        assertEquals(17, illustrated)
    }
}
