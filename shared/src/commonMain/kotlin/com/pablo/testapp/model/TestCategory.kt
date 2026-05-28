package com.pablo.testapp.model

enum class TestCategory(
    val displayName: String,
    val folderName: String,
    val questionsFile: String,
    val answersFile: String
) {
    TER("TER", "TER", "PreguntasMixtas.txt", "RespuestasMixtas.txt"),
    CONSTITUCION("Constitucion", "constitucion", "PreguntasCE.txt", "Respuestas.txt")
}
