package com.pablo.testoposiciones

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun ApplySystemBarTheme(darkTheme: Boolean)
