package com.pablo.testoposiciones

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pablo.testoposiciones.logic.Screen
import com.pablo.testoposiciones.logic.TestViewModel
import com.pablo.testoposiciones.model.TipoTest
import com.pablo.testoposiciones.platform.ProgressStorage
import com.pablo.testoposiciones.ui.AppTheme
import com.pablo.testoposiciones.ui.QuestionScreen
import com.pablo.testoposiciones.ui.ResultsScreen
import com.pablo.testoposiciones.ui.ReviewScreen
import com.pablo.testoposiciones.ui.WelcomeScreen

@Composable
fun App() {
    val systemDarkTheme = isSystemInDarkTheme()
    var savedDarkTheme by remember { mutableStateOf(ProgressStorage.readDarkTheme()) }
    val darkTheme = savedDarkTheme ?: systemDarkTheme

    ApplySystemBarTheme(darkTheme)

    AppTheme(darkTheme) {
        val vm: TestViewModel = viewModel { TestViewModel() }
        val screen = vm.screen
        var showExitTestDialog by remember { mutableStateOf(false) }

        LifecycleStartEffect(vm) {
            vm.onAppVisibilityChanged(true)
            onStopOrDispose { vm.onAppVisibilityChanged(false) }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                // El contenido ya define su propio margen horizontal. Aplicar también
                // safeContent en los cuatro lados duplicaba ese espacio en dispositivos
                // que informan insets laterales amplios, como el Pixel 10.
                .windowInsetsPadding(
                    WindowInsets.safeContent.only(WindowInsetsSides.Vertical)
                )
        ) {
            when (screen) {
                is Screen.Welcome -> WelcomeScreen(
                    darkTheme = darkTheme,
                    onToggleTheme = {
                        savedDarkTheme = !darkTheme
                        ProgressStorage.writeDarkTheme(!darkTheme)
                    },
                    categories = vm.categories,
                    categoriesLoading = vm.categoriesLoading,
                    loadError = vm.loadError,
                    selectedCategory = vm.testCategory,
                    categoryProgress = vm.categoryProgress,
                    onRetryLoad = { vm.loadCategories() },
                    onCategorySelected = { vm.selectCategory(it) },
                    onResetProgress = { vm.resetProgress(it) },
                    onStartTest = { tipo, category ->
                        showExitTestDialog = false
                        vm.startTest(tipo, category)
                    }
                )

                is Screen.Question -> {
                    if (vm.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        QuestionScreen(
                            preguntas = vm.preguntas,
                            currentIndex = vm.currentIndex,
                            displayQuestionNumber = vm.displayQuestionNumber,
                            displayTotal = vm.displayQuestionTotal,
                            tipoTest = vm.tipoTest,
                            userAnswers = vm.userAnswers,
                            secondsElapsed = vm.secondsElapsed,
                            onAnswer = { vm.selectAnswer(it) },
                            onNext = { vm.goNext() },
                            onPrevious = { vm.goPrevious() },
                            onClose = { showExitTestDialog = true }
                        )
                    }
                }

                is Screen.Results -> ResultsScreen(
                    correctas = screen.correctas,
                    incorrectas = screen.incorrectas,
                    total = screen.total,
                    segundos = screen.segundos,
                    tipoTest = screen.tipoTest,
                    categoryCompleted = screen.categoryCompleted,
                    failedRemaining = screen.failedRemaining,
                    onRetry = { vm.retry() },
                    onReview = { vm.reviewAnswers(screen) },
                    onBack = { vm.goToWelcome() }
                )

                is Screen.Review -> {
                    var reviewIndex by remember(screen) { mutableIntStateOf(0) }
                    ReviewScreen(
                        historial = screen.historial,
                        currentIndex = reviewIndex,
                        onNext = { if (reviewIndex < screen.historial.size - 1) reviewIndex++ },
                        onPrevious = { if (reviewIndex > 0) reviewIndex-- },
                        onClose = {
                            vm.backToResults(screen.results)
                        }
                    )
                }
            }

            AppBackHandler(enabled = screen !is Screen.Welcome) {
                when (screen) {
                    is Screen.Question -> showExitTestDialog = true
                    is Screen.Results -> vm.goToWelcome()
                    is Screen.Review -> vm.backToResults(screen.results)
                    is Screen.Welcome -> Unit
                }
            }

            if (showExitTestDialog && screen is Screen.Question) {
                AlertDialog(
                    onDismissRequest = { showExitTestDialog = false },
                    title = { Text("Salir del test") },
                    text = {
                        Text(
                            when (vm.tipoTest) {
                                TipoTest.SECUENCIAL -> "Se guardará tu progreso. ¿Quieres volver al inicio?"
                                TipoTest.FALLADAS ->
                                    "Las que ya has acertado han salido de falladas. ¿Quieres volver al inicio?"
                                else -> "¿Quieres volver al inicio?"
                            }
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showExitTestDialog = false
                                vm.goToWelcome()
                            }
                        ) {
                            Text("Salir")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExitTestDialog = false }) {
                            Text("Continuar")
                        }
                    }
                )
            }
        }
    }
}
