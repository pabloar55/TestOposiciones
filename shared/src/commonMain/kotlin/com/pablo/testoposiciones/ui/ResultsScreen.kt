package com.pablo.testoposiciones.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pablo.testoposiciones.logic.calcularPuntuacion
import com.pablo.testoposiciones.logic.formatNota
import com.pablo.testoposiciones.model.TipoTest
import com.pablo.testoposiciones.ui.icons.schedule

@Composable
fun ResultsScreen(
    correctas: Int,
    incorrectas: Int,
    total: Int,
    segundos: Int,
    tipoTest: TipoTest,
    categoryCompleted: Boolean,
    failedRemaining: Int,
    onRetry: () -> Unit,
    onReview: () -> Unit,
    onBack: () -> Unit
) {
    val blanco = total - correctas - incorrectas
    val nota = formatNota(calcularPuntuacion(correctas, incorrectas, total))
    val canReview = tipoTest == TipoTest.ALEATORIO_30 || tipoTest == TipoTest.BLOQUES_30 ||
        tipoTest == TipoTest.FALLADAS
    val canRetry = tipoTest != TipoTest.FALLADAS || failedRemaining > 0
    val success = successColors

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = tipoTest.resultTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(208.dp)
                    .clearAndSetSemantics {
                        contentDescription = "Nota $nota sobre 10. $correctas correctas, " +
                            "$incorrectas incorrectas y $blanco en blanco."
                    }
            ) {
                ScoreRing(
                    correctas = correctas,
                    incorrectas = incorrectas,
                    total = total,
                    correctColor = success.success,
                    wrongColor = MaterialTheme.colorScheme.error,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = nota,
                        style = MaterialTheme.typography.displayMedium.merge(TabularNumbers),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "sobre 10",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile("Correctas", correctas, success.success, Modifier.weight(1f))
                StatTile("Incorrectas", incorrectas, MaterialTheme.colorScheme.error, Modifier.weight(1f))
                StatTile(
                    "En blanco",
                    blanco,
                    MaterialTheme.colorScheme.outline,
                    Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics(mergeDescendants = true) { }
            ) {
                Icon(
                    schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Tiempo ${formatTime(segundos)}",
                    style = MaterialTheme.typography.bodyLarge.merge(TabularNumbers),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val message = when {
                tipoTest == TipoTest.FALLADAS -> when (failedRemaining) {
                    0 -> "Has acertado todas las preguntas falladas."
                    1 -> "Te queda 1 pregunta fallada."
                    else -> "Te quedan $failedRemaining preguntas falladas."
                }
                categoryCompleted && tipoTest != TipoTest.ALEATORIO_30 ->
                    "Has completado todas las preguntas de esta categoría."
                else -> null
            }
            if (message != null) {
                Spacer(Modifier.height(20.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (canRetry) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        val label = when {
                            tipoTest == TipoTest.BLOQUES_30 && categoryCompleted -> "Volver a empezar"
                            tipoTest == TipoTest.BLOQUES_30 -> "Siguiente bloque"
                            tipoTest == TipoTest.FALLADAS -> "Repasar las que quedan"
                            tipoTest == TipoTest.SECUENCIAL -> "Volver a empezar"
                            else -> "Hacer otro test"
                        }
                        Text(label)
                    }
                }
                if (canReview) {
                    OutlinedButton(
                        onClick = onReview,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) { Text("Revisar respuestas") }
                }
                TextButton(onClick = onBack) { Text("Volver al inicio") }
            }
        }
    }
}

private val TipoTest.resultTitle: String
    get() = when (this) {
        TipoTest.ALEATORIO_30 -> "Test aleatorio terminado"
        TipoTest.BLOQUES_30 -> "Bloque terminado"
        TipoTest.SECUENCIAL -> "Preguntas seguidas"
        TipoTest.FALLADAS -> "Repaso de falladas terminado"
    }

/** Anillo con aciertos y fallos; lo que queda de pista son las preguntas en blanco. */
@Composable
private fun ScoreRing(
    correctas: Int,
    incorrectas: Int,
    total: Int,
    correctColor: Color,
    wrongColor: Color,
    trackColor: Color
) {
    Canvas(Modifier.fillMaxSize()) {
        val strokeWidth = 16.dp.toPx()
        val inset = strokeWidth / 2
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(inset, inset)
        val stroke = Stroke(width = strokeWidth)
        drawArc(trackColor, 0f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
        if (total <= 0) return@Canvas
        val correctSweep = 360f * correctas / total
        val wrongSweep = 360f * incorrectas / total
        drawArc(correctColor, -90f, correctSweep, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
        drawArc(
            wrongColor,
            -90f + correctSweep,
            wrongSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke
        )
    }
}

@Composable
private fun StatTile(label: String, value: Int, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxHeight().semantics(mergeDescendants = true) { }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineSmall.merge(TabularNumbers),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(accent, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
