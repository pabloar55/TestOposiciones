package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.failedKey
import com.pablo.testoposiciones.logic.updateFailedKeys
import com.pablo.testoposiciones.model.Pregunta
import com.pablo.testoposiciones.model.ResultadoPregunta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FailedQuestionsTest {

    private fun pregunta(texto: String = "¿Pregunta?", opcionA: String = "a") =
        Pregunta(texto, arrayOf(opcionA, "b", "c", "d"), respuestaCorrecta = 'a')

    @Test
    fun keyIsStableAcrossVersions() {
        // Si cambia, se pierden las falladas que los usuarios ya tienen guardadas.
        assertEquals("6be835976d36af3d", pregunta().failedKey())
    }

    @Test
    fun keyDependsOnTextAndOptions() {
        assertEquals(pregunta().failedKey(), pregunta().failedKey())
        assertNotEquals(pregunta().failedKey(), pregunta(texto = "¿Otra?").failedKey())
        assertNotEquals(pregunta().failedKey(), pregunta(opcionA = "x").failedKey())
    }

    @Test
    fun wrongAnswersAreAddedAndCorrectOnesRemoved() {
        val fallada = pregunta(texto = "Fallada")
        val acertada = pregunta(texto = "Acertada")
        val enBlanco = pregunta(texto = "En blanco")

        val updated = updateFailedKeys(
            keys = setOf(acertada.failedKey(), enBlanco.failedKey()),
            resultados = listOf(
                ResultadoPregunta(fallada, 'b', esCorrecta = false),
                ResultadoPregunta(acertada, 'a', esCorrecta = true),
                ResultadoPregunta(enBlanco, null, esCorrecta = false)
            )
        )

        assertEquals(setOf(fallada.failedKey(), enBlanco.failedKey()), updated)
    }
}
