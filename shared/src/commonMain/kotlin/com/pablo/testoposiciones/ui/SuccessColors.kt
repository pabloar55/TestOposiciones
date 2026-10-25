package com.pablo.testoposiciones.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Material 3 no tiene color de acierto: tertiary por defecto es malva, así que se define aquí.
// Se usa el tema aplicado en la app, que puede no coincidir con el del sistema.
@Composable
private fun isAppDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

/** Fondo de tarjetas y opciones correctas. */
@Composable
internal fun successContainerColor(): Color =
    if (isAppDarkTheme()) Color(0xFF1B5E20) else Color(0xFFC8E6C9)

/** Texto sobre [successContainerColor]. */
@Composable
internal fun onSuccessContainerColor(): Color = MaterialTheme.colorScheme.onSurface

/** Texto de acierto sobre el fondo de la pantalla. */
@Composable
internal fun successColor(): Color =
    if (isAppDarkTheme()) Color(0xFF81C784) else Color(0xFF2E7D32)
