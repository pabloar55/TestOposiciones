package com.pablo.testoposiciones.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pablo.testoposiciones.model.CategoryProgress
import com.pablo.testoposiciones.model.TestCategory
import com.pablo.testoposiciones.model.TipoTest
import com.pablo.testoposiciones.ui.icons.check
import com.pablo.testoposiciones.ui.icons.dark_mode
import com.pablo.testoposiciones.ui.icons.expand_more
import com.pablo.testoposiciones.ui.icons.format_list_numbered
import com.pablo.testoposiciones.ui.icons.light_mode
import com.pablo.testoposiciones.ui.icons.more_vert
import com.pablo.testoposiciones.ui.icons.replay
import com.pablo.testoposiciones.ui.icons.shuffle
import com.pablo.testoposiciones.ui.icons.view_agenda
import kotlinx.coroutines.launch

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
    var showCategorySheet by remember { mutableStateOf(false) }
    val canStart = selectedCategory != null && !categoriesLoading && loadError == null
    // Mientras se calcula el progreso de otra categoría no se muestra el de la anterior.
    val progress = categoryProgress?.takeIf { it.categoryId == selectedCategory?.id }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 8.dp, bottom = 24.dp)
        ) {
            Header(
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme,
                canReset = canStart,
                onResetProgress = { showResetDialog = true }
            )

            Column(Modifier.padding(end = 12.dp)) {
                Spacer(Modifier.height(8.dp))
                when {
                    categoriesLoading -> Box(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }

                    loadError != null -> LoadErrorCard(loadError, onRetryLoad)

                    selectedCategory != null -> CategoryCard(
                        category = selectedCategory,
                        progress = progress,
                        onClick = { showCategorySheet = true }
                    )
                }

                Spacer(Modifier.height(28.dp))
                Text(
                    text = "Elige cómo practicar",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ModeGrid(
                    selectedCategory = selectedCategory,
                    canStart = canStart,
                    progress = progress,
                    onStartTest = onStartTest
                )
            }
        }

        if (showCategorySheet) {
            CategorySheet(
                categories = categories,
                selectedCategory = selectedCategory,
                onSelected = onCategorySelected,
                onDismiss = { showCategorySheet = false }
            )
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
private fun Header(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    canReset: Boolean,
    onResetProgress: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Test Oposiciones",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onToggleTheme) {
            Icon(
                imageVector = if (darkTheme) light_mode else dark_mode,
                contentDescription = if (darkTheme) "Cambiar a tema claro" else "Cambiar a tema oscuro",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    imageVector = more_vert,
                    contentDescription = "Más opciones",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Reiniciar progreso") },
                    enabled = canReset,
                    onClick = {
                        menuOpen = false
                        onResetProgress()
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: TestCategory,
    progress: CategoryProgress?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f).alpha(0.8f)
                )
                Text("Cambiar", style = MaterialTheme.typography.labelLarge)
                Icon(expand_more, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(12.dp))
            val facts = buildList {
                if (progress == null) {
                    add("Cargando…")
                } else {
                    add("${progress.totalQuestions} preguntas")
                    if (category.exams.size > 1) add("${category.exams.size} exámenes")
                    when (progress.failedCount) {
                        0 -> Unit
                        1 -> add("1 fallada")
                        else -> add("${progress.failedCount} falladas")
                    }
                }
            }
            Text(
                text = facts.joinToString("  ·  "),
                style = MaterialTheme.typography.bodyMedium.merge(TabularNumbers),
                modifier = Modifier.alpha(0.85f)
            )
        }
    }
}

