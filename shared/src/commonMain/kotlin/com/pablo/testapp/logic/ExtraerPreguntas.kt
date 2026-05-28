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
        val respuestasLines = respuestasText.lines().filter { it.isNotBlank() }

        val preguntas = mutableListOf<Pregunta>()
        var texto: String? = null
        val opciones = arrayOfNulls<String>(4)
        var respuestaIndex = 0

        for (linea in preguntasLines) {
            val trimmed = linea.trim()
            when {
                trimmed.matches(Regex("[0-9]+\\).*")) -> texto = linea
                trimmed.startsWith("a)") -> opciones[0] = linea
                trimmed.startsWith("b)") -> opciones[1] = linea
                trimmed.startsWith("c)") -> opciones[2] = linea
                trimmed.startsWith("d)") -> {
                    opciones[3] = linea
                    val t = texto
                    if (t != null && opciones.all { it != null } && respuestaIndex < respuestasLines.size) {
                        val correcta = respuestasLines[respuestaIndex++]
                        val opcion = when {
                            correcta.contains("a") -> 'a'
                            correcta.contains("b") -> 'b'
                            correcta.contains("c") -> 'c'
                            correcta.contains("d") -> 'd'
                            else -> ' '
                        }
                        preguntas.add(Pregunta(t, arrayOf(opciones[0]!!, opciones[1]!!, opciones[2]!!, opciones[3]!!), opcion))
                    }
                    for (i in opciones.indices) opciones[i] = null
                }
            }
        }

        return preguntas
    }
}