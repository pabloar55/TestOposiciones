package com.pablo.testapp.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.testapp.model.Pregunta
import com.pablo.testapp.model.ResultadoPregunta
import com.pablo.testapp.model.TestCategory
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
        
    var testCategory by mutableStateOf<TestCategory?>(null)
        private set

    var categories by mutableStateOf<List<TestCategory>>(emptyList())
        private set

    var categoriesLoading by mutableStateOf(true)
        private set

    var loadError by mutableStateOf<String?>(null)
        private set

    val userAnswers = mutableStateMapOf<Int, Char>()

    var secondsElapsed by mutableIntStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var offsetInicial = 0
        private set

    var totalPreguntas by mutableIntStateOf(0)
        private set

    val displayQuestionNumber: Int
        get() = if (tipoTest == TipoTest.SECUENCIAL) offsetInicial + currentIndex + 1 else currentIndex + 1

    val displayQuestionTotal: Int
        get() = if (tipoTest == TipoTest.SECUENCIAL && totalPreguntas > 0) totalPreguntas else preguntas.size

    private var timerJob: Job? = null

    init {
        loadCategories()
    }

    fun loadCategories() {
        categoriesLoading = true
        loadError = null
        viewModelScope.launch {
            try {
                val loadedCategories = TestCategories.load()
                val selectedId = testCategory?.id
                categories = loadedCategories
                testCategory = loadedCategories.firstOrNull { it.id == selectedId } ?: loadedCategories.firstOrNull()
            } catch (e: Exception) {
                categories = emptyList()
                testCategory = null
                loadError = e.message ?: "No se han podido cargar las categorias."
            } finally {
                categoriesLoading = false
            }
        }
    }

    fun selectCategory(category: TestCategory) {
        testCategory = category
    }

    fun resetProgress(category: TestCategory) {
        ProgressStorage.reset(category)
    }

    fun startTest(tipo: TipoTest, category: TestCategory) {
        tipoTest = tipo
        testCategory = category
        isLoading = true
        loadError = null
        screen = Screen.Question(tipo)
        viewModelScope.launch {
            try {
                val allPreguntas = ExtraerPreguntas.extraerPreguntas(category)
                totalPreguntas = allPreguntas.size
                offsetInicial = 0
                val loaded = when (tipo) {
                    TipoTest.ALEATORIO_30 -> allPreguntas.shuffled().take(30)
                    TipoTest.SECUENCIAL -> {
                        val start = readProgressStartIndex(category, allPreguntas.size)
                        offsetInicial = start
                        allPreguntas.drop(offsetInicial)
                    }
                    TipoTest.BLOQUES_30 -> {
                        val start = readProgressStartIndex(category, allPreguntas.size)
                        offsetInicial = start
                        allPreguntas.drop(offsetInicial).take(30)
                    }
                }
                preguntas = loaded
                currentIndex = 0
                userAnswers.clear()
                secondsElapsed = 0
                startTimer()
            } catch (e: Exception) {
                preguntas = emptyList()
                loadError = e.message ?: "No se han podido cargar las preguntas."
                screen = Screen.Welcome
            } finally {
                isLoading = false
            }
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
        val leavingActiveTest = screen is Screen.Question
        stopTimer()
        if (leavingActiveTest) {
            saveProgress()
        }
        screen = Screen.Welcome
    }

    fun retry() {
        stopTimer()
        val category = testCategory ?: return
        if (tipoTest == TipoTest.SECUENCIAL) {
            ProgressStorage.reset(category)
        }
        startTest(tipoTest, category)
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
        val category = testCategory ?: return
        when (tipoTest) {
            TipoTest.SECUENCIAL -> ProgressStorage.writeNumber(category, offsetInicial + currentIndex + 1)
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

        val category = testCategory
        when (tipoTest) {
            TipoTest.SECUENCIAL -> if (category != null) {
                ProgressStorage.writeNumber(
                    category,
                    (offsetInicial + preguntas.size).coerceIn(1, totalPreguntas.coerceAtLeast(1))
                )
            }
            TipoTest.BLOQUES_30 -> if (category != null) {
                ProgressStorage.writeNumber(category, offsetInicial + preguntas.size + 1)
            }
            else -> {}
        }

        screen = Screen.Results(correctas, incorrectas, preguntas.size, secondsElapsed, tipoTest, historial)
    }

    private fun readProgressStartIndex(category: TestCategory, total: Int): Int {
        if (total <= 0) return 0
        return (ProgressStorage.readNumber(category) - 1).coerceIn(0, total - 1)
    }
}
