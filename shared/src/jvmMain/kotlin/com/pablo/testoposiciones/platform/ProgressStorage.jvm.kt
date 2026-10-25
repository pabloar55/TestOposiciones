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

    actual fun readDarkTheme(): Boolean? {
        val file = getThemeFile()
        return try {
            if (!file.exists()) null else file.readText().trim().toBooleanStrictOrNull()
        } catch (e: Exception) {
            null
        }
    }

    actual fun writeDarkTheme(dark: Boolean) {
        try {
            getThemeFile().writeText(dark.toString())
        } catch (e: Exception) {
            // ignore write errors
        }
    }

    actual fun readFailed(category: TestCategory): Set<String> {
        val file = getFailedFile(category)
        return try {
            if (!file.exists()) emptySet()
            else file.readLines().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    actual fun writeFailed(category: TestCategory, keys: Set<String>) {
        try {
            getFailedFile(category).writeText(keys.sorted().joinToString("\n"))
        } catch (e: Exception) {
            // ignore write errors
        }
    }

    // Va por id y no por progressStorageId: las falladas no dependen del orden de las preguntas.
    private fun getFailedFile(category: TestCategory): File {
        val safeId = category.id.replace(Regex("[^A-Za-z0-9_-]"), "_")
        return File(System.getProperty("user.home"), ".testoposiciones_failed_$safeId.txt")
    }

    private fun getThemeFile(): File {
        return File(System.getProperty("user.home"), ".testoposiciones_dark_theme.txt")
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
