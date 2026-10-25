package com.pablo.testoposiciones

import com.pablo.testoposiciones.logic.TestCategories
import com.pablo.testoposiciones.model.TipoTest
import com.pablo.testoposiciones.platform.ProgressStorage
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CarProgressDesktopTest {
    @Test
    fun filteredBanksStartFreshAndKeepTheirOwnProgress() = runBlocking {
        val directory = Files.createTempDirectory("testoposiciones-car").toFile()
        val previousHome = System.getProperty("user.home")
        val categories = TestCategories.load().filter { it.id in setOf("ADMINISTRATIVO", "INFORMATICA") }
        try {
            System.setProperty("user.home", directory.absolutePath)
            for (category in categories) {
                val oldCategory = category.copy(progressVersion = "")
                for (mode in listOf(TipoTest.SECUENCIAL, TipoTest.BLOQUES_30)) {
                    ProgressStorage.writeNumber(oldCategory, mode, 200)
                    assertEquals(1, ProgressStorage.readNumber(category, mode))
                    ProgressStorage.writeNumber(category, mode, 17)
                    assertEquals(17, ProgressStorage.readNumber(category, mode))
                    assertEquals(200, ProgressStorage.readNumber(oldCategory, mode))
                }
                ProgressStorage.reset(category)
                assertEquals(1, ProgressStorage.readNumber(category, TipoTest.SECUENCIAL))
                assertEquals(1, ProgressStorage.readNumber(category, TipoTest.BLOQUES_30))
            }
        } finally {
            System.setProperty("user.home", previousHome)
            directory.deleteRecursively()
        }
    }
}
