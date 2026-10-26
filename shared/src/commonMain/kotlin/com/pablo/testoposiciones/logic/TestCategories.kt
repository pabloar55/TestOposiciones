package com.pablo.testoposiciones.logic

import com.pablo.testoposiciones.model.TestCategory
import com.pablo.testoposiciones.model.TestExam
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import testoposiciones.shared.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
object TestCategories {
    private const val CATEGORIES_PATH = "files/categories.json"

    suspend fun load(): List<TestCategory> {
        val text = Res.readBytes(CATEGORIES_PATH).decodeToString()
        val definitions = parseCategoryDefinitions(text, CATEGORIES_PATH)
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
                exams = exams,
                progressVersion = definition.progressVersion
            )
        }

        require(categories.isNotEmpty()) { "El archivo $CATEGORIES_PATH no contiene categorias." }
        val duplicatedIds = categories.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(duplicatedIds.isEmpty()) {
            "Hay ids de categorias duplicados en $CATEGORIES_PATH: ${duplicatedIds.joinToString()}."
        }
        return categories
    }

    internal fun parseCategoryDefinitions(text: String, path: String): List<CategoryDefinition> {
        return decode<List<CategoryJson>>(text, path).mapIndexed { index, raw ->
            val item = "La categoria ${index + 1}"
            val examsFile = raw.examenes.optional()
            val questionsFile = raw.preguntas.optional()
            val answersFile = raw.respuestas.optional()

            if (examsFile != null) {
                require(questionsFile == null && answersFile == null) {
                    "$item de $path no puede mezclar 'examenes' con 'preguntas' o 'respuestas'."
                }
            } else {
                require(questionsFile != null && answersFile != null) {
                    "$item de $path debe indicar 'examenes' o la pareja 'preguntas' y 'respuestas'."
                }
            }

            CategoryDefinition(
                id = raw.id.required("id", item, path),
                displayName = raw.nombre.required("nombre", item, path),
                folderName = raw.carpeta.required("carpeta", item, path),
                questionsFile = questionsFile,
                answersFile = answersFile,
                examsFile = examsFile,
                progressVersion = raw.versionProgreso.optional().orEmpty()
            )
        }
    }

    internal fun parseExams(text: String, path: String): List<TestExam> {
        val exams = decode<List<ExamJson>>(text, path).mapIndexed { index, raw ->
            val item = "El examen ${index + 1}"
            TestExam(
                id = raw.id.required("id", item, path),
                displayName = raw.nombre.required("nombre", item, path),
                folderName = raw.carpeta.required("carpeta", item, path),
                questionsFile = raw.preguntas.required("preguntas", item, path),
                answersFile = raw.respuestas.required("respuestas", item, path)
            )
        }

        require(exams.isNotEmpty()) { "El archivo $path no contiene examenes." }
        val duplicatedIds = exams.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(duplicatedIds.isEmpty()) {
            "Hay ids de examenes duplicados en $path: ${duplicatedIds.joinToString()}."
        }
        return exams
    }

    // Estricto a propósito: un campo mal escrito ("preguntass") o un valor que no sea texto
    // debe dar error al cargar, no ignorarse sin avisar.
    private val json = Json { ignoreUnknownKeys = false }

    private inline fun <reified T> decode(text: String, path: String): T = try {
        json.decodeFromString<T>(text)
    } catch (e: SerializationException) {
        throw IllegalArgumentException("El archivo $path no tiene un formato valido: ${e.message}", e)
    }

    private fun String?.optional(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun String?.required(name: String, item: String, path: String): String {
        val value = optional()
        require(value != null) { "$item de $path no tiene el campo '$name'." }
        return value
    }

    // Reflejan los ficheros tal cual, con sus nombres en español. Todo es opcional aquí
    // para poder dar un mensaje propio cuando falta un campo.
    @Serializable
    private data class CategoryJson(
        val id: String? = null,
        val nombre: String? = null,
        val carpeta: String? = null,
        val preguntas: String? = null,
        val respuestas: String? = null,
        val examenes: String? = null,
        val versionProgreso: String? = null
    )

    @Serializable
    private data class ExamJson(
        val id: String? = null,
        val nombre: String? = null,
        val carpeta: String? = null,
        val preguntas: String? = null,
        val respuestas: String? = null
    )

    internal data class CategoryDefinition(
        val id: String,
        val displayName: String,
        val folderName: String,
        val questionsFile: String?,
        val answersFile: String?,
        val examsFile: String?,
        val progressVersion: String
    )
}
