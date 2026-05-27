package com.pablo.testapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.testapp.model.TipoTest

@Composable
fun WelcomeScreen(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onStartTest: (TipoTest) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (darkTheme) LightModeIcon else DarkModeIcon,
                contentDescription = if (darkTheme) "Cambiar a tema claro" else "Cambiar a tema oscuro",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Test Constitución",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "T.E.R. — Test Especializado en Regulación",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            ModeButton(
                title = "30 Preguntas Aleatorias",
                description = "30 preguntas escogidas al azar. Muestra feedback inmediato.",
                onClick = { onStartTest(TipoTest.ALEATORIO_30) }
            )

            Spacer(Modifier.height(16.dp))

            ModeButton(
                title = "Bloques de 30",
                description = "30 preguntas en bloques consecutivos. Reanuda desde el último bloque.",
                onClick = { onStartTest(TipoTest.BLOQUES_30) }
            )

            Spacer(Modifier.height(16.dp))

            ModeButton(
                title = "Preguntas Seguidas",
                description = "Todas las preguntas en orden. Reanuda desde donde lo dejaste.",
                onClick = { onStartTest(TipoTest.SECUENCIAL) }
            )
        }
    }
}

private val LightModeIcon: ImageVector
    get() = ImageVector.Builder(
        name = "LightMode",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 18f)
            curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
            reflectiveCurveTo(8.69f, 6f, 12f, 6f)
            reflectiveCurveTo(18f, 8.69f, 18f, 12f)
            reflectiveCurveTo(15.31f, 18f, 12f, 18f)
            close()
            moveTo(12f, 16f)
            curveTo(14.21f, 16f, 16f, 14.21f, 16f, 12f)
            reflectiveCurveTo(14.21f, 8f, 12f, 8f)
            reflectiveCurveTo(8f, 9.79f, 8f, 12f)
            reflectiveCurveTo(9.79f, 16f, 12f, 16f)
            close()
            moveTo(11f, 2f)
            horizontalLineTo(13f)
            verticalLineTo(5f)
            horizontalLineTo(11f)
            verticalLineTo(2f)
            close()
            moveTo(11f, 19f)
            horizontalLineTo(13f)
            verticalLineTo(22f)
            horizontalLineTo(11f)
            verticalLineTo(19f)
            close()
            moveTo(2f, 11f)
            horizontalLineTo(5f)
            verticalLineTo(13f)
            horizontalLineTo(2f)
            verticalLineTo(11f)
            close()
            moveTo(19f, 11f)
            horizontalLineTo(22f)
            verticalLineTo(13f)
            horizontalLineTo(19f)
            verticalLineTo(11f)
            close()
            moveTo(4.22f, 5.64f)
            lineTo(5.64f, 4.22f)
            lineTo(7.76f, 6.34f)
            lineTo(6.34f, 7.76f)
            lineTo(4.22f, 5.64f)
            close()
            moveTo(16.24f, 17.66f)
            lineTo(17.66f, 16.24f)
            lineTo(19.78f, 18.36f)
            lineTo(18.36f, 19.78f)
            lineTo(16.24f, 17.66f)
            close()
            moveTo(16.24f, 6.34f)
            lineTo(18.36f, 4.22f)
            lineTo(19.78f, 5.64f)
            lineTo(17.66f, 7.76f)
            lineTo(16.24f, 6.34f)
            close()
            moveTo(4.22f, 18.36f)
            lineTo(6.34f, 16.24f)
            lineTo(7.76f, 17.66f)
            lineTo(5.64f, 19.78f)
            lineTo(4.22f, 18.36f)
            close()
        }
    }.build()

private val DarkModeIcon: ImageVector
    get() = ImageVector.Builder(
        name = "DarkMode",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(21f, 14.5f)
            curveTo(19.8f, 15.08f, 18.45f, 15.4f, 17.03f, 15.4f)
            curveTo(12.16f, 15.4f, 8.2f, 11.44f, 8.2f, 6.57f)
            curveTo(8.2f, 5.15f, 8.52f, 3.8f, 9.1f, 2.6f)
            curveTo(5.55f, 3.76f, 3f, 7.1f, 3f, 11.04f)
            curveTo(3f, 16.54f, 7.46f, 21f, 12.96f, 21f)
            curveTo(16.9f, 21f, 20.24f, 18.45f, 21f, 14.5f)
            close()
            moveTo(12.96f, 19f)
            curveTo(8.57f, 19f, 5f, 15.43f, 5f, 11.04f)
            curveTo(5f, 9.09f, 5.71f, 7.31f, 6.88f, 5.94f)
            curveTo(6.83f, 6.15f, 6.8f, 6.36f, 6.8f, 6.57f)
            curveTo(6.8f, 12.21f, 11.39f, 16.8f, 17.03f, 16.8f)
            curveTo(17.24f, 16.8f, 17.45f, 16.77f, 17.66f, 16.72f)
            curveTo(16.37f, 18.13f, 14.64f, 19f, 12.96f, 19f)
            close()
        }
    }.build()

@Composable
private fun ModeButton(title: String, description: String, onClick: () -> Unit) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Text(
                text = description,
                fontSize = 12.sp,
                color = LocalContentColor.current.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
