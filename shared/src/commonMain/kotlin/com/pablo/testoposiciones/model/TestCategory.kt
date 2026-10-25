package com.pablo.testoposiciones.model

data class TestCategory(
    val id: String,
    val displayName: String,
    val folderName: String,
    val exams: List<TestExam>,
    val progressVersion: String = ""
) {
    // El avance por posición solo es válido para una misma selección de preguntas.
    val progressStorageId: String
        get() = if (progressVersion.isBlank()) id else "${id}_$progressVersion"
}

data class TestExam(
    val id: String,
    val displayName: String,
    val folderName: String,
    val questionsFile: String,
    val answersFile: String
)
