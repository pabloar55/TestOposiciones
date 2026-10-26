package com.pablo.testoposiciones.logic

import com.pablo.testoposiciones.model.Pregunta
import com.pablo.testoposiciones.model.TestCategory
import com.pablo.testoposiciones.model.TestExam
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.ExperimentalResourceApi
import testoposiciones.shared.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
object ExtraerPreguntas {
    // Los ficheros van dentro de la app y no cambian mientras se ejecuta, así que basta con
    // leerlos una vez. Sin caché, cada vuelta al inicio volvía a parsear toda la categoría.
    // La clave es la categoría completa (con sus exámenes) porque los tests piden subconjuntos.
    private val cache = mutableMapOf<TestCategory, List<Pregunta>>()
    private val cacheMutex = Mutex()

    suspend fun extraerPreguntas(category: TestCategory): List<Pregunta> = cacheMutex.withLock {
        cache.getOrPut(category) {
            category.exams.flatMap { exam -> extraerPreguntas(category, exam) }
        }
    }

    private suspend fun extraerPreguntas(category: TestCategory, exam: TestExam): List<Pregunta> {
        val basePath = listOf("files", category.folderName, exam.folderName)
            .filter { it.isNotBlank() }
            .joinToString("/")
        val preguntasPath = "$basePath/${exam.questionsFile}"
        val respuestasPath = "$basePath/${exam.answersFile}"

        val preguntasText = Res.readBytes(preguntasPath).decodeToString()
        val respuestasText = Res.readBytes(respuestasPath).decodeToString()

        return parsePreguntas(preguntasText, respuestasText, preguntasPath, respuestasPath)
    }

    internal fun parsePreguntas(
        preguntasText: String,
        respuestasText: String,
        preguntasPath: String,
        respuestasPath: String
    ): List<Pregunta> {
        val preguntasLines = preguntasText.lines()
        val respuestasLines = respuestasText.lines()

        val preguntas = mutableListOf<Pregunta>()
        var texto: String? = null
        var questionNumber: Int? = null
        var questionLine = 0
        val opciones = arrayOfNulls<String>(4)
        // Las respuestas se emparejan por ORDEN (posicion), no por numero, para
        // que funcione aunque la numeracion de las preguntas se reinicie por bloques.
        val respuestas = parseRespuestas(respuestasLines, respuestasPath)
        var answerIndex = 0
        var currentOptionIndex: Int? = null
        var imagenPath: String? = null

        fun finishQuestion() {
            val questionText = texto ?: return
            val number = questionNumber
                ?: fail(preguntasPath, questionLine, "La pregunta no tiene numero.")
            val missing = opciones.indexOfFirst { it == null }
            if (missing >= 0) {
                fail(preguntasPath, questionLine, "La pregunta $number no tiene la opcion ${('a' + missing)}).")
            }
            if (answerIndex >= respuestas.size) {
                fail(
                    preguntasPath,
                    questionLine,
                    "Hay mas preguntas que respuestas: falta la respuesta de la pregunta $number."
                )
            }
            val respuesta = respuestas[answerIndex]
            if (respuesta.number != number) {
                fail(
                    respuestasPath,
                    respuesta.line,
                    "Descuadre en la posicion ${answerIndex + 1}: la pregunta es la $number pero la respuesta declarada es la ${respuesta.number}."
                )
            }

            preguntas.add(
                Pregunta(
                    texto = questionText,
                    opciones = arrayOf(opciones[0]!!, opciones[1]!!, opciones[2]!!, opciones[3]!!),
                    respuestaCorrecta = respuesta.option,
                    imagenPath = imagenPath
                )
            )
            answerIndex++
            texto = null
            questionNumber = null
            questionLine = 0
            currentOptionIndex = null
            imagenPath = null
            for (i in opciones.indices) opciones[i] = null
        }

        for ((lineIndex, linea) in preguntasLines.withIndex()) {
            val lineNumber = lineIndex + 1
            val trimmed = linea.trim()
            when {
                trimmed.isBlank() -> Unit
                trimmed.startsWith("@imagen:") -> {
                    if (texto == null || imagenPath != null) {
                        fail(preguntasPath, lineNumber, "La imagen debe pertenecer a una pregunta y no estar duplicada.")
                    }
                    val relativePath = trimmed.substringAfter(":").trim()
                    if (!IMAGE_REGEX.matches(relativePath)) {
                        fail(preguntasPath, lineNumber, "La imagen debe tener formato 'imagenes/nombre.png'.")
                    }
                    imagenPath = "${preguntasPath.substringBeforeLast('/')}/$relativePath"
                }
                trimmed.matches(QUESTION_REGEX) -> {
                    finishQuestion()
                    questionNumber = trimmed.substringBefore(")").toInt()
                    questionLine = lineNumber
                    texto = trimmed
                    currentOptionIndex = null
                    for (i in opciones.indices) opciones[i] = null
                }
                OPTION_REGEX.matches(trimmed) -> {
                    val optionIndex = trimmed.first().lowercaseChar() - 'a'
                    setOption(opciones, optionIndex, trimmed, texto, preguntasPath, lineNumber)
                    currentOptionIndex = optionIndex
                }
                isSectionHeading(trimmed) -> Unit
                texto != null -> {
                    val optionIndex = currentOptionIndex
                    if (optionIndex == null) {
                        texto = "$texto $trimmed"
                    } else {
                        opciones[optionIndex] = "${opciones[optionIndex]} $trimmed"
                    }
                }
                else -> fail(preguntasPath, lineNumber, "Texto fuera de una pregunta: '$trimmed'.")
            }
        }

        finishQuestion()
        if (preguntas.isEmpty()) {
            fail(preguntasPath, 0, "No se ha encontrado ninguna pregunta valida.")
        }

        if (answerIndex < respuestas.size) {
            val sobrantes = respuestas.size - answerIndex
            fail(respuestasPath, respuestas[answerIndex].line, "Hay $sobrantes respuesta(s) de mas: ${respuestas.size} respuestas para ${preguntas.size} preguntas.")
        }

        return preguntas
    }