@Composable
private fun LoadErrorCard(message: String, onRetry: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("No se ha podido cargar el temario", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(message, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySheet(
    categories: List<TestCategory>,
    selectedCategory: TestCategory?,
    onSelected: (TestCategory) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Text(
            text = "Elige categoría",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
        )
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
            categories.forEach { category ->
                val selected = category.id == selectedCategory?.id
                ListItem(
                    headlineContent = { Text(category.displayName) },
                    supportingContent = if (category.exams.size > 1) {
                        { Text("${category.exams.size} exámenes") }
                    } else {
                        null
                    },
                    trailingContent = if (selected) {
                        { Icon(check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else {
                        null
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = if (selected) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            Color.Transparent
                        }
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(role = Role.RadioButton) {
                            onSelected(category)
                            scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                        }
                )
            }
        }
    }
}

@Composable
private fun ModeGrid(
    selectedCategory: TestCategory?,
    canStart: Boolean,
    progress: CategoryProgress?,
    onStartTest: (TipoTest, TestCategory) -> Unit
) {
    val failedCount = progress?.failedCount ?: 0
    fun start(tipo: TipoTest) {
        selectedCategory?.let { onStartTest(tipo, it) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModeTile(
                icon = shuffle,
                title = "Aleatorio",
                description = "30 preguntas al azar, corregidas al momento.",
                status = progress?.let { "De entre ${it.totalQuestions}" },
                enabled = canStart,
                onClick = { start(TipoTest.ALEATORIO_30) },
                modifier = Modifier.weight(1f)
            )
            ModeTile(
                icon = view_agenda,
                title = "Bloques de 30",
                description = "En orden y de 30 en 30. Corriges al terminar.",
                status = progress?.let {
                    if (it.blocksStartIndex == 0) "${it.totalBlocks} bloques"
                    else "Bloque ${it.currentBlock} de ${it.totalBlocks}"
                },
                progress = progress?.takeIf { it.blocksStartIndex > 0 }
                    ?.let { it.blocksStartIndex.toFloat() / it.totalQuestions },
                enabled = canStart,
                onClick = { start(TipoTest.BLOQUES_30) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModeTile(
                icon = format_list_numbered,
                title = "Seguidas",
                description = "Todo el temario en orden. Retomas donde lo dejaste.",
                status = progress?.let {
                    if (it.sequentialStartIndex == 0) "${it.totalQuestions} preguntas"
                    else "Vas por la ${it.sequentialStartIndex + 1} de ${it.totalQuestions}"
                },
                progress = progress?.takeIf { it.sequentialStartIndex > 0 }
                    ?.let { it.sequentialStartIndex.toFloat() / it.totalQuestions },
                enabled = canStart,
                onClick = { start(TipoTest.SECUENCIAL) },
                modifier = Modifier.weight(1f)
            )
            ModeTile(
                icon = replay,
                title = "Falladas",
                description = "Salen de la lista en cuanto las aciertas.",
                status = progress?.let {
                    if (failedCount > 0) "$failedCount por repasar" else "Ninguna por ahora"
                },
                highlighted = failedCount > 0,
                enabled = canStart && failedCount > 0,
                onClick = { start(TipoTest.FALLADAS) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeTile(
    icon: ImageVector,
    title: String,
    description: String,
    status: String?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    highlighted: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    // Falladas pendientes se marcan en ámbar: es lo que más conviene repasar.
    val container = if (highlighted) colors.tertiaryContainer else colors.surfaceContainerLow
    val content = if (highlighted) colors.onTertiaryContainer else colors.onSurface
    val iconContainer = if (highlighted) colors.tertiary else colors.secondaryContainer
    val iconTint = if (highlighted) colors.onTertiary else colors.onSecondaryContainer
    val accent = if (highlighted) colors.onTertiaryContainer else colors.primary

    Surface(
        onClick = onClick,
        enabled = enabled,
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
                .alpha(if (enabled) 1f else 0.5f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconContainer, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (highlighted) content else colors.onSurfaceVariant
            )
            // Empuja el estado abajo para que las dos fichas de cada fila queden alineadas.
            Spacer(Modifier.height(12.dp))
            Spacer(Modifier.weight(1f))
            if (status != null) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelMedium.merge(TabularNumbers),
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
            if (progress != null) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = colors.surfaceContainerHighest,
                    drawStopIndicator = {}
                )
            }
        }
    }
}
