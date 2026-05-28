package com.pablo.testapp.platform

import com.pablo.testapp.model.TestCategory
import java.io.File

private var filesDir: File? = null

fun initProgressStorage(dir: File) {
    filesDir = dir
}

actual object ProgressStorage {
    private fun getFile(category: TestCategory): File? {
        val dir = filesDir ?: return null
        return File(dir, "testapp_progress_${category.safeFileName()}.txt")
    }

    actual fun readNumber(category: TestCategory): Int {
        return try {
            val f = getFile(category) ?: return 1
            if (!f.exists()) 1
            else f.readText().trim().toIntOrNull() ?: 1
        } catch (e: Exception) {
            1
        }
    }

    actual fun writeNumber(category: TestCategory, num: Int) {
        try {
            val file = getFile(category) ?: return
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
