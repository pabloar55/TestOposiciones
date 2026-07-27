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
        assertEquals(1, category.exams.size)
        assertEquals("Auxilio Judicial 2025 teórico", category.exams.single().displayName)

        val questions = ExtraerPreguntas.extraerPreguntas(category)
        assertEquals(104, questions.size)
        assertEquals('d', questions.first().respuestaCorrecta)
        assertEquals('c', questions.last().respuestaCorrecta)
    }
}
