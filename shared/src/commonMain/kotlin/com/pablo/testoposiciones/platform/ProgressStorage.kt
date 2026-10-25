package com.pablo.testoposiciones.platform

import com.pablo.testoposiciones.model.TestCategory
import com.pablo.testoposiciones.model.TipoTest

expect object ProgressStorage {
    fun readNumber(category: TestCategory, mode: TipoTest): Int
    fun writeNumber(category: TestCategory, mode: TipoTest, num: Int)
    fun reset(category: TestCategory)

    // null indica que el usuario nunca ha elegido tema y se sigue el del sistema.
    fun readDarkTheme(): Boolean?
    fun writeDarkTheme(dark: Boolean)
}
