package com.pablo.testoposiciones.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.testoposiciones.logic.TestSession
import com.pablo.testoposiciones.model.Pregunta
import com.pablo.testoposiciones.model.ResultadoPregunta
import com.pablo.testoposiciones.ui.icons.arrow_back
import com.pablo.testoposiciones.ui.icons.arrow_forward
import com.pablo.testoposiciones.ui.icons.check
import com.pablo.testoposiciones.ui.icons.close
import com.pablo.testoposiciones.ui.icons.schedule

private val OPTION_LETTERS = listOf('a', 'b', 'c', 'd')

@Composable
fun QuestionScreen(
    session: TestSession,
    onAnswer: (Char) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit
) {
    val number = session.displayQuestionNumber
    val visibleTotal = session.displayQuestionTotal.coerceAtLeast(number)
    val answerLocked = session.answerLocked

    QuestionLayout(
        title = "Pregunta $number de $visibleTotal",
        progress = number.toFloat() / visibleTotal,
        navigationIcon = close,
        navigationLabel = "Salir del test",
        onNavigate = onClose,
        scrollKey = session.currentIndex,
        actions = { TimerChip(session.secondsElapsed) },
        bottomBar = {
            OutlinedButton(
                onClick = onPrevious,
                enabled = !session.isFirst,
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(arrow_back, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Anterior")
            }
            Button(onClick = onNext, modifier = Modifier.weight(1f).height(52.dp)) {
                if (session.isLast) {
                    Text("Finalizar")
                } else {
                    Text("Siguiente")
                    Spacer(Modifier.width(8.dp))
                    Icon(arrow_forward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    ) {
        QuestionBody(
            pregunta = session.pregunta,
            selectedAnswer = session.respuestaActual,
            revealed = answerLocked,
            onAnswer = onAnswer.takeIf { !answerLocked }
        )
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

    QuestionLayout(
        title = "Revisión · ${currentIndex + 1} de ${historial.size}",
        progress = (currentIndex + 1).toFloat() / historial.size,
        navigationIcon = arrow_back,
        navigationLabel = "Volver a resultados",
        onNavigate = onClose,
        scrollKey = currentIndex,
        bottomBar = {
            OutlinedButton(
                onClick = onPrevious,
                enabled = currentIndex > 0,
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Icon(arrow_back, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Anterior")
            }
            Button(
                onClick = onNext,
                enabled = currentIndex < historial.size - 1,
                modifier = Modifier.weight(1f).height(52.dp)
            ) {
                Text("Siguiente")
                Spacer(Modifier.width(8.dp))
                Icon(arrow_forward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    ) {
        QuestionBody(
            pregunta = resultado.pregunta,
            selectedAnswer = resultado.respuestaUsuario,
            revealed = true,
            onAnswer = null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionLayout(
    title: String,
    progress: Float,
    navigationIcon: ImageVector,
    navigationLabel: String,
    onNavigate: () -> Unit,
    scrollKey: Int,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onNavigate) {
                            Icon(navigationIcon, contentDescription = navigationLabel)
                        }
                    },
                    title = { Text(title, style = MaterialTheme.typography.titleMedium.merge(TabularNumbers)) },
                    actions = actions,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    drawStopIndicator = {}
                )
            }
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 800.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    content = bottomBar
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
                    .verticalScroll(rememberQuestionScrollState(scrollKey))
                    .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 24.dp),
                content = content
            )
        }
    }
}

/** Enunciado, ilustración, opciones y, una vez respondida, la corrección. */
@Composable
private fun QuestionBody(
    pregunta: Pregunta,
    selectedAnswer: Char?,
    revealed: Boolean,
    onAnswer: ((Char) -> Unit)?
) {
    Text(
        text = enunciadoSinNumero(pregunta.texto),
        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 28.sp),
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 20.dp)
    )

    QuestionIllustration(pregunta.imagenPath)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OPTION_LETTERS.forEachIndexed { i, letter ->
            val state = when {
                revealed && letter == pregunta.respuestaCorrecta -> AnswerState.Correct
                revealed && letter == selectedAnswer -> AnswerState.Wrong
                revealed -> AnswerState.Dimmed
                letter == selectedAnswer -> AnswerState.Selected
                else -> AnswerState.Idle
            }
            AnswerOption(
                letter = letter,
                text = opcionSinLetra(pregunta.opciones[i]),
                state = state,
                onClick = onAnswer?.let { { it(letter) } }
            )
        }
    }

    if (revealed) {
        Spacer(Modifier.height(16.dp))
        FeedbackBanner(selectedAnswer = selectedAnswer, correctAnswer = pregunta.respuestaCorrecta)
    }
}

private enum class AnswerState { Idle, Selected, Correct, Wrong, Dimmed }

@Composable
private fun AnswerOption(
    letter: Char,
    text: String,
    state: AnswerState,
    onClick: (() -> Unit)?
) {
    val colors = MaterialTheme.colorScheme
    val success = successColors
    val container = when (state) {
        AnswerState.Selected -> colors.primaryContainer
        AnswerState.Correct -> success.successContainer
        AnswerState.Wrong -> colors.errorContainer
        AnswerState.Idle, AnswerState.Dimmed -> colors.surfaceContainerLow
    }
    val border = when (state) {
        AnswerState.Selected -> colors.primary
        AnswerState.Correct -> success.success
        AnswerState.Wrong -> colors.error
        AnswerState.Idle, AnswerState.Dimmed -> colors.outlineVariant
    }
    val content = when (state) {
        AnswerState.Selected -> colors.onPrimaryContainer
        AnswerState.Correct -> success.onSuccessContainer
        AnswerState.Wrong -> colors.onErrorContainer
        AnswerState.Idle -> colors.onSurface
        AnswerState.Dimmed -> colors.onSurfaceVariant
    }
    val stateText = when (state) {
        AnswerState.Correct -> "Respuesta correcta"
        AnswerState.Wrong -> "Tu respuesta, incorrecta"
        AnswerState.Selected -> "Respuesta seleccionada"
        AnswerState.Idle, AnswerState.Dimmed -> "Respuesta no seleccionada"
    }
    val animatedContainer by animateColorAsState(container)
    val animatedBorder by animateColorAsState(border)
    val shape = MaterialTheme.shapes.medium
    val emphasized = state != AnswerState.Idle && state != AnswerState.Dimmed

    val interactionModifier = if (onClick != null) {
        Modifier.selectable(
            selected = state == AnswerState.Selected,
            role = Role.RadioButton,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(interactionModifier)
            .semantics(mergeDescendants = true) { stateDescription = stateText },
        shape = shape,
        color = animatedContainer,
        contentColor = content,
        border = BorderStroke(if (emphasized) 2.dp else 1.dp, animatedBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LetterBadge(letter, state)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = text, style = MaterialTheme.typography.bodyLarge)
                val label = when (state) {
                    AnswerState.Correct -> "Respuesta correcta"
                    AnswerState.Wrong -> "Tu respuesta"
                    else -> null
                }
                if (label != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LetterBadge(letter: Char, state: AnswerState) {
    val colors = MaterialTheme.colorScheme
    val success = successColors
    val (background, foreground) = when (state) {
        AnswerState.Selected -> colors.primary to colors.onPrimary
        AnswerState.Correct -> success.success to success.onSuccess
        AnswerState.Wrong -> colors.error to colors.onError
        AnswerState.Idle, AnswerState.Dimmed -> colors.surfaceContainerHighest to colors.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(background, CircleShape)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            AnswerState.Correct -> Icon(check, contentDescription = null, tint = foreground, modifier = Modifier.size(20.dp))
            AnswerState.Wrong -> Icon(close, contentDescription = null, tint = foreground, modifier = Modifier.size(20.dp))
            else -> Text(
                text = letter.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = foreground
            )
        }
    }
}

@Composable
private fun FeedbackBanner(selectedAnswer: Char?, correctAnswer: Char) {
    val success = successColors
    val colors = MaterialTheme.colorScheme
    val correctLetter = correctAnswer.uppercase()
    val (title, detail, palette) = when (selectedAnswer) {
        correctAnswer -> Triple("¡Correcta!", null, success.successContainer to success.onSuccessContainer)
        null -> Triple(
            "Sin responder",
            "La respuesta correcta es la $correctLetter.",
            colors.surfaceContainerHigh to colors.onSurface
        )
        else -> Triple(
            "Incorrecta",
            "La respuesta correcta es la $correctLetter.",
            colors.errorContainer to colors.onErrorContainer
        )
    }
    Surface(
        color = palette.first,
        contentColor = palette.second,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (detail != null) {
                Text(detail, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun TimerChip(seconds: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = CircleShape,
        modifier = Modifier.padding(end = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(schedule, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = formatTime(seconds),
                style = MaterialTheme.typography.labelLarge.merge(TabularNumbers)
            )
        }
    }
}

// Cada pregunta empieza arriba del todo; si no, hereda el desplazamiento de la anterior.
@Composable
private fun rememberQuestionScrollState(questionIndex: Int): ScrollState =
    rememberSaveable(questionIndex, saver = ScrollState.Saver) { ScrollState(0) }
