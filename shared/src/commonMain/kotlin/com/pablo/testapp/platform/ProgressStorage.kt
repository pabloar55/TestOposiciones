package com.pablo.testapp.platform

expect object ProgressStorage {
    fun readNumber(): Int
    fun writeNumber(num: Int)
}
