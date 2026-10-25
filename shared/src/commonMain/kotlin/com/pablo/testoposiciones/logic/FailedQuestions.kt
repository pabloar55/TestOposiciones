package com.pablo.testoposiciones.logic

import com.pablo.testoposiciones.model.Pregunta
import com.pablo.testoposiciones.model.ResultadoPregunta
import com.pablo.testoposiciones.model.TestCategory
import com.pablo.testoposiciones.platform.ProgressStorage

object FailedQuestions {
    fun record(category: TestCategory, resultados: List<ResultadoPregunta>) {
        val stored = ProgressStorage.readFailed(category)
        val updated = updateFailedKeys(stored, resultados)
        if (updated != stored) ProgressStorage.writeFailed(category, updated)
    }

    /** Falladas que siguen existiendo en la categoría. Olvida las que ya no están. */
    fun pending(category: TestCategory, allPreguntas: List<Pregunta>): List<Pregunta> {
        val stored = ProgressStorage.readFailed(category)
        val pending = allPreguntas
            .filter { it.failedKey() in stored }
            .distinctBy { it.failedKey() }
        val existing = pending.mapTo(mutableSetOf()) { it.failedKey() }
        if (existing != stored) ProgressStorage.writeFailed(category, existing)
        return pending
    }

    fun count(category: TestCategory): Int = ProgressStorage.readFailed(category).size
}

/** Un fallo añade la pregunta a la lista y un acierto la quita. Las preguntas en blanco no cuentan. */
internal fun updateFailedKeys(keys: Set<String>, resultados: List<ResultadoPregunta>): Set<String> {
    val updated = keys.toMutableSet()
    for (resultado in resultados) {
        if (resultado.respuestaUsuario == ' ') continue
        val key = resultado.pregunta.failedKey()
        if (resultado.esCorrecta) updated.remove(key) else updated.add(key)
    }
    return updated
}

// Las preguntas no tienen id: se identifican por su contenido, así la lista sobrevive a
// reordenaciones y a cambios de versionProgreso. Si se corrige el texto, la antigua se olvida.
// FNV-1a de 64 bits para que no haya colisiones entre los miles de preguntas.
internal fun Pregunta.failedKey(): String {
    var hash = 0xcbf29ce484222325uL.toLong()
    val content = buildString {
        append(texto)
        opciones.forEach { append('\n').append(it) }
    }
    for (c in content) {
        hash = (hash xor c.code.toLong()) * 0x100000001b3L
    }
    return hash.toULong().toString(16).padStart(16, '0')
}
