package com.pablo.testapp.model

data class ResultadoPregunta(
    val pregunta: Pregunta,
    val respuestaUsuario: Char,
    val esCorrecta: Boolean
)
