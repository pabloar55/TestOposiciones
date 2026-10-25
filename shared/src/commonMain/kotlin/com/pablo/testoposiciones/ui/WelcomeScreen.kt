package com.pablo.testoposiciones.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pablo.testoposiciones.model.CategoryProgress
import com.pablo.testoposiciones.model.TestCategory
import com.pablo.testoposiciones.model.TipoTest
import com.pablo.testoposiciones.ui.icons.arrow_forward
import com.pablo.testoposiciones.ui.icons.dark_mode
import com.pablo.testoposiciones.ui.icons.light_mode

@Composable
fun WelcomeScreen(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    categories: List<TestCategory>,
    categoriesLoading: Boolean,
    loadError: String?,
    selectedCategory: TestCategory?,
    categoryProgress: CategoryProgress?,
    onRetryLoad: () -> Unit,
    onCategorySelected: (TestCategory) -> Unit,
    onResetProgress: (TestCategory) -> Unit,
    onStartTest: (TipoTest, TestCategory) -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    val canStart = selectedCategory != null && !categoriesLoading && loadError == null

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useTwoPaneLayout = maxWidth >= 720.dp

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 1040.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (darkTheme) light_mode else dark_mode,
                        contentDescription = if (darkTheme) "Cambiar a tema claro" else "Cambiar a tema oscuro",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = "Test Oposiciones",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Text(
                text = "Elige una categoría y el modo de test",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            if (useTwoPaneLayout) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    CategoryPanel(
                        categories = categories,
                        categoriesLoading = categoriesLoading,
                        loadError = loadError,
                        selectedCategory = selectedCategory,
                        canStart = canStart,
                        onRetryLoad = onRetryLoad,
                        onCategorySelected = onCategorySelected,
                        onResetProgress = { showResetDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    ModePanel(
                        selectedCategory = selectedCategory,
                        canStart = canStart,
                        progress = categoryProgress,
                        onStartTest = onStartTest,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .widthIn(max = 680.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CategoryPanel(
                        categories = categories,
                        categoriesLoading = categoriesLoading,
                        loadError = loadError,
                        selectedCategory = selectedCategory,
                        canStart = canStart,
                        onRetryLoad = onRetryLoad,
                        onCategorySelected = onCategorySelected,
                        onResetProgress = { showResetDialog = true }
                    )
                    ModePanel(
                        selectedCategory = selectedCategory,
                        canStart = canStart,
                        progress = categoryProgress,
                        onStartTest = onStartTest
                    )
                }
            }
        }

        if (showResetDialog && selectedCategory != null) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reiniciar progreso") },
                text = {
                    Text(
                        "Se reiniciará el progreso de ${selectedCategory.displayName}. " +
                            "Preguntas seguidas y Bloques de 30 volverán a empezar desde la pregunta 1. " +
                            "Las preguntas falladas se mantienen."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onResetProgress(selectedCategory)
                            showResetDialog = false
                        }
                    ) {
                        Text("Reiniciar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun CategoryPanel(
    categories: List<TestCategory>,
    categoriesLoading: Boolean,
    loadError: String?,
    selectedCategory: TestCategory?,
    canStart: Boolean,
    onRetryLoad: () -> Unit,
    onCategorySelected: (TestCategory) -> Unit,
    onResetProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            categoriesLoading -> CircularProgressIndicator(modifier = Modifier.padding(bottom = 24.dp))
            loadError != null -> {
                Text(
                    text = loadError,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedButton(
                    onClick = onRetryLoad,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text("Reintentar carga")
                }
            }
            else -> CategorySelector(
                categories = categories,
                selectedCategory = selectedCategory,
                onSelected = onCategorySelected
            )
        }

        if (canStart && selectedCategory != null) {
            TextButton(
                onClick = onResetProgress,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Reiniciar progreso de ${selectedCategory.displayName}")
            }
        }
    }
}

@Composable
private fun ModePanel(
    selectedCategory: TestCategory?,
    canStart: Boolean,
    progress: CategoryProgress?,
    onStartTest: (TipoTest, TestCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val failedCount = progress?.failedCount ?: 0

    Column(modifier = modifier.fillMaxWidth()) {
        ModeButton(
            title = "Test aleatorio",
            description = "Hasta 30 preguntas escogidas al azar. Muestra feedback inmediato.",
            status = progress?.let { "De entre ${it.totalQuestions} preguntas" },
            enabled = canStart,
            onClick = { selectedCategory?.let { onStartTest(TipoTest.ALEATORIO_30, it) } }
        )

        Spacer(Modifier.height(16.dp))

        ModeButton(
            title = "Bloques de hasta 30",
            description = "Preguntas consecutivas. Reanuda desde el último bloque completado.",
            status = progress?.let {
                if (it.blocksStartIndex == 0) "${it.totalBlocks} bloques · empiezas por el primero"
                else "Vas por el bloque ${it.currentBlock} de ${it.totalBlocks}"
            },
            progress = progress?.takeIf { it.blocksStartIndex > 0 }
                ?.let { it.blocksStartIndex.toFloat() / it.totalQuestions },
            enabled = canStart,
            onClick = { selectedCategory?.let { onStartTest(TipoTest.BLOQUES_30, it) } }
        )

        Spacer(Modifier.height(16.dp))

        ModeButton(
            title = "Preguntas seguidas",
            description = "Todas las preguntas en orden. Reanuda desde donde lo dejaste.",
            status = progress?.let {
                if (it.sequentialStartIndex == 0) "${it.totalQuestions} preguntas · empiezas por la primera"
                else "Vas por la pregunta ${it.sequentialStartIndex + 1} de ${it.totalQuestions}"
            },
            progress = progress?.takeIf { it.sequentialStartIndex > 0 }
                ?.let { it.sequentialStartIndex.toFloat() / it.totalQuestions },
            enabled = canStart,
            onClick = { selectedCategory?.let { onStartTest(TipoTest.SECUENCIAL, it) } }
        )

        Spacer(Modifier.height(16.dp))

        ModeButton(
            title = if (failedCount > 0) "Repasar falladas ($failedCount)" else "Repasar falladas",
            description = if (failedCount > 0) {
                "Las preguntas que has fallado en esta categoría. Al acertarlas salen de la lista."
            } else {
                "Aún no tienes preguntas falladas en esta categoría."
            },
            enabled = canStart && failedCount > 0,
            onClick = { selectedCategory?.let { onStartTest(TipoTest.FALLADAS, it) } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySelector(
    categories: List<TestCategory>,
    selectedCategory: TestCategory?,
    onSelected: (TestCategory) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = category.id == selectedCategory?.id,
                onClick = { onSelected(category) },
                label = { Text(text = category.displayName) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun ModeButton(
    title: String,
    description: String,
    enabled: Boolean,
    status: String? = null,
    progress: Float? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (status != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (progress != null) {
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        drawStopIndicator = {}
                    )
                }
            }
            Icon(
                imageVector = arrow_forward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
