package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.ExtraerPreguntas
import com.pablo.testoposiciones.logic.TestCategories
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class QuestionCacheDesktopTest {

    @Test
    fun laMismaCategoriaSoloSeLeeUnaVez() = runBlocking {
        val category = TestCategories.load().single { it.id == "AUXILIO_JUDICIAL" }
        val first = ExtraerPreguntas.extraerPreguntas(category)
        // Una categoría recargada es otra instancia con el mismo contenido: también debe acertar.
        val reloaded = TestCategories.load().single { it.id == "AUXILIO_JUDICIAL" }
        assertSame(first, ExtraerPreguntas.extraerPreguntas(reloaded))
    }

    @Test
    fun unSubconjuntoDeExamenesNoReutilizaLaCategoriaCompleta() = runBlocking {
        val category = TestCategories.load().single { it.id == "AUXILIO_JUDICIAL" }
        val all = ExtraerPreguntas.extraerPreguntas(category)
        val one = ExtraerPreguntas.extraerPreguntas(category.copy(exams = category.exams.take(1)))
        assertNotSame(all, one)
    }
}
