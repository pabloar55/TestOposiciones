package com.pablo.testapp.platform

import java.io.File

private var filesDir: File? = null

fun initProgressStorage(dir: File) {
    filesDir = dir
}

actual object ProgressStorage {
    actual fun readNumber(): Int {
        return try {
            val f = File(filesDir ?: return 1, "testapp_progress.txt")
            if (!f.exists()) 1
            else f.readText().trim().toIntOrNull() ?: 1
        } catch (e: Exception) {
            1
        }
    }

    actual fun writeNumber(num: Int) {
        try {
            val dir = filesDir ?: return
            File(dir, "testapp_progress.txt").writeText(num.toString())
        } catch (e: Exception) {
            // ignore write errors
        }
    }
}
