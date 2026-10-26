package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.Screen
import com.pablo.testoposiciones.logic.TestSession
import com.pablo.testoposiciones.model.Pregunta
import com.pablo.testoposiciones.model.TestCategory
import com.pablo.testoposiciones.model.TipoTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestSessionTest {

    private val category = TestCategory(id = "C", displayName = "C", folderName = "c", exams = emptyList())

    // La correcta de cada pregunta es la 'a'.
    private fun preguntas(n: Int) = List(n) { i ->
        Pregunta("$i) ¿Pregunta $i?", arrayOf("a) Sí", "b) No", "c) Quizá", "d) Nunca"), respuestaCorrecta = 'a')
    }

    private fun session(tipo: TipoTest, n: Int = 3, offset: Int = 0, total: Int = n) =
        TestSession(category, tipo, preguntas(n), offsetInicial = offset, totalEnCategoria = total)

    @Test
    fun conCorreccionInmediataLaRespuestaQuedaFijada() {
        val answered = session(TipoTest.ALEATORIO_30).answer('b')
        assertTrue(answered.answerLocked)
        assertSame(answered, answered.answer('a'))
        assertEquals('b', answered.respuestaActual)
    }

    @Test
    fun enBloquesSePuedeCambiarDeRespuesta() {
        val answered = session(TipoTest.BLOQUES_30).answer('b').answer('a')
        assertFalse(answered.answerLocked)
        assertEquals('a', answered.respuestaActual)
    }

    @Test
    fun laNavegacionNoSaleDeLosLimites() {
        val start = session(TipoTest.BLOQUES_30, n = 2)
        assertSame(start, start.previous())
        val last = start.next()
        assertTrue(last.isLast)
        assertSame(last, last.next())
        assertEquals(0, last.previous().currentIndex)
    }

    @Test
    fun lasRespuestasSeConservanAlVolverAtras() {
        val s = session(TipoTest.BLOQUES_30).answer('c').next().previous()
        assertEquals('c', s.respuestaActual)
    }

    @Test
    fun enSeguidasSeNumeraRespectoATodaLaCategoria() {
        val s = session(TipoTest.SECUENCIAL, n = 3, offset = 40, total = 43).next()
        assertEquals(42, s.displayQuestionNumber)
        assertEquals(43, s.displayQuestionTotal)
        assertEquals(42, s.resumeQuestionNumber)
        assertEquals(44, s.nextQuestionNumber)
        assertTrue(s.completesCategory)
    }

    @Test
    fun enLosDemasModosSeNumeraDentroDelTest() {
        val s = session(TipoTest.BLOQUES_30, n = 30, offset = 30, total = 416).next()
        assertEquals(2, s.displayQuestionNumber)
        assertEquals(30, s.displayQuestionTotal)
        assertEquals(61, s.nextQuestionNumber)
        assertFalse(s.completesCategory)
    }

    @Test
    fun repasarFalladasNuncaCompletaLaCategoria() {
        assertFalse(session(TipoTest.FALLADAS, n = 3, total = 3).completesCategory)
    }

    @Test
    fun elHistorialMarcaAciertosFallosYBlancos() {
        val historial = session(TipoTest.BLOQUES_30).answer('a').next().answer('d').historial()
        assertTrue(historial[0].esCorrecta)
        assertEquals('d', historial[1].respuestaUsuario)
        assertFalse(historial[1].esCorrecta)
        assertNull(historial[2].respuestaUsuario)
        assertFalse(historial[2].esCorrecta)
    }

    @Test
    fun losResultadosNoCuentanLasEnBlancoComoFallos() {
        val s = session(TipoTest.BLOQUES_30, n = 4).answer('a').next().answer('b').next().answer('a')
        val results = Screen.Results(
            category = category,
            tipoTest = TipoTest.BLOQUES_30,
            historial = s.historial(),
            segundos = 0,
            categoryCompleted = false,
            failedRemaining = 0
        )
        assertEquals(4, results.total)
        assertEquals(2, results.correctas)
        assertEquals(1, results.incorrectas)
    }

    @Test
    fun elCronometroAvanzaDeUnoEnUno() {
        assertEquals(2, session(TipoTest.ALEATORIO_30).tick().tick().secondsElapsed)
    }

    @Test
    fun unTestSinPreguntasNoEsValido() {
        assertFailsWith<IllegalArgumentException> {
            TestSession(category, TipoTest.FALLADAS, emptyList(), offsetInicial = 0, totalEnCategoria = 0)
        }
    }
}
