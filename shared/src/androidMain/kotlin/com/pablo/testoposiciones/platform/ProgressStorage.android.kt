package com.pablo.testoposiciones.platform

import com.pablo.testoposiciones.model.TestCategory
import com.pablo.testoposiciones.model.TipoTest
import java.io.File

private var filesDir: File? = null

fun initProgressStorage(dir: File) {
    filesDir = dir
}

actual object ProgressStorage {
    private fun getFile(category: TestCategory, mode: TipoTest): File? {
        val dir = filesDir ?: return null
        return File(dir, "testoposiciones_progress_${category.safeFileName()}_${mode.name.lowercase()}.txt")
    }

    private fun getLegacyFile(category: TestCategory): File? {
        val dir = filesDir ?: return null
        return File(dir, "testapp_progress_${category.safeFileName()}.txt")
    }

    actual fun readNumber(category: TestCategory, mode: TipoTest): Int {
        migrateLegacyProgress(category)
        return try {
            val f = getFile(category, mode) ?: return 1
            if (!f.exists()) 1
            else f.readText().trim().toIntOrNull() ?: 1
        } catch (e: Exception) {
            1
        }
    }

    actual fun writeNumber(category: TestCategory, mode: TipoTest, num: Int) {
        writeProgressNumber(category, mode, num)
    }

    actual fun reset(category: TestCategory) {
        writeNumber(category, TipoTest.SECUENCIAL, 1)
        writeNumber(category, TipoTest.BLOQUES_30, 1)
        getLegacyFile(category)?.delete()
    }

    actual fun readDarkTheme(): Boolean? {
        return try {
            val file = getThemeFile() ?: return null
            if (!file.exists()) null else file.readText().trim().toBooleanStrictOrNull()
        } catch (e: Exception) {
            null
        }
    }

    actual fun writeDarkTheme(dark: Boolean) {
        try {
            getThemeFile()?.writeText(dark.toString())
        } catch (e: Exception) {
            // ignore write errors
        }
    }

    actual fun readFailed(category: TestCategory): Set<String> {
        return try {
            val file = getFailedFile(category) ?: return emptySet()
            if (!file.exists()) emptySet()
            else file.readLines().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    actual fun writeFailed(category: TestCategory, keys: Set<String>) {
        try {
            getFailedFile(category)?.writeText(keys.sorted().joinToString("\n"))
        } catch (e: Exception) {
            // ignore write errors
        }
    }

    // Va por id y no por progressStorageId: las falladas no dependen del orden de las preguntas.
    private fun getFailedFile(category: TestCategory): File? {
        val dir = filesDir ?: return null
        val safeId = category.id.replace(Regex("[^A-Za-z0-9_-]"), "_")
        return File(dir, "testoposiciones_failed_$safeId.txt")
    }

    private fun getThemeFile(): File? {
        val dir = filesDir ?: return null
        return File(dir, "testoposiciones_dark_theme.txt")
    }

    private fun migrateLegacyProgress(category: TestCategory) {
        val legacyFile = getLegacyFile(category) ?: return
        if (!legacyFile.exists()) return
        val value = try {
            legacyFile.readText().trim().toIntOrNull() ?: 1
        } catch (e: Exception) {
            1
        }
        val modes = listOf(TipoTest.SECUENCIAL, TipoTest.BLOQUES_30)
        val migrationCompleted = modes.all { mode ->
            val target = getFile(category, mode)
            target?.exists() == true || writeProgressNumber(category, mode, value)
        }
        if (migrationCompleted) legacyFile.delete()
    }

    private fun writeProgressNumber(category: TestCategory, mode: TipoTest, num: Int): Boolean {
        return try {
            val file = getFile(category, mode) ?: return false
            file.writeText(num.toString())
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun TestCategory.safeFileName(): String {
        return progressStorageId.replace(Regex("[^A-Za-z0-9_-]"), "_")
    }
}
