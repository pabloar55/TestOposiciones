package com.pablo.testoposiciones.ui

internal fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}

// Los ficheros traen "10) Enunciado" y "a) Opción". En pantalla el número original confunde
// (no coincide con el contador del test) y la letra ya va en su propio círculo.
private val QUESTION_NUMBER_PREFIX = Regex("^\\s*[0-9]+\\)\\s*")
private val OPTION_LETTER_PREFIX = Regex("^\\s*[a-dA-D]\\)\\s*")

internal fun enunciadoSinNumero(texto: String): String = texto.replaceFirst(QUESTION_NUMBER_PREFIX, "")

internal fun opcionSinLetra(texto: String): String = texto.replaceFirst(OPTION_LETTER_PREFIX, "")
