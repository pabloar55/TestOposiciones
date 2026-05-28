package com.pablo.testapp.platform

import java.io.File
import com.pablo.testapp.model.TestCategory

actual object ProgressStorage {
    private fun getFile(category: TestCategory): File {
        return File(System.getProperty("user.home"), ".testapp_progress_${category.safeFileName()}.txt")
    }

    actual fun readNumber(category: TestCategory): Int {
        val file = getFile(category)
        return try {
            if (!file.exists()) 1
            else file.readText().trim().toIntOrNull() ?: 1
        } catch (e: Exception) {
            1
        }
    }

    actual fun writeNumber(category: TestCategory, num: Int) {
        val file = getFile(category)
        try {
            file.writeText(num.toString())
        } catch (e: Exception) {
            // ignore write errors
        }
    }

    actual fun reset(category: TestCategory) {
        writeNumber(category, 1)
    }

    private fun TestCategory.safeFileName(): String {
        return id.replace(Regex("[^A-Za-z0-9_-]"), "_")
    }
}
