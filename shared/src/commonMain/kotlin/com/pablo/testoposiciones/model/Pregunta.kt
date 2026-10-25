package com.pablo.testoposiciones.model

data class Pregunta(
    val texto: String,
    val opciones: Array<String>,
    val respuestaCorrecta: Char,
    val imagenPath: String? = null
) {
    fun opcionA() = opciones[0]
    fun opcionB() = opciones[1]
    fun opcionC() = opciones[2]
    fun opcionD() = opciones[3]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Pregunta
        return texto == other.texto && opciones.contentEquals(other.opciones) &&
            respuestaCorrecta == other.respuestaCorrecta && imagenPath == other.imagenPath
    }

    override fun hashCode(): Int {
        var result = texto.hashCode()
        result = 31 * result + opciones.contentHashCode()
        result = 31 * result + respuestaCorrecta.hashCode()
        result = 31 * result + (imagenPath?.hashCode() ?: 0)
        return result
    }
}
