package com.pablo.testoposiciones.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablo.testoposiciones.model.BLOCK_SIZE
import com.pablo.testoposiciones.model.CategoryProgress
import com.pablo.testoposiciones.model.ResultadoPregunta
import com.pablo.testoposiciones.model.TestCategory
import com.pablo.testoposiciones.model.TipoTest
import com.pablo.testoposiciones.model.showsFeedback
import com.pablo.testoposiciones.platform.ProgressStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.*
import kotlin.time.Duration.Companion.milliseconds

private const val RANDOM_TEST_SIZE = 30

/** Cada pantalla lleva su propio estado: fuera de [Question] no existe ningún test en curso. */
sealed interface Screen {
    data object Welcome : Screen

    data class Loading(val tipoTest: TipoTest) : Screen

    data class Question(val session: TestSession) : Screen

    data class Results(
        val category: TestCategory,
        val tipoTest: TipoTest,
        val historial: List<ResultadoPregunta>,
        val segundos: Int,
        val categoryCompleted: Boolean,
        val failedRemaining: Int
    ) : Screen {
        val total: Int get() = historial.size
        val correctas: Int get() = historial.count { it.esCorrecta }
        val incorrectas: Int get() = historial.count { it.respuestaUsuario != null && !it.esCorrecta }
    }

    data class Review(val results: Results, val index: Int = 0) : Screen
}

class TestViewModel : ViewModel() {

    var screen by mutableStateOf<Screen>(Screen.Welcome)
        private set

    // Estado de la pantalla de inicio.

    var testCategory by mutableStateOf<TestCategory?>(null)
        private set

    var categories by mutableStateOf<List<TestCategory>>(emptyList())
        private set

    var categoriesLoading by mutableStateOf(true)
        private set

    var loadError by mutableStateOf<String?>(null)
        private set

    var categoryProgress by mutableStateOf<CategoryProgress?>(null)
        private set

    private val session: TestSession?
        get() = (screen as? Screen.Question)?.session

    private var timerJob: Job? = null
    private var appVisible = true

    private var testLoadJob: Job? = null
    private var categoriesLoadJob: Job? = null
    private var categoryProgressJob: Job? = null
    private var testLoadRequestId = 0
    private var categoriesLoadRequestId = 0

    init {
        loadCategories()
    }

    fun loadCategories() {
        categoriesLoadJob?.cancel()
        val requestId = ++categoriesLoadRequestId
        categoriesLoading = true
        loadError = null
        categoriesLoadJob = viewModelScope.launch {
            try {
                val loadedCategories = withContext(Dispatchers.Default) {
                    TestCategories.load()
                }
                if (requestId != categoriesLoadRequestId) return@launch
                val selectedId = testCategory?.id
                categories = loadedCategories
                testCategory = loadedCategories.firstOrNull { it.id == selectedId } ?: loadedCategories.firstOrNull()
                refreshCategoryProgress()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (requestId != categoriesLoadRequestId) return@launch
                categories = emptyList()
                testCategory = null
                loadError = e.message ?: "No se han podido cargar las categorías."
            } finally {
                if (requestId == categoriesLoadRequestId) {
                    categoriesLoading = false
                    categoriesLoadJob = null
                }
            }
        }
    }

    fun selectCategory(category: TestCategory) {
        testCategory = category
        refreshCategoryProgress()
    }

    fun resetProgress(category: TestCategory) {
        ProgressStorage.reset(category)
        refreshCategoryProgress()
    }

    fun startTest(tipo: TipoTest, category: TestCategory) {
        testLoadJob?.cancel()
        stopTimer()
        val requestId = ++testLoadRequestId
        testCategory = category
        loadError = null
        screen = Screen.Loading(tipo)
        testLoadJob = viewModelScope.launch {
            try {
                val allPreguntas = withContext(Dispatchers.Default) {
                    ExtraerPreguntas.extraerPreguntas(category)
                }
                if (requestId != testLoadRequestId) return@launch
                val offset = when (tipo) {
                    TipoTest.SECUENCIAL, TipoTest.BLOQUES_30 ->
                        readProgressStartIndex(category, tipo, allPreguntas.size)
                    TipoTest.ALEATORIO_30, TipoTest.FALLADAS -> 0
                }
                val preguntas = when (tipo) {
                    TipoTest.ALEATORIO_30 -> allPreguntas.shuffled().take(RANDOM_TEST_SIZE)
                    TipoTest.SECUENCIAL -> allPreguntas.drop(offset)
                    TipoTest.BLOQUES_30 -> allPreguntas.drop(offset).take(BLOCK_SIZE)
                    TipoTest.FALLADAS -> FailedQuestions.pending(category, allPreguntas).shuffled()
                }
                if (preguntas.isEmpty()) {
                    // Solo ocurre en falladas cuando ya no queda ninguna.
                    screen = Screen.Welcome
                    refreshCategoryProgress()
                    return@launch
                }
                screen = Screen.Question(
                    TestSession(
                        category = category,
                        tipoTest = tipo,
                        preguntas = preguntas,
                        offsetInicial = offset,
                        totalEnCategoria = allPreguntas.size
                    )
                )
                startTimer()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (requestId != testLoadRequestId) return@launch
                loadError = e.message ?: "No se han podido cargar las preguntas."
                screen = Screen.Welcome
            } finally {
                if (requestId == testLoadRequestId) testLoadJob = null
            }
        }
    }

