package com.pablo.testoposiciones.platform

import java.io.File
import com.pablo.testoposiciones.model.TestCategory
import com.pablo.testoposiciones.model.TipoTest

actual object ProgressStorage {
    private fun getFile(category: TestCategory, mode: TipoTest): File {
        return File(
            System.getProperty("user.home"),
            ".testoposiciones_progress_${category.safeFileName()}_${mode.name.lowercase()}.txt"
        )
    }

    private fun getLegacyFile(category: TestCategory): File {
        return File(System.getProperty("user.home"), ".testapp_progress_${category.safeFileName()}.txt")
    }

    actual fun readNumber(category: TestCategory, mode: TipoTest): Int {
        migrateLegacyProgress(category)
        val file = getFile(category, mode)
        return try {
            if (!file.exists()) 1
            else file.readText().trim().toIntOrNull() ?: 1
        } catch (e: Exception) {
            1
        }
    }

    actual fun writeNumber(category: TestCategory, mode: TipoTest, num: Int) {
        val file = getFile(category, mode)
        try {
            file.writeText(num.toString())
        } catch (e: Exception) {
            // ignore write errors
        }
    }

    actual fun reset(category: TestCategory) {
        writeNumber(category, TipoTest.SECUENCIAL, 1)
        writeNumber(category, TipoTest.BLOQUES_30, 1)
        getLegacyFile(category).delete()
    }

    private fun migrateLegacyProgress(category: TestCategory) {
        val legacyFile = getLegacyFile(category)
        if (!legacyFile.exists()) return
        val value = try {
            legacyFile.readText().trim().toIntOrNull() ?: 1
        } catch (e: Exception) {
            1
        }
        writeNumber(category, TipoTest.SECUENCIAL, value)
        writeNumber(category, TipoTest.BLOQUES_30, value)
        legacyFile.delete()
    }

    private fun TestCategory.safeFileName(): String {
        return progressStorageId.replace(Regex("[^A-Za-z0-9_-]"), "_")
    }
}
