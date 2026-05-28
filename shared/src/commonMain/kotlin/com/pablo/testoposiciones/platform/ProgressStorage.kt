package com.pablo.testoposiciones.platform

import com.pablo.testoposiciones.model.TestCategory

expect object ProgressStorage {
    fun readNumber(category: TestCategory): Int
    fun writeNumber(category: TestCategory, num: Int)
    fun reset(category: TestCategory)
}
