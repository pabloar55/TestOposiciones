package com.pablo.testoposiciones.model

data class ResultadoPregunta(
    val pregunta: Pregunta,
    val respuestaUsuario: Char,
    val esCorrecta: Boolean
)
