package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.progressStartIndex
import com.pablo.testoposiciones.model.CategoryProgress
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCommonTest {

    @Test
    fun progressStartsAtStoredQuestion() {
        assertEquals(0, progressStartIndex(storedQuestionNumber = 1, total = 60))
        assertEquals(29, progressStartIndex(storedQuestionNumber = 30, total = 60))
    }

    @Test
    fun completedOrInvalidProgressRestartsFromBeginning() {
        assertEquals(0, progressStartIndex(storedQuestionNumber = 61, total = 60))
        assertEquals(0, progressStartIndex(storedQuestionNumber = 0, total = 60))
        assertEquals(0, progressStartIndex(storedQuestionNumber = 1, total = 0))
    }

    @Test
    fun blocksAreCountedFromStoredPosition() {
        val progress = CategoryProgress(
            categoryId = "CONSTITUCION",
            totalQuestions = 345,
            sequentialStartIndex = 0,
            blocksStartIndex = 90,
            failedCount = 0
        )
        assertEquals(12, progress.totalBlocks)
        assertEquals(4, progress.currentBlock)
    }
}
