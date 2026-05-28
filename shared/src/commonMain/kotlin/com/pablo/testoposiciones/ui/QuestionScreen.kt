package com.pablo.testoposiciones.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pablo.testoposiciones.model.Pregunta
import com.pablo.testoposiciones.model.ResultadoPregunta
import com.pablo.testoposiciones.model.TipoTest
import com.pablo.testoposiciones.ui.icons.arrow_back
import com.pablo.testoposiciones.ui.icons.arrow_forward

@Composable
fun QuestionScreen(
    preguntas: List<Pregunta>,
    currentIndex: Int,
    displayQuestionNumber: Int = currentIndex + 1,
    displayTotal: Int = preguntas.size,
    tipoTest: TipoTest,
    userAnswers: Map<Int, Char>,
    secondsElapsed: Int,
    onAnswer: (Char) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit
) {
    if (preguntas.isEmpty()) return
    val pregunta = preguntas[currentIndex]
    val selectedAnswer = userAnswers[currentIndex]
    val total = preguntas.size
    val visibleTotal = displayTotal.coerceAtLeast(displayQuestionNumber)
    val showFeedback = tipoTest != TipoTest.BLOQUES_30

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onClose) {
                Icon(arrow_back, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Salir")
            }
            Text(
                text = "$displayQuestionNumber / $visibleTotal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatTime(secondsElapsed),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LinearProgressIndicator(
            progress = { displayQuestionNumber.toFloat() / visibleTotal },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            drawStopIndicator = {}
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = pregunta.texto,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            AnswerOption(
                text = pregunta.opcionA(),
                option = 'a',
                selected = selectedAnswer == 'a',
                correctAnswer = pregunta.respuestaCorrecta,
                showFeedback = showFeedback && selectedAnswer != null,
                onClick = { onAnswer('a') }
            )
            Spacer(Modifier.height(8.dp))
            AnswerOption(
                text = pregunta.opcionB(),
                option = 'b',
                selected = selectedAnswer == 'b',
                correctAnswer = pregunta.respuestaCorrecta,
                showFeedback = showFeedback && selectedAnswer != null,
                onClick = { onAnswer('b') }
            )
            Spacer(Modifier.height(8.dp))
            AnswerOption(
                text = pregunta.opcionC(),
                option = 'c',
                selected = selectedAnswer == 'c',
                correctAnswer = pregunta.respuestaCorrecta,
                showFeedback = showFeedback && selectedAnswer != null,
                onClick = { onAnswer('c') }
            )
            Spacer(Modifier.height(8.dp))
            AnswerOption(
                text = pregunta.opcionD(),
                option = 'd',
                selected = selectedAnswer == 'd',
                correctAnswer = pregunta.respuestaCorrecta,
                showFeedback = showFeedback && selectedAnswer != null,
                onClick = { onAnswer('d') }
            )

            if (showFeedback && selectedAnswer != null) {
                Spacer(Modifier.height(16.dp))
                val isCorrect = selectedAnswer == pregunta.respuestaCorrecta
                Text(
                    text = if (isCorrect) "Respuesta correcta ✓" else "Respuesta incorrecta ✗",
                    color = if (isCorrect) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = currentIndex > 0
            ) {
                Icon(arrow_back, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Anterior")
            }

            Button(onClick = onNext) {
                if (currentIndex == total - 1) {
                    Text("Finalizar")
                } else {
                    Text("Siguiente")
                    Spacer(Modifier.width(4.dp))
                    Icon(arrow_forward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun AnswerOption(
    text: String,
    option: Char,
    selected: Boolean,
    correctAnswer: Char,
    showFeedback: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

// Definimos los tonos de verde para claro/oscuro
    val correctContainerColor = if (isDark) Color(0xFF1B5E20) else Color(0xFFC8E6C9)

// Puedes mantener el error de MaterialTheme o definirlo manualmente si no te convence el naranja
    val errorContainerColor = MaterialTheme.colorScheme.errorContainer

    val containerColor = when {
        showFeedback && option == correctAnswer -> correctContainerColor
        showFeedback && selected -> errorContainerColor
        selected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(Modifier.width(8.dp))
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun ReviewScreen(
    historial: List<ResultadoPregunta>,
    currentIndex: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit
) {
    if (historial.isEmpty()) return
    val resultado = historial[currentIndex]
    val pregunta = resultado.pregunta

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onClose) {
                Icon(arrow_back, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Volver")
            }
            Text(
                text = "${currentIndex + 1} / ${historial.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / historial.size },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            drawStopIndicator = {}
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = pregunta.texto,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            listOf('a', 'b', 'c', 'd').forEachIndexed { i, opt ->
                val optionText = pregunta.opciones[i]
                AnswerOption(
                    text = optionText,
                    option = opt,
                    selected = opt == resultado.respuestaUsuario,
                    correctAnswer = pregunta.respuestaCorrecta,
                    showFeedback = true,
                    onClick = {}
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))
            val statusText = when {
                resultado.respuestaUsuario == ' ' -> "Sin respuesta"
                resultado.esCorrecta -> "Correcta ✓"
                else -> "Incorrecta ✗ — Respuesta correcta: ${pregunta.respuestaCorrecta})"
            }
            val statusColor = when {
                resultado.respuestaUsuario == ' ' -> MaterialTheme.colorScheme.onSurfaceVariant
                resultado.esCorrecta -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }
            Text(text = statusText, color = statusColor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onPrevious, enabled = currentIndex > 0) {
                Icon(arrow_back, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Anterior")
            }
            Button(onClick = onNext, enabled = currentIndex < historial.size - 1) {
                Text("Siguiente")
                Spacer(Modifier.width(4.dp))
                Icon(arrow_forward, contentDescription = null)
            }
        }
    }
}


private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
