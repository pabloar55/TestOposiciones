package com.pablo.testapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.testapp.model.ResultadoPregunta
import com.pablo.testapp.model.TipoTest
import kotlin.math.roundToInt

@Composable
fun ResultsScreen(
    correctas: Int,
    incorrectas: Int,
    total: Int,
    segundos: Int,
    tipoTest: TipoTest,
    historial: List<ResultadoPregunta>,
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
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        ScoreCard(
            label = "Correctas",
            value = "$correctas / $total",
            color = Color(0xFF2E7D32)
        )
        Spacer(Modifier.height(12.dp))
        ScoreCard(
            label = "Incorrectas",
            value = "$incorrectas / $total",
            color = Color(0xFFC62828)
        )
        Spacer(Modifier.height(12.dp))
        ScoreCard(
            label = "En blanco",
            value = "$blanco / $total",
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tiempo: ", fontSize = 20.sp)
            Text(
                text = formatTime(segundos),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Puntuación: $score / 10",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = onBack) { Text("Inicio") }
            Button(onClick = onRetry) { Text("Reintentar") }
            if (canReview) {
                Button(onClick = onReview) { Text("Revisar") }
            }
        }
    }
}

@Composable
private fun ScoreCard(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 18.sp, color = color, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 22.sp, color = color, fontWeight = FontWeight.Bold)
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
