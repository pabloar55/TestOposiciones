package com.pablo.testapp

import androidx.compose.runtime.Composable

@Composable
expect fun AppBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
)
