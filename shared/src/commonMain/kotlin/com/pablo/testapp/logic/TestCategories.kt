package com.pablo.testapp.logic

import com.pablo.testapp.model.TestCategory
import org.jetbrains.compose.resources.ExperimentalResourceApi
import testapp.shared.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
object TestCategories {
    suspend fun load(): List<TestCategory> {
        val text = Res.readBytes("files/categories.json").decodeToString()
        val categories = parseCategories(text)
        require(categories.isNotEmpty()) { "El archivo files/categories.json no contiene categorias." }
        val duplicatedIds = categories.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(duplicatedIds.isEmpty()) {
            "Hay ids de categorias duplicados en files/categories.json: ${duplicatedIds.joinToString()}."
        }
        return categories
    }

    private fun parseCategories(text: String): List<TestCategory> {
        val objectRegex = Regex("\\{([^{}]*)}", RegexOption.DOT_MATCHES_ALL)
        val fieldRegex = Regex("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"")

        return objectRegex.findAll(text).mapIndexed { index, match ->
            val fields = fieldRegex.findAll(match.groupValues[1]).associate {
                it.groupValues[1] to unescapeJsonString(it.groupValues[2])
            }

            fun required(name: String): String {
                val value = fields[name]?.trim().orEmpty()
                require(value.isNotEmpty()) {
                    "La categoria ${index + 1} de files/categories.json no tiene el campo '$name'."
                }
                return value
            }

            TestCategory(
                id = required("id"),
                displayName = required("nombre"),
                folderName = required("carpeta"),
                questionsFile = required("preguntas"),
                answersFile = required("respuestas")
            )
        }.toList()
    }

    private fun unescapeJsonString(value: String): String {
        return value
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\/", "/")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
    }
}
