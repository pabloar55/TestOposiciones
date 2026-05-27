package com.pablo.testapp

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
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
    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        val vm: TestViewModel = viewModel { TestViewModel() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
        ) {
            when (val screen = vm.screen) {
                is Screen.Welcome -> WelcomeScreen(onStartTest = { vm.startTest(it) })

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
                            onClose = { vm.goToWelcome() }
                        )
                    }
                }

                is Screen.Results -> ResultsScreen(
                    correctas = screen.correctas,
                    incorrectas = screen.incorrectas,
                    total = screen.total,
                    segundos = screen.segundos,
                    tipoTest = screen.tipoTest,
                    historial = screen.historial,
                    onRetry = { vm.retry() },
                    onReview = { vm.reviewAnswers(screen.historial) },
                    onBack = { vm.goToWelcome() }
                )

                is Screen.Review -> {
                    var reviewIndex by remember(screen) { mutableStateOf(0) }
                    ReviewScreen(
                        historial = screen.historial,
                        currentIndex = reviewIndex,
                        onNext = { if (reviewIndex < screen.historial.size - 1) reviewIndex++ },
                        onPrevious = { if (reviewIndex > 0) reviewIndex-- },
                        onClose = {
                            // Return to the last results. We don't store them in vm, so go to welcome.
                            vm.goToWelcome()
                        }
                    )
                }
            }
        }
    }
}
