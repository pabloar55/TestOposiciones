package com.pablo.testoposiciones.model

data class ResultadoPregunta(
    val pregunta: Pregunta,
    /** null si se dejó en blanco. */
    val respuestaUsuario: Char?,
    val esCorrecta: Boolean
)
