package com.pablo.testapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform