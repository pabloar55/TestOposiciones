package com.pablo.testoposiciones.logic

import com.pablo.testoposiciones.model.TestCategory
import com.pablo.testoposiciones.model.TestExam
import org.jetbrains.compose.resources.ExperimentalResourceApi
import testoposiciones.shared.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
object TestCategories {
    suspend fun load(): List<TestCategory> {
        val categoriesPath = "files/categories.json"
        val text = Res.readBytes(categoriesPath).decodeToString()
        val definitions = parseCategoryDefinitions(text, categoriesPath)
        val categories = definitions.map { definition ->
            val exams = definition.examsFile?.let { examsFile ->
                val examsPath = "files/${definition.folderName}/$examsFile"
                val examsText = Res.readBytes(examsPath).decodeToString()
                parseExams(examsText, examsPath)
            } ?: listOf(
                TestExam(
                    id = definition.id,
                    displayName = definition.displayName,
                    folderName = "",
                    questionsFile = definition.questionsFile!!,
                    answersFile = definition.answersFile!!
                )
            )

            TestCategory(
                id = definition.id,
                displayName = definition.displayName,
                folderName = definition.folderName,
                exams = exams
            )
        }

        require(categories.isNotEmpty()) { "El archivo files/categories.json no contiene categorias." }
        val duplicatedIds = categories.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(duplicatedIds.isEmpty()) {
            "Hay ids de categorias duplicados en files/categories.json: ${duplicatedIds.joinToString()}."
        }
        return categories
    }

    private fun parseCategoryDefinitions(text: String, path: String): List<CategoryDefinition> {
        return parseObjects(text).mapIndexed { index, fields ->
            fun required(name: String): String = required(fields, name, "La categoria ${index + 1}", path)
            fun optional(name: String): String? = fields[name]?.trim()?.takeIf { it.isNotEmpty() }

            val examsFile = optional("examenes")
            val questionsFile = optional("preguntas")
            val answersFile = optional("respuestas")

            if (examsFile != null) {
                require(questionsFile == null && answersFile == null) {
                    "La categoria ${index + 1} de $path no puede mezclar 'examenes' con 'preguntas' o 'respuestas'."
                }
            } else {
                require(questionsFile != null && answersFile != null) {
                    "La categoria ${index + 1} de $path debe indicar 'examenes' o la pareja 'preguntas' y 'respuestas'."
                }
            }

            CategoryDefinition(
                id = required("id"),
                displayName = required("nombre"),
                folderName = required("carpeta"),
                questionsFile = questionsFile,
                answersFile = answersFile,
                examsFile = examsFile
            )
        }
    }

    private fun parseExams(text: String, path: String): List<TestExam> {
        val exams = parseObjects(text).mapIndexed { index, fields ->
            fun required(name: String): String = required(fields, name, "El examen ${index + 1}", path)

            TestExam(
                id = required("id"),
                displayName = required("nombre"),
                folderName = required("carpeta"),
                questionsFile = required("preguntas"),
                answersFile = required("respuestas")
            )
        }

        require(exams.isNotEmpty()) { "El archivo $path no contiene examenes." }
        val duplicatedIds = exams.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(duplicatedIds.isEmpty()) {
            "Hay ids de examenes duplicados en $path: ${duplicatedIds.joinToString()}."
        }
        return exams
    }

    private fun parseObjects(text: String): List<Map<String, String>> {
        val objectRegex = Regex("\\{([^{}]*)\\}", RegexOption.DOT_MATCHES_ALL)
        val fieldRegex = Regex("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"")

        return objectRegex.findAll(text).map { match ->
            fieldRegex.findAll(match.groupValues[1]).associate {
                it.groupValues[1] to unescapeJsonString(it.groupValues[2])
            }
        }.toList()
    }

    private fun required(
        fields: Map<String, String>,
        name: String,
        itemDescription: String,
        path: String
    ): String {
        val value = fields[name]?.trim().orEmpty()
        require(value.isNotEmpty()) {
            "$itemDescription de $path no tiene el campo '$name'."
        }
        return value
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

    private data class CategoryDefinition(
        val id: String,
        val displayName: String,
        val folderName: String,
        val questionsFile: String?,
        val answersFile: String?,
        val examsFile: String?
    )
}
