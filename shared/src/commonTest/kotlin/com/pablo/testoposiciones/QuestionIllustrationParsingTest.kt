package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.ExtraerPreguntas
import kotlin.test.*

class QuestionIllustrationParsingTest {
    private val question = """
        1) Identifique el botón.
        @imagen: imagenes/boton.png
        a) Copiar
        b) Pegar
        c) Cortar
        d) Guardar

        2) Pregunta sin imagen.
        a) Uno
        b) Dos
        c) Tres
        d) Cuatro
    """.trimIndent()

    @Test
    fun imageBelongsOnlyToItsQuestionAndDoesNotBecomeOptionText() {
        val parsed = parse(question)
        assertEquals("files/categoria/examen/imagenes/boton.png", parsed[0].imagenPath)
        assertEquals("1) Identifique el botón.", parsed[0].texto)
        assertEquals("a) Copiar", parsed[0].opcionA())
        assertEquals("d) Guardar", parsed[0].opcionD())
        assertNull(parsed[1].imagenPath)
        assertNotEquals(parsed[0], parsed[0].copy(imagenPath = null))
    }

    @Test
    fun invalidOrMisplacedImageMetadataFailsWithLocation() {
        for (text in listOf(
            "@imagen: imagenes/boton.png\n$question",
            question.replace("imagenes/boton.png", "../boton.png"),
            question.replace("@imagen: imagenes/boton.png", "@imagen: imagenes/boton.png\n@imagen: imagenes/otro.png")
        )) {
            val error = assertFailsWith<IllegalStateException> { parse(text) }
            assertTrue(error.message.orEmpty().startsWith("files/categoria/examen/Preguntas.txt:"))
        }
    }

    @Test
    fun numberingMismatchStillFailsInsteadOfShiftingAnswers() {
        assertFailsWith<IllegalStateException> { parse(question, "1b\n3d") }
    }

    private fun parse(text: String, answers: String = "1b\n2d") = ExtraerPreguntas.parsePreguntas(
        text, answers, "files/categoria/examen/Preguntas.txt", "files/categoria/examen/Respuestas.txt"
    )
}
