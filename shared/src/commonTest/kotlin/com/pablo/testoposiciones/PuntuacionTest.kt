package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.calcularPuntuacion
import com.pablo.testoposiciones.logic.formatNota
import com.pablo.testoposiciones.ui.enunciadoSinNumero
import com.pablo.testoposiciones.ui.formatTime
import com.pablo.testoposiciones.ui.opcionSinLetra
import kotlin.test.Test
import kotlin.test.assertEquals

class PuntuacionTest {

    @Test
    fun cadaFalloRestaUnCuartoDeAcierto() {
        assertEquals(10.0, calcularPuntuacion(correctas = 30, incorrectas = 0, total = 30))
        assertEquals(6.25, calcularPuntuacion(correctas = 20, incorrectas = 5, total = 30))
        assertEquals(5.0, calcularPuntuacion(correctas = 15, incorrectas = 0, total = 30))
    }

    @Test
    fun laNotaNoBajaDeCero() {
        assertEquals(0.0, calcularPuntuacion(correctas = 0, incorrectas = 30, total = 30))
        assertEquals(0.0, calcularPuntuacion(correctas = 0, incorrectas = 0, total = 0))
    }

    @Test
    fun laNotaSeEscribeConComaYSinCerosSobrantes() {
        assertEquals("6,25", formatNota(6.25))
        assertEquals("7,5", formatNota(7.5))
        assertEquals("10", formatNota(10.0))
        assertEquals("0", formatNota(0.0))
        assertEquals("0,08", formatNota(0.08))
    }

    @Test
    fun elTiempoSeMuestraEnMinutosYSegundos() {
        assertEquals("00:00", formatTime(0))
        assertEquals("02:05", formatTime(125))
        assertEquals("61:01", formatTime(3661))
    }

    @Test
    fun seQuitanElNumeroYLaLetraDelFichero() {
        assertEquals("Según el artículo 9…", enunciadoSinNumero("10) Según el artículo 9…"))
        assertEquals("Texto sin número", enunciadoSinNumero("Texto sin número"))
        assertEquals("Los Tribunales", opcionSinLetra("a) Los Tribunales"))
        assertEquals("Ley 39/2015 a) bis", opcionSinLetra("D)Ley 39/2015 a) bis"))
    }
}
