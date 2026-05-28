package com.pablo.testapp.platform

import com.pablo.testapp.model.TestCategory

expect object ProgressStorage {
    fun readNumber(category: TestCategory): Int
    fun writeNumber(category: TestCategory, num: Int)
    fun reset(category: TestCategory)
}
