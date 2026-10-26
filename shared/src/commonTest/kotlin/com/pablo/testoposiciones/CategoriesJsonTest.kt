package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.TestCategories
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CategoriesJsonTest {

    @Test
    fun leeCategoriasConExamenesOConPreguntasSueltas() {
        val definitions = TestCategories.parseCategoryDefinitions(
            """
            [
              { "id": "A", "nombre": "Con exámenes", "carpeta": "a", "examenes": "examenes.json",
                "versionProgreso": " v2 " },
              { "id": "B", "nombre": "Suelta", "carpeta": "b", "preguntas": "P.txt", "respuestas": "R.txt" }
            ]
            """,
            "categories.json"
        )

        assertEquals(2, definitions.size)
        assertEquals("examenes.json", definitions[0].examsFile)
        assertEquals("v2", definitions[0].progressVersion)
        assertNull(definitions[1].examsFile)
        assertEquals("P.txt", definitions[1].questionsFile)
        assertEquals("", definitions[1].progressVersion)
    }

    @Test
    fun respetaComillasYBarrasEscapadas() {
        val exams = TestCategories.parseExams(
            """[{ "id": "E", "nombre": "Examen \"oficial\" 1\/2", "carpeta": "e",
                 "preguntas": "P.txt", "respuestas": "R.txt" }]""",
            "examenes.json"
        )
        assertEquals("Examen \"oficial\" 1/2", exams.single().displayName)
    }

    @Test
    fun unCampoQueFaltaIndicaCualYEnQueFichero() {
        val error = assertFailsWith<IllegalArgumentException> {
            TestCategories.parseExams(
                """[{ "id": "E", "nombre": "  ", "carpeta": "e", "preguntas": "P.txt", "respuestas": "R.txt" }]""",
                "files/x/examenes.json"
            )
        }
        assertEquals("El examen 1 de files/x/examenes.json no tiene el campo 'nombre'.", error.message)
    }

    // Con el lector anterior, basado en expresiones regulares, estos casos se ignoraban sin avisar.
    @Test
    fun unCampoMalEscritoDaError() {
        val error = assertFailsWith<IllegalArgumentException> {
            TestCategories.parseCategoryDefinitions(
                """[{ "id": "A", "nombre": "A", "carpeta": "a", "preguntass": "P.txt", "respuestas": "R.txt" }]""",
                "categories.json"
            )
        }
        assertTrue(error.message!!.startsWith("El archivo categories.json no tiene un formato valido"))
    }

    @Test
    fun unValorQueNoEsTextoDaError() {
        assertFailsWith<IllegalArgumentException> {
            TestCategories.parseCategoryDefinitions(
                """[{ "id": "A", "nombre": "A", "carpeta": "a", "examenes": "e.json", "versionProgreso": 2 }]""",
                "categories.json"
            )
        }
    }

    @Test
    fun noSePuedenMezclarExamenesConPreguntasSueltas() {
        val error = assertFailsWith<IllegalArgumentException> {
            TestCategories.parseCategoryDefinitions(
                """[{ "id": "A", "nombre": "A", "carpeta": "a", "examenes": "e.json", "preguntas": "P.txt" }]""",
                "categories.json"
            )
        }
        assertEquals(
            "La categoria 1 de categories.json no puede mezclar 'examenes' con 'preguntas' o 'respuestas'.",
            error.message
        )
    }

    @Test
    fun idsDeExamenDuplicadosDanError() {
        assertFailsWith<IllegalArgumentException> {
            TestCategories.parseExams(
                """[
                  { "id": "E", "nombre": "1", "carpeta": "a", "preguntas": "P.txt", "respuestas": "R.txt" },
                  { "id": "E", "nombre": "2", "carpeta": "b", "preguntas": "P.txt", "respuestas": "R.txt" }
                ]""",
                "examenes.json"
            )
        }
    }
}
