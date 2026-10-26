package com.pablo.testoposiciones.logic

import com.pablo.testoposiciones.model.Pregunta
import com.pablo.testoposiciones.model.ResultadoPregunta
import com.pablo.testoposiciones.model.TestCategory
import com.pablo.testoposiciones.model.TipoTest
import com.pablo.testoposiciones.model.showsFeedback

/**
 * Un test en curso. Es inmutable: cada acción devuelve una sesión nueva y el ViewModel
 * decide qué guardar. Así las reglas del test se pueden probar sin corrutinas ni disco.
 */
data class TestSession(
    val category: TestCategory,
    val tipoTest: TipoTest,
    val preguntas: List<Pregunta>,
    /** Posición en la categoría (desde 0) de la primera pregunta. Solo cuenta en bloques y seguidas. */
    val offsetInicial: Int,
    val totalEnCategoria: Int,
    val currentIndex: Int = 0,
    val userAnswers: Map<Int, Char> = emptyMap(),
    val secondsElapsed: Int = 0
) {
    init {
        require(preguntas.isNotEmpty()) { "Un test necesita al menos una pregunta." }
    }

    val pregunta: Pregunta get() = preguntas[currentIndex]
    val respuestaActual: Char? get() = userAnswers[currentIndex]
    val isFirst: Boolean get() = currentIndex == 0
    val isLast: Boolean get() = currentIndex == preguntas.lastIndex

    // Con corrección inmediata la respuesta queda fijada una vez vista.
    val answerLocked: Boolean get() = tipoTest.showsFeedback && respuestaActual != null

    /** En "seguidas" se numera respecto a toda la categoría, que es lo que se retoma. */
    val displayQuestionNumber: Int
        get() = if (tipoTest == TipoTest.SECUENCIAL) offsetInicial + currentIndex + 1 else currentIndex + 1

    val displayQuestionTotal: Int
        get() = if (tipoTest == TipoTest.SECUENCIAL) totalEnCategoria else preguntas.size

    /** Número de pregunta (desde 1) por el que se retomaría si se sale ahora. */
    val resumeQuestionNumber: Int get() = offsetInicial + currentIndex + 1

    /** Número de pregunta (desde 1) por el que empieza el siguiente test tras terminar este. */
    val nextQuestionNumber: Int get() = offsetInicial + preguntas.size + 1

    val completesCategory: Boolean
        get() = tipoTest != TipoTest.FALLADAS && offsetInicial + preguntas.size >= totalEnCategoria

    fun answer(opcion: Char): TestSession =
        if (answerLocked) this else copy(userAnswers = userAnswers + (currentIndex to opcion))

    fun next(): TestSession = if (isLast) this else copy(currentIndex = currentIndex + 1)

    fun previous(): TestSession = if (isFirst) this else copy(currentIndex = currentIndex - 1)

    fun tick(): TestSession = copy(secondsElapsed = secondsElapsed + 1)

    fun resultadoActual(): ResultadoPregunta = resultado(currentIndex)

    fun historial(): List<ResultadoPregunta> = preguntas.indices.map(::resultado)

    private fun resultado(index: Int): ResultadoPregunta {
        val pregunta = preguntas[index]
        val respuesta = userAnswers[index]
        return ResultadoPregunta(pregunta, respuesta, esCorrecta = respuesta == pregunta.respuestaCorrecta)
    }
}
