package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.ExtraerPreguntas
import com.pablo.testoposiciones.logic.TestCategories
import java.security.MessageDigest
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import testoposiciones.shared.generated.resources.Res
import kotlin.test.*

class AdditionalOfficialExamsDesktopTest {
    private val counts = mapOf(
        "CAR_AUXILIAR_2025_TEORIA" to 42, "UV_2022_TEORIA_CAR" to 34,
        "CAR_AUXILIAR_2025_WORD" to 23, "UV_2022_OFIMATICA_S1" to 7,
        "UV_2022_OFIMATICA_S2" to 6, "UV_2022_OFIMATICA_S3" to 6
    )

    private fun fixture(name: String): List<List<String>> =
        checkNotNull(javaClass.getResourceAsStream("/examenes_adicionales/$name.tsv"))
            .bufferedReader().use { reader ->
                reader.readLines().filter { it.isNotBlank() && !it.startsWith("#") }.map { it.split('\t') }
            }

    private fun sha256(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
        .joinToString("") { "%02x".format(it.toInt() and 255) }

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun allNewExamsMatchTheSourceNumbersKeysAndOriginalImages() = runBlocking {
        val selection = fixture("seleccion")
        val templates = fixture("plantillas").associate { it[0] to it[1] }
        assertEquals(265, selection.size)
        assertEquals(265, selection.map { it[0] to it[1] }.toSet().size)
        assertEquals(147, selection.count { it[2] == "descartar" })
        assertEquals(2, selection.count { it[3] == "x" })
        val categories = TestCategories.load()
        assertEquals(counts.keys, categories.flatMap { it.exams }.filter { it.id in counts }.map { it.id }.toSet())
        var imageCount = 0
        for (category in categories) {
            for (exam in category.exams.filter { it.id in counts }) {
                val theory = exam.id.endsWith("TEORIA") || exam.id == "UV_2022_TEORIA_CAR"
                assertEquals(if (theory) "ADMINISTRATIVO" else "INFORMATICA", category.id)
                val rows = selection.filter { it[0] == exam.id }
                assertEquals((1..templates.getValue(exam.id).length).toList(), rows.map { it[1].toInt() })
                for (row in rows) assertEquals(templates.getValue(exam.id)[row[1].toInt()-1], row[3].single())
                val kept = rows.filter { it[2] == "añadir" }
                val questions = ExtraerPreguntas.extraerPreguntas(category.copy(exams = listOf(exam)))
                assertEquals(counts.getValue(exam.id), questions.size)
                assertEquals(kept.map { it[1].toInt() }, questions.map { it.texto.substringBefore(')').toInt() })
                for ((q, row) in questions.zip(kept)) {
                    assertEquals(row[3].single(), q.respuestaCorrecta, "${exam.id}: ${row[1]}")
                    assertEquals(row[4], sha256((q.texto + "\n" + q.opciones.joinToString("\n")).toByteArray()))
                    assertEquals(4, q.opciones.size)
                    assertTrue(q.opciones.all { it.length > 3 })
                    if (row[5] == "-") assertNull(q.imagenPath)
                    else {
                        val bytes = Res.readBytes(assertNotNull(q.imagenPath))
                        assertEquals(row[5], sha256(bytes))
                        assertEquals(listOf(137,80,78,71,13,10,26,10), bytes.take(8).map { it.toInt() and 255 })
                        imageCount++
                    }
                }
            }
        }
        assertEquals(7, imageCount)
    }

    @Test
    fun officialWordCorrectionsAndIndependentOfficeSelectionArePreserved() = runBlocking {
        val category = TestCategories.load().single { it.id == "INFORMATICA" }
        val word = ExtraerPreguntas.extraerPreguntas(category.copy(exams = category.exams.filter { it.id == "CAR_AUXILIAR_2025_WORD" }))
        assertEquals((1..25).filterNot { it in setOf(6,12) }, word.map { it.texto.substringBefore(')').toInt() })
        val corrected = word.single { it.texto.startsWith("17)") }
        assertTrue(corrected.texto.contains("la sección actual termina en una página impar"))
        assertEquals('c', corrected.respuestaCorrecta)
        val formatted = word.single { it.texto.startsWith("8)") }
        assertEquals('a', formatted.respuestaCorrecta)
        assertNotNull(formatted.imagenPath)
        assertTrue(formatted.opcionA().contains("negrita y cursiva"))
        assertTrue(formatted.opcionC().contains("sin negrita ni cursiva"))

        val expected = mapOf(
            "UV_2022_OFIMATICA_S1" to listOf(2,5,6,9,11,15,20),
            "UV_2022_OFIMATICA_S2" to listOf(11,12,13,14,15,20),
            "UV_2022_OFIMATICA_S3" to listOf(3,5,7,8,10,13)
        )
        for ((id, numbers) in expected) {
            val questions = ExtraerPreguntas.extraerPreguntas(category.copy(exams = category.exams.filter { it.id == id }))
            assertEquals(numbers, questions.map { it.texto.substringBefore(')').toInt() })
            assertTrue(questions.none { it.texto.contains("anterior pregunta", ignoreCase = true) || it.texto.contains("siguiente imagen", ignoreCase = true) })
            if (id.endsWith("S1")) {
                val saveAs = questions.single { it.texto.startsWith("20)") }
                assertTrue(saveAs.texto.endsWith("del cuadro de diálogo “Guardar como”?"))
                assertEquals('c', saveAs.respuestaCorrecta)
            }
        }
        val practical = TestCategories.load().single { it.id == "SUPUESTOS_ADMINISTRATIVO_CAR" }
        assertEquals(58, ExtraerPreguntas.extraerPreguntas(practical).size)
        assertTrue(practical.exams.none { it.id in counts })
    }
}
