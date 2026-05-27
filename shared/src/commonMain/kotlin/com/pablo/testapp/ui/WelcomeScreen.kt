package com.pablo.testapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.testapp.model.TipoTest

@Composable
fun WelcomeScreen(onStartTest: (TipoTest) -> Unit) {
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
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
