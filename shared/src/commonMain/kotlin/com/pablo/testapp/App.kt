package com.pablo.testapp

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pablo.testapp.logic.Screen
import com.pablo.testapp.logic.TestViewModel
import com.pablo.testapp.ui.QuestionScreen
import com.pablo.testapp.ui.ResultsScreen
import com.pablo.testapp.ui.ReviewScreen
import com.pablo.testapp.ui.WelcomeScreen

@Composable
fun App() {
    val systemDarkTheme = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(systemDarkTheme) }
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

    ApplySystemBarTheme(darkTheme)

    MaterialTheme(colorScheme = colorScheme) {
        val vm: TestViewModel = viewModel { TestViewModel() }
        val screen = vm.screen
        var showExitTestDialog by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
        ) {
            when (screen) {
                is Screen.Welcome -> WelcomeScreen(
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                    onStartTest = {
                        showExitTestDialog = false
                        vm.startTest(it)
                    }
                )

                is Screen.Question -> {
                    if (vm.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        QuestionScreen(
                            preguntas = vm.preguntas,
                            currentIndex = vm.currentIndex,
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
                    text = { Text("Se guardara tu progreso. Quieres volver al inicio?") },
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
