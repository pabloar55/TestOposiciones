package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.ExtraerPreguntas
import com.pablo.testoposiciones.logic.TestCategories
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import testoposiciones.shared.generated.resources.Res
import kotlin.test.*

class LegacyMigrationDesktopTest {
    @Test
    fun selectedQuestionsMatchSourceTemplatesAndCarSyllabusSelection() = runBlocking {
        val templates = checkNotNull(javaClass.getResourceAsStream("/migracion/plantillas.tsv"))
            .bufferedReader().use { reader ->
                reader.readLines().filter { it.isNotBlank() && !it.startsWith("#") }
                    .associate { it.substringBefore('\t') to it.substringAfter('\t') }
            }
        val selection = checkNotNull(javaClass.getResourceAsStream("/temario_car/seleccion.tsv"))
            .bufferedReader().use { reader ->
                reader.readLines().filter { it.isNotBlank() && !it.startsWith("#") }
                    .map { it.split('\t') }
            }
        assertEquals(661, selection.size)
        assertEquals(selection.size, selection.map { it[0] to it[1].toInt() }.toSet().size)
        assertTrue(selection.all { it[2] in setOf("conservar", "descartar") && it[3].isNotBlank() })
        // La selección debe justificar todas las preguntas originales válidas,
        // sin reintroducir anuladas ni alterar las plantillas de referencia.
        assertEquals(templates.keys, selection.map { it[0] }.toSet())
        for ((examId, template) in templates) {
            assertEquals(
                template.withIndex().filter { it.value != 'x' }.map { it.index + 1 },
                selection.filter { it[0] == examId }.map { it[1].toInt() },
                examId
            )
        }
        val retainedByExam = selection.filter { it[2] == "conservar" }
            .groupBy({ it[0] }, { it[1].toInt() })
        val categories = TestCategories.load().filter { it.id in setOf("ADMINISTRATIVO", "INFORMATICA") }
        assertEquals(2, categories.size)
        assertEquals(retainedByExam.keys, categories.flatMap { it.exams }.map { it.id }.toSet())
        assertTrue(categories.all { it.progressVersion == "CAR_FA13_2026_v1" })
        var total = 0
        for (category in categories) {
            for (exam in category.exams) {
                val questions = ExtraerPreguntas.extraerPreguntas(category.copy(exams = listOf(exam)))
                val expected = templates.getValue(exam.id).withIndex()
                    .filter { it.index + 1 in retainedByExam.getValue(exam.id) }
                    .map { (index, letter) -> index + 1 to letter }
                val actual = questions.map { it.texto.substringBefore(')').toInt() to it.respuestaCorrecta }
                assertEquals(expected, actual, exam.displayName)
                assertTrue(questions.all { q -> q.opciones.all { it.length > 3 } }, exam.displayName)
                total += questions.size
            }
        }
        assertEquals(365, total)
    }

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun allQuestionIllustrationsArePackagedAndOnlyAssignedWhereNeeded() = runBlocking {
        val categories = TestCategories.load().filter { it.id in setOf("ADMINISTRATIVO", "INFORMATICA") }
        val questions = categories.flatMap { ExtraerPreguntas.extraerPreguntas(it) }
        val paths = questions.mapNotNull { it.imagenPath }
        assertEquals(48, paths.size)
        assertEquals(34, paths.toSet().size)
        val pngSignature = listOf(137, 80, 78, 71, 13, 10, 26, 10)
        for (path in paths.toSet()) {
            val bytes = Res.readBytes(path)
            assertEquals(pngSignature, bytes.take(8).map { it.toInt() and 255 }, path)
        }

        assertFalse(questions.any { it.texto == "4) ¿Qué es Telnet?" })

        val pilcrow = questions.single { it.texto.startsWith("24) ¿Qué acción ejecuta el siguiente icono en Word?") }
        assertNotNull(pilcrow.imagenPath)
        assertFalse(pilcrow.texto.contains("icono Pi"))

        assertFalse(questions.any { it.texto.startsWith("90) Según el artículo 9 de la Ley 31/1995") })
    }
}
