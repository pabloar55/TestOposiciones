package com.pablo.testapp.logic

import com.pablo.testapp.model.Pregunta
import com.pablo.testapp.model.TestCategory
import org.jetbrains.compose.resources.ExperimentalResourceApi
import testapp.shared.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
object ExtraerPreguntas {
    suspend fun extraerPreguntas(category: TestCategory): List<Pregunta> {
        val preguntasPath = "files/${category.folderName}/${category.questionsFile}"
        val respuestasPath = "files/${category.folderName}/${category.answersFile}"

        val preguntasText = Res.readBytes(preguntasPath).decodeToString()
        val respuestasText = Res.readBytes(respuestasPath).decodeToString()

        val preguntasLines = preguntasText.lines()
        val respuestasLines = respuestasText.lines()

        val preguntas = mutableListOf<Pregunta>()
        var texto: String? = null
        var questionNumber: Int? = null
        var questionLine = 0
        val opciones = arrayOfNulls<String>(4)
        val questionNumbers = mutableSetOf<Int>()
        val respuestas = parseRespuestas(respuestasLines, respuestasPath)

        for ((lineIndex, linea) in preguntasLines.withIndex()) {
            val lineNumber = lineIndex + 1
            val trimmed = linea.trim()
            when {
                trimmed.matches(QUESTION_REGEX) -> {
                    if (texto != null) {
                        fail(preguntasPath, questionLine, "La pregunta $questionNumber esta incompleta antes de empezar otra.")
                    }
                    questionNumber = trimmed.substringBefore(")").toInt()
                    if (!questionNumbers.add(questionNumber)) {
                        fail(preguntasPath, lineNumber, "La pregunta $questionNumber esta duplicada.")
                    }
                    questionLine = lineNumber
                    texto = linea
                    for (i in opciones.indices) opciones[i] = null
                }
                trimmed.startsWith("a)") -> setOption(opciones, 0, linea, texto, preguntasPath, lineNumber)
                trimmed.startsWith("b)") -> setOption(opciones, 1, linea, texto, preguntasPath, lineNumber)
                trimmed.startsWith("c)") -> setOption(opciones, 2, linea, texto, preguntasPath, lineNumber)
                trimmed.startsWith("d)") -> {
                    setOption(opciones, 3, linea, texto, preguntasPath, lineNumber)
                    val t = texto
                    val number = questionNumber
                    if (t == null || number == null) {
                        fail(preguntasPath, lineNumber, "Hay opciones antes de una pregunta.")
                    }
                    val missing = opciones.indexOfFirst { it == null }
                    if (missing >= 0) {
                        fail(preguntasPath, questionLine, "La pregunta $number no tiene la opcion ${('a' + missing)}).")
                    }
                    val opcion = respuestas[number]
                        ?: fail(respuestasPath, 0, "Falta la respuesta de la pregunta $number.")

                    preguntas.add(Pregunta(t, arrayOf(opciones[0]!!, opciones[1]!!, opciones[2]!!, opciones[3]!!), opcion))
                    texto = null
                    questionNumber = null
                    questionLine = 0
                    for (i in opciones.indices) opciones[i] = null
                }
            }
        }

        if (texto != null) {
            fail(preguntasPath, questionLine, "La ultima pregunta esta incompleta.")
        }
        if (preguntas.isEmpty()) {
            fail(preguntasPath, 0, "No se ha encontrado ninguna pregunta valida.")
        }

        val missingAnswers = questionNumbers.filter { it !in respuestas.keys }.sorted()
        if (missingAnswers.isNotEmpty()) {
            fail(respuestasPath, 0, "Faltan respuestas para las preguntas: ${missingAnswers.take(10).joinToString()}.")
        }

        val extraAnswers = respuestas.keys.filter { it !in questionNumbers }.sorted()
        if (extraAnswers.isNotEmpty()) {
            fail(respuestasPath, 0, "Hay respuestas sin pregunta: ${extraAnswers.take(10).joinToString()}.")
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

    private fun parseRespuestas(lines: List<String>, path: String): Map<Int, Char> {
        val respuestas = mutableMapOf<Int, Char>()
        for ((lineIndex, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue

            val match = ANSWER_REGEX.matchEntire(trimmed)
                ?: fail(path, lineIndex + 1, "La respuesta debe tener formato como '12c'.")

            val number = match.groupValues[1].toInt()
            val option = match.groupValues[2].first().lowercaseChar()
            if (number in respuestas) {
                fail(path, lineIndex + 1, "La respuesta de la pregunta $number esta duplicada.")
            }
            respuestas[number] = option
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

    private val QUESTION_REGEX = Regex("[0-9]+\\).*")
    private val ANSWER_REGEX = Regex("([0-9]+)\\s*([a-dA-D])")
}
