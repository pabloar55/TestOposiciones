package com.pablo.testoposiciones.model

const val BLOCK_SIZE = 30

/** Por dónde va el usuario en una categoría, para mostrarlo en el inicio. */
data class CategoryProgress(
    val categoryId: String,
    val totalQuestions: Int,
    /** Posición (desde 0) por la que se retoma "Preguntas seguidas". */
    val sequentialStartIndex: Int,
    /** Posición (desde 0) por la que empieza el siguiente bloque. */
    val blocksStartIndex: Int,
    val failedCount: Int
) {
    val totalBlocks: Int
        get() = (totalQuestions + BLOCK_SIZE - 1) / BLOCK_SIZE

    val currentBlock: Int
        get() = blocksStartIndex / BLOCK_SIZE + 1
}
