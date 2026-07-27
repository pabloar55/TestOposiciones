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
                "Auxilio Judicial septiembre 2024 - primera parte"
            ),
            category.exams.map { it.displayName }
        )

        val questions = ExtraerPreguntas.extraerPreguntas(category)
        assertEquals(208, questions.size)
        assertEquals('d', questions.first().respuestaCorrecta)
        assertEquals(
            "1) Conforme a la Carta de derechos de los ciudadanos ante la Justicia, " +
                "las reclamaciones e indemnizaciones por error judicial se tramitarán:",
            questions[104].texto
        )
        assertEquals('c', questions.last().respuestaCorrecta)
    }
}
