package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.progressStartIndex
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
}
