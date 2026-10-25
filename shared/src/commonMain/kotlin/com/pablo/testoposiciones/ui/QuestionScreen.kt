package com.pablo.testoposiciones.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pablo.testoposiciones.model.Pregunta
import com.pablo.testoposiciones.model.ResultadoPregunta
import com.pablo.testoposiciones.model.TipoTest
import com.pablo.testoposiciones.model.showsFeedback
import com.pablo.testoposiciones.ui.icons.arrow_back
import com.pablo.testoposiciones.ui.icons.arrow_forward

@OptIn(ExperimentalMaterial3Api::class)
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
    val showFeedback = tipoTest.showsFeedback
    val answerLocked = showFeedback && selectedAnswer != null

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        TextButton(onClick = onClose) {
                            Icon(arrow_back, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Salir")
                        }
                    },
                    title = {
                        Text(
                            text = "$displayQuestionNumber / $visibleTotal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        Text(
                            text = formatTime(secondsElapsed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                )
                LinearProgressIndicator(
                    progress = { displayQuestionNumber.toFloat() / visibleTotal },
                    modifier = Modifier.fillMaxWidth(),
                    drawStopIndicator = {}
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 800.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberQuestionScrollState(currentIndex))
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp)
            ) {
                Text(
                    text = pregunta.texto,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                QuestionIllustration(pregunta.imagenPath)

                AnswerOption(
                    text = pregunta.opcionA(),
                    option = 'a',
                    selected = selectedAnswer == 'a',
                    correctAnswer = pregunta.respuestaCorrecta,
                    showFeedback = answerLocked,
                    interactive = !answerLocked,
                    onClick = { onAnswer('a') }
                )
                Spacer(Modifier.height(8.dp))
                AnswerOption(
                    text = pregunta.opcionB(),
                    option = 'b',
                    selected = selectedAnswer == 'b',
                    correctAnswer = pregunta.respuestaCorrecta,
                    showFeedback = answerLocked,
                    interactive = !answerLocked,
                    onClick = { onAnswer('b') }
                )
                Spacer(Modifier.height(8.dp))
                AnswerOption(
                    text = pregunta.opcionC(),
                    option = 'c',
                    selected = selectedAnswer == 'c',
                    correctAnswer = pregunta.respuestaCorrecta,
                    showFeedback = answerLocked,
                    interactive = !answerLocked,
                    onClick = { onAnswer('c') }
                )
                Spacer(Modifier.height(8.dp))
                AnswerOption(
                    text = pregunta.opcionD(),
                    option = 'd',
                    selected = selectedAnswer == 'd',
                    correctAnswer = pregunta.respuestaCorrecta,
                    showFeedback = answerLocked,
                    interactive = !answerLocked,
                    onClick = { onAnswer('d') }
                )

                if (showFeedback && selectedAnswer != null) {
                    Spacer(Modifier.height(16.dp))
                    val isCorrect = selectedAnswer == pregunta.respuestaCorrecta
                    Text(
                        text = if (isCorrect) "Respuesta correcta ✓" else "Respuesta incorrecta ✗",
                        color = if (isCorrect) successColor() else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 800.dp)
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onPrevious,
                    enabled = currentIndex > 0,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
}

@Composable
fun AnswerOption(
    text: String,
    option: Char,
    selected: Boolean,
    correctAnswer: Char,
    showFeedback: Boolean,
    interactive: Boolean = true,
    onClick: () -> Unit
) {
    val containerColor = when {
        showFeedback && option == correctAnswer -> successContainerColor()
        showFeedback && selected -> MaterialTheme.colorScheme.errorContainer
        selected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        showFeedback && option == correctAnswer -> onSuccessContainerColor()
        showFeedback && selected -> MaterialTheme.colorScheme.onErrorContainer
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val stateText = when {
        showFeedback && option == correctAnswer -> "Respuesta correcta"
        showFeedback && selected -> "Tu respuesta, incorrecta"
        selected -> "Respuesta seleccionada"
        else -> "Respuesta no seleccionada"
    }
    val interactionModifier = if (interactive) {
        Modifier.selectable(
            selected = selected,
            role = Role.RadioButton,
            onClick = onClick
        ).semantics(mergeDescendants = true) {
            stateDescription = stateText
        }
    } else {
        Modifier.semantics(mergeDescendants = true) {
            stateDescription = stateText
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(interactionModifier),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                modifier = Modifier.clearAndSetSemantics { }
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = text, style = MaterialTheme.typography.bodyLarge)
                if (showFeedback && (option == correctAnswer || selected)) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (option == correctAnswer) "Respuesta correcta" else "Tu respuesta",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        TextButton(onClick = onClose) {
                            Icon(arrow_back, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Volver")
                        }
                    },
                    title = {
                        Text(
                            text = "${currentIndex + 1} / ${historial.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / historial.size },
                    modifier = Modifier.fillMaxWidth(),
                    drawStopIndicator = {}
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 800.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberQuestionScrollState(currentIndex))
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp)
            ) {
            Text(
                text = pregunta.texto,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            QuestionIllustration(pregunta.imagenPath)

            listOf('a', 'b', 'c', 'd').forEachIndexed { i, opt ->
                val optionText = pregunta.opciones[i]
                AnswerOption(
                    text = optionText,
                    option = opt,
                    selected = opt == resultado.respuestaUsuario,
                    correctAnswer = pregunta.respuestaCorrecta,
                    showFeedback = true,
                    interactive = false,
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
                resultado.esCorrecta -> successColor()
                else -> MaterialTheme.colorScheme.error
            }
            Text(text = statusText, color = statusColor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 800.dp)
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
}

// Cada pregunta empieza arriba del todo; si no, hereda el desplazamiento de la anterior.
@Composable
private fun rememberQuestionScrollState(questionIndex: Int): ScrollState =
    rememberSaveable(questionIndex, saver = ScrollState.Saver) { ScrollState(0) }

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