    private fun setOption(
        opciones: Array<String?>,
        index: Int,
        linea: String,
        currentQuestion: String?,
        path: String,
        lineNumber: Int
    ) {
        if (currentQuestion == null) {
            fail(path, lineNumber, "Hay una opcion antes de una pregunta.")
        }
        if (opciones[index] != null) {
            fail(path, lineNumber, "La opcion ${('a' + index)}) esta duplicada.")
        }
        opciones[index] = linea
    }

    private data class Respuesta(val number: Int, val option: Char, val line: Int)

    private fun parseRespuestas(lines: List<String>, path: String): List<Respuesta> {
        val respuestas = mutableListOf<Respuesta>()
        for ((lineIndex, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue

            val match = ANSWER_REGEX.matchEntire(trimmed)
                ?: fail(path, lineIndex + 1, "La respuesta debe tener formato como '12c'.")

            val number = match.groupValues[1].toInt()
            val option = match.groupValues[2].first().lowercaseChar()
            respuestas.add(Respuesta(number, option, lineIndex + 1))
        }
        if (respuestas.isEmpty()) {
            fail(path, 0, "No se ha encontrado ninguna respuesta valida.")
        }
        return respuestas
    }

    private fun fail(path: String, line: Int, message: String): Nothing {
        val location = if (line > 0) "$path:$line" else path
        throw IllegalStateException("$location - $message")
    }

    private fun isSectionHeading(text: String): Boolean {
        val letters = text.filter { it.isLetter() }
        return letters.isNotEmpty() && letters.all { it.isUpperCase() }
    }

    private val QUESTION_REGEX = Regex("[0-9]+\\).*")
    private val OPTION_REGEX = Regex("[a-dA-D]\\).*")
    private val ANSWER_REGEX = Regex("([0-9]+)\\s*([a-dA-D])")
    private val IMAGE_REGEX = Regex("imagenes/[a-z0-9_]+\\.png")
}
