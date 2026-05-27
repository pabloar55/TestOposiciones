package com.pablo.testapp.platform

import java.io.File

actual object ProgressStorage {
    private val file = File(System.getProperty("user.home"), ".testapp_progress.txt")

    actual fun readNumber(): Int {
        return try {
            if (!file.exists()) 1
            else file.readText().trim().toIntOrNull() ?: 1
        } catch (e: Exception) {
            1
        }
    }

    actual fun writeNumber(num: Int) {
        try {
            file.writeText(num.toString())
        } catch (e: Exception) {
            // ignore write errors
        }
    }
}