    fun selectAnswer(opcion: Char) {
        val current = session ?: return
        if (current.answerLocked) return
        val updated = updateSession { it.answer(opcion) } ?: return
        if (updated.tipoTest.showsFeedback) {
            FailedQuestions.record(updated.category, listOf(updated.resultadoActual()))
        }
        saveProgress(updated)
    }

    fun goNext() {
        val current = session ?: return
        if (current.isLast) {
            finalizeTest(current)
        } else {
            updateSession { it.next() }?.let(::saveProgress)
        }
    }

    fun goPrevious() {
        val current = session ?: return
        if (current.isFirst) return
        updateSession { it.previous() }?.let(::saveProgress)
    }

    // El cronómetro solo cuenta mientras la app está a la vista.
    fun onAppVisibilityChanged(visible: Boolean) {
        appVisible = visible
        if (!visible) {
            stopTimer()
        } else if (session != null) {
            startTimer()
        }
    }

    fun goToWelcome() {
        val activeSession = session
        testLoadRequestId++
        testLoadJob?.cancel()
        testLoadJob = null
        stopTimer()
        activeSession?.let(::saveProgress)
        screen = Screen.Welcome
        refreshCategoryProgress()
    }

    fun retry(results: Screen.Results) {
        if (results.tipoTest == TipoTest.SECUENCIAL) {
            ProgressStorage.writeNumber(results.category, TipoTest.SECUENCIAL, 1)
        }
        startTest(results.tipoTest, results.category)
    }

    fun reviewAnswers(results: Screen.Results) {
        screen = Screen.Review(results)
    }

    fun reviewNext() = moveReview(+1)

    fun reviewPrevious() = moveReview(-1)

    fun backToResults() {
        (screen as? Screen.Review)?.let { screen = it.results }
    }

    private fun moveReview(delta: Int) {
        val review = screen as? Screen.Review ?: return
        val index = (review.index + delta).coerceIn(0, review.results.historial.lastIndex)
        screen = review.copy(index = index)
    }

    /** Aplica [transform] a la sesión en curso y la publica. Devuelve la nueva, o null si no hay test. */
    private inline fun updateSession(transform: (TestSession) -> TestSession): TestSession? {
        val current = session ?: return null
        return transform(current).also { screen = Screen.Question(it) }
    }

    private fun refreshCategoryProgress() {
        categoryProgressJob?.cancel()
        val category = testCategory ?: run {
            categoryProgress = null
            return
        }
        // Al volver a la misma categoría se deja el valor anterior hasta tener el nuevo.
        if (categoryProgress?.categoryId != category.id) categoryProgress = null
        categoryProgressJob = viewModelScope.launch {
            try {
                val allPreguntas = withContext(Dispatchers.Default) {
                    ExtraerPreguntas.extraerPreguntas(category)
                }
                val total = allPreguntas.size
                categoryProgress = CategoryProgress(
                    categoryId = category.id,
                    totalQuestions = total,
                    sequentialStartIndex = progressStartIndex(
                        ProgressStorage.readNumber(category, TipoTest.SECUENCIAL),
                        total
                    ),
                    blocksStartIndex = progressStartIndex(
                        ProgressStorage.readNumber(category, TipoTest.BLOQUES_30),
                        total
                    ),
                    failedCount = FailedQuestions.pending(category, allPreguntas).size
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                categoryProgress = null
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        if (!appVisible) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000.milliseconds)
                updateSession { it.tick() } ?: break
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    // Se guarda en cada paso y no solo al salir: si Android cierra la app en segundo
    // plano, el ViewModel se pierde sin pasar por goToWelcome().
    private fun saveProgress(session: TestSession) {
        if (session.tipoTest == TipoTest.SECUENCIAL) {
            ProgressStorage.writeNumber(session.category, TipoTest.SECUENCIAL, session.resumeQuestionNumber)
        }
    }

    private fun finalizeTest(session: TestSession) {
        stopTimer()
        val historial = session.historial()
        // Con corrección inmediata ya se han apuntado al responder.
        if (!session.tipoTest.showsFeedback) {
            FailedQuestions.record(session.category, historial)
        }
        when (session.tipoTest) {
            TipoTest.SECUENCIAL, TipoTest.BLOQUES_30 ->
                ProgressStorage.writeNumber(session.category, session.tipoTest, session.nextQuestionNumber)
            TipoTest.ALEATORIO_30, TipoTest.FALLADAS -> Unit
        }

        screen = Screen.Results(
            category = session.category,
            tipoTest = session.tipoTest,
            historial = historial,
            segundos = session.secondsElapsed,
            categoryCompleted = session.completesCategory,
            failedRemaining = if (session.tipoTest == TipoTest.FALLADAS) {
                FailedQuestions.count(session.category)
            } else {
                0
            }
        )
    }

    private fun readProgressStartIndex(category: TestCategory, mode: TipoTest, total: Int): Int {
        val storedNumber = ProgressStorage.readNumber(category, mode)
        val startIndex = progressStartIndex(storedNumber, total)
        if (total > 0 && storedNumber !in 1..total) {
            ProgressStorage.writeNumber(category, mode, 1)
        }
        return startIndex
    }
}

internal fun progressStartIndex(storedQuestionNumber: Int, total: Int): Int {
    if (total <= 0 || storedQuestionNumber <= 0 || storedQuestionNumber > total) return 0
    return storedQuestionNumber - 1
}
