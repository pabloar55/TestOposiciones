package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.ExtraerPreguntas
import com.pablo.testoposiciones.logic.TestCategories
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import testoposiciones.shared.generated.resources.Res
import kotlin.test.*

class LegacyMigrationDesktopTest {
    @Test
    fun everyMigratedQuestionMatchesItsSourceTemplateAndAnnulledQuestionsAreExcluded() = runBlocking {
        val templates = checkNotNull(javaClass.getResourceAsStream("/migracion/plantillas.tsv"))
            .bufferedReader().use { reader ->
                reader.readLines().filter { it.isNotBlank() && !it.startsWith("#") }
                    .associate { it.substringBefore('\t') to it.substringAfter('\t') }
            }
        val categories = TestCategories.load().filter { it.id in setOf("ADMINISTRATIVO", "INFORMATICA") }
        assertEquals(2, categories.size)
        assertEquals(templates.keys, categories.flatMap { it.exams }.map { it.id }.toSet())
        var total = 0
        for (category in categories) {
            for (exam in category.exams) {
                val questions = ExtraerPreguntas.extraerPreguntas(category.copy(exams = listOf(exam)))
                val expected = templates.getValue(exam.id).withIndex().filter { it.value != 'x' }
                    .map { (index, letter) -> index + 1 to letter }
                val actual = questions.map { it.texto.substringBefore(')').toInt() to it.respuestaCorrecta }
                assertEquals(expected, actual, exam.displayName)
                assertTrue(questions.all { q -> q.opciones.all { it.length > 3 } }, exam.displayName)
                total += questions.size
            }
        }
        assertEquals(661, total)
    }

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun allQuestionIllustrationsArePackagedAndOnlyAssignedWhereNeeded() = runBlocking {
        val categories = TestCategories.load().filter { it.id in setOf("ADMINISTRATIVO", "INFORMATICA") }
        val questions = categories.flatMap { ExtraerPreguntas.extraerPreguntas(it) }
        val paths = questions.mapNotNull { it.imagenPath }
        assertEquals(49, paths.size)
        assertEquals(35, paths.toSet().size)
        val pngSignature = listOf(137, 80, 78, 71, 13, 10, 26, 10)
        for (path in paths.toSet()) {
            val bytes = Res.readBytes(path)
            assertEquals(pngSignature, bytes.take(8).map { it.toInt() and 255 }, path)
        }

        val telnet = questions.single { it.texto == "4) ¿Qué es Telnet?" }
        assertEquals('a', telnet.respuestaCorrecta)
        assertEquals("a) Un servicio de conexión remota a ordenador.", telnet.opcionA())

        val pilcrow = questions.single { it.texto.startsWith("24) ¿Qué acción ejecuta el siguiente icono en Word?") }
        assertNotNull(pilcrow.imagenPath)
        assertFalse(pilcrow.texto.contains("icono Pi"))

        val recovered = questions.single { it.texto.startsWith("90) Según el artículo 9 de la Ley 31/1995") }
        assertEquals('d', recovered.respuestaCorrecta)
        assertEquals("d) Inspección de Trabajo y Seguridad Social.", recovered.opcionD())
    }
}
