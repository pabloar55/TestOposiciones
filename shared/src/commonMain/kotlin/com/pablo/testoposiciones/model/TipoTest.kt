package com.pablo.testoposiciones.model

enum class TipoTest {
    ALEATORIO_30,
    SECUENCIAL,
    BLOQUES_30,
    FALLADAS,
}

// Corrección inmediata al responder; en estos modos la respuesta queda fijada una vez vista.
val TipoTest.showsFeedback: Boolean
    get() = this != TipoTest.BLOQUES_30
