package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.ExtraerPreguntas
import com.pablo.testoposiciones.logic.TestCategories
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedLogicDesktopTest {

    @Test
    fun loadsEveryAuxilioJudicialExam() = runBlocking {
        val category = TestCategories.load().single { it.id == "AUXILIO_JUDICIAL" }

        assertEquals("Auxilio judicial", category.displayName)
        assertEquals(
            listOf(
                "Auxilio Judicial 2025 teórico",
                "Auxilio Judicial septiembre 2024 - primera parte",
                "Auxilio Judicial 2024 incidencias - primera parte",
                "Auxilio Judicial marzo 2024 - primera parte"
            ),
            category.exams.map { it.displayName }
        )

        val questions = ExtraerPreguntas.extraerPreguntas(category)
        assertEquals(416, questions.size)
        assertEquals('d', questions.first().respuestaCorrecta)
        assertEquals(
            "1) Conforme a la Carta de derechos de los ciudadanos ante la Justicia, " +
                "las reclamaciones e indemnizaciones por error judicial se tramitarán:",
            questions[104].texto
        )
        assertEquals(
            "1) La elaboración de la Carta de derechos de los ciudadanos ante la Justicia, " +
                "atiende a los principios de:",
            questions[208].texto
        )
        assertEquals(
            "1) Los artículos 6 y 7 de la Constitución Española están recogidos en:",
            questions[312].texto
        )
        assertEquals('c', questions.last().respuestaCorrecta)
    }
}
