package com.pablo.testapp.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.testapp.model.Pregunta
import com.pablo.testapp.model.ResultadoPregunta
import com.pablo.testapp.model.TipoTest
import com.pablo.testapp.platform.ProgressStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import kotlin.time.Duration.Companion.milliseconds

sealed class Screen {
    object Welcome : Screen()
    data class Question(val tipoTest: TipoTest) : Screen()
    data class Results(
        val correctas: Int,
        val incorrectas: Int,
        val total: Int,
        val segundos: Int,
        val tipoTest: TipoTest,
        val historial: List<ResultadoPregunta>
    ) : Screen()
    data class Review(val historial: List<ResultadoPregunta>, val results: Results) : Screen()
}

class TestViewModel : ViewModel() {

    var screen by mutableStateOf<Screen>(Screen.Welcome)
        private set

    var preguntas by mutableStateOf<List<Pregunta>>(emptyList())
        private set

    var currentIndex by mutableIntStateOf(0)
        private set

    var tipoTest by mutableStateOf(TipoTest.ALEATORIO_30)
        private set

    val userAnswers = mutableStateMapOf<Int, Char>()

    var secondsElapsed by mutableIntStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var offsetInicial = 0
        private set

    private var timerJob: Job? = null

    fun startTest(tipo: TipoTest) {
        tipoTest = tipo
        isLoading = true
        viewModelScope.launch {
            val allPreguntas = ExtraerPreguntas.extraerPreguntas()
            val loaded = when (tipo) {
                TipoTest.ALEATORIO_30 -> allPreguntas.shuffled().take(30)
                TipoTest.SECUENCIAL -> {
                    val start = ProgressStorage.readNumber() - 1
                    offsetInicial = start.coerceAtLeast(0)
                    allPreguntas.drop(offsetInicial)
                }
                TipoTest.BLOQUES_30 -> {
                    val start = ProgressStorage.readNumber() - 1
                    offsetInicial = start.coerceAtLeast(0)
                    allPreguntas.drop(offsetInicial).take(30)
                }
            }
            preguntas = loaded
            currentIndex = 0
            userAnswers.clear()
            secondsElapsed = 0
            isLoading = false
            screen = Screen.Question(tipo)
            startTimer()
        }
    }

    fun selectAnswer(opcion: Char) {
        userAnswers[currentIndex] = opcion
    }

    fun goNext() {
        if (currentIndex < preguntas.size - 1) {
            currentIndex++
        } else {
            finalizeTest()
        }
    }

    fun goPrevious() {
        if (currentIndex > 0) {
            currentIndex--
        }
    }

    fun goToWelcome() {
        stopTimer()
        saveProgress()
        screen = Screen.Welcome
    }

    fun retry() {
        stopTimer()
        startTest(tipoTest)
    }

    fun reviewAnswers(results: Screen.Results) {
        screen = Screen.Review(results.historial, results)
    }

    fun backToResults(results: Screen.Results) {
        screen = results
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000.milliseconds)
                secondsElapsed++
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun saveProgress() {
        when (tipoTest) {
            TipoTest.SECUENCIAL -> ProgressStorage.writeNumber(offsetInicial + currentIndex + 1)
            TipoTest.BLOQUES_30 -> ProgressStorage.writeNumber(offsetInicial + currentIndex + 1)
            else -> {}
        }
    }

    private fun finalizeTest() {
        stopTimer()
        var correctas = 0
        var incorrectas = 0
        val historial = preguntas.mapIndexed { i, pregunta ->
            val respuesta = userAnswers.getOrElse(i) { ' ' }
            val esCorrecta = respuesta == pregunta.respuestaCorrecta
            if (respuesta != ' ') {
                if (esCorrecta) correctas++ else incorrectas++
            }
            ResultadoPregunta(pregunta, respuesta, esCorrecta)
        }

        when (tipoTest) {
            TipoTest.SECUENCIAL -> ProgressStorage.writeNumber(1)
            TipoTest.BLOQUES_30 -> ProgressStorage.writeNumber(offsetInicial + preguntas.size + 1)
            else -> {}
        }

        screen = Screen.Results(correctas, incorrectas, preguntas.size, secondsElapsed, tipoTest, historial)
    }
}
