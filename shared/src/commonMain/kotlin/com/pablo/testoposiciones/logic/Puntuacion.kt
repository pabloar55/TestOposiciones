package com.pablo.testoposiciones.logic

import kotlin.math.roundToInt

/** Nota sobre 10 con dos decimales. Cada fallo resta un cuarto de acierto; las en blanco no restan. */
internal fun calcularPuntuacion(correctas: Int, incorrectas: Int, total: Int): Double {
    if (total == 0) return 0.0
    val conDescuento = (correctas - incorrectas * 0.25).coerceAtLeast(0.0)
    val sobre10 = (conDescuento * 10) / total
    return (sobre10 * 100).roundToInt() / 100.0
}

/** "7,25", "7,5" o "10": coma decimal y sin ceros sobrantes. */
internal fun formatNota(nota: Double): String {
    val centesimas = (nota * 100).roundToInt()
    val enteros = centesimas / 100
    val decimales = (centesimas % 100).toString().padStart(2, '0').trimEnd('0')
    return if (decimales.isEmpty()) "$enteros" else "$enteros,$decimales"
}
