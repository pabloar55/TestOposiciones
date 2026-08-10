package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.progressStartIndex
import com.pablo.testoposiciones.model.TestCategory
import com.pablo.testoposiciones.model.TipoTest
import com.pablo.testoposiciones.platform.ProgressStorage
import com.pablo.testoposiciones.platform.initProgressStorage
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedLogicAndroidHostTest {

    @Test
    fun completedProgressRestartsFromBeginning() {
        assertEquals(0, progressStartIndex(storedQuestionNumber = 31, total = 30))
    }

    @Test
    fun legacyProgressIsMigratedToBothProgressModes() {
        val directory = Files.createTempDirectory("testoposiciones-migration").toFile()
        val category = TestCategory(
            id = "auxilio_judicial",
            displayName = "Auxilio judicial",
            folderName = "auxilio_judicial",
            exams = emptyList()
        )
        val legacyFile = directory.resolve("testapp_progress_auxilio_judicial.txt")

        try {
            legacyFile.writeText("47")
            initProgressStorage(directory)

            assertEquals(47, ProgressStorage.readNumber(category, TipoTest.SECUENCIAL))
            assertEquals(47, ProgressStorage.readNumber(category, TipoTest.BLOQUES_30))
            assertTrue(directory.resolve("testoposiciones_progress_auxilio_judicial_secuencial.txt").exists())
            assertTrue(directory.resolve("testoposiciones_progress_auxilio_judicial_bloques_30.txt").exists())
            assertFalse(legacyFile.exists())
        } finally {
            directory.deleteRecursively()
        }
    }
}
