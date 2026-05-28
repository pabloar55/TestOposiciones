package com.pablo.testoposiciones.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pablo.testoposiciones.model.TipoTest
import kotlin.math.roundToInt

@Composable
fun ResultsScreen(
    correctas: Int,
    incorrectas: Int,
    total: Int,
    segundos: Int,
    tipoTest: TipoTest,
    onRetry: () -> Unit,
    onReview: () -> Unit,
    onBack: () -> Unit
) {
    val blanco = total - correctas - incorrectas
    val score = calcularPuntuacion(correctas, incorrectas, total)
    val canReview = tipoTest == TipoTest.ALEATORIO_30 || tipoTest == TipoTest.BLOQUES_30

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Resultados",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        ScoreCard(
            label = "Correctas",
            value = "$correctas / $total",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Spacer(Modifier.height(12.dp))
        ScoreCard(
            label = "Incorrectas",
            value = "$incorrectas / $total",
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
        Spacer(Modifier.height(12.dp))
        ScoreCard(
            label = "En blanco",
            value = "$blanco / $total",
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tiempo: ", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = formatTime(segundos),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Puntuación: $score / 10",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (tipoTest == TipoTest.BLOQUES_30) "Continuar" else "Reintentar") }
            if (canReview) {
                OutlinedButton(
                    onClick = onReview,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Revisar") }
            }
            TextButton(onClick = onBack) { Text("Inicio") }
        }
    }
}

@Composable
private fun ScoreCard(label: String, value: String, containerColor: Color, contentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

private fun calcularPuntuacion(correctas: Int, incorrectas: Int, total: Int): Double {
    if (total == 0) return 0.0
    val conDescuento = (correctas - incorrectas * 0.33).coerceAtLeast(0.0)
    val sobre10 = (conDescuento * 10) / total
    return (sobre10 * 100).roundToInt() / 100.0
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
