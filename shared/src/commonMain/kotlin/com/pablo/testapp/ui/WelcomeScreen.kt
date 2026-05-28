package com.pablo.testapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pablo.testapp.model.TestCategory
import com.pablo.testapp.model.TipoTest
import com.pablo.testapp.ui.icons.dark_mode
import com.pablo.testapp.ui.icons.light_mode

@Composable
fun WelcomeScreen(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    categories: List<TestCategory>,
    categoriesLoading: Boolean,
    loadError: String?,
    onRetryLoad: () -> Unit,
    onStartTest: (TipoTest, TestCategory) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<TestCategory?>(null) }

    LaunchedEffect(categories) {
        if (selectedCategory == null || categories.none { it.id == selectedCategory?.id }) {
            selectedCategory = categories.firstOrNull()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (darkTheme) light_mode else dark_mode,
                contentDescription = if (darkTheme) "Cambiar a tema claro" else "Cambiar a tema oscuro",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Test Oposiciones",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Elige una categoria y el modo de test",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            when {
                categoriesLoading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(bottom = 24.dp))
                }
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
                    onSelected = { selectedCategory = it }
                )
            }

            val canStart = selectedCategory != null && !categoriesLoading && loadError == null

            ModeButton(
                title = "30 Preguntas Aleatorias",
                description = "30 preguntas escogidas al azar. Muestra feedback inmediato.",
                enabled = canStart,
                onClick = { selectedCategory?.let { onStartTest(TipoTest.ALEATORIO_30, it) } }
            )

            Spacer(Modifier.height(16.dp))

            ModeButton(
                title = "Bloques de 30",
                description = "30 preguntas en bloques consecutivos. Reanuda desde el ultimo bloque.",
                enabled = canStart,
                onClick = { selectedCategory?.let { onStartTest(TipoTest.BLOQUES_30, it) } }
            )

            Spacer(Modifier.height(16.dp))

            ModeButton(
                title = "Preguntas Seguidas",
                description = "Todas las preguntas en orden. Reanuda desde donde lo dejaste.",
                enabled = canStart,
                onClick = { selectedCategory?.let { onStartTest(TipoTest.SECUENCIAL, it) } }
            )
        }
    }
}

@Composable
private fun CategorySelector(
    categories: List<TestCategory>,
    selectedCategory: TestCategory?,
    onSelected: (TestCategory) -> Unit
) {
    Row(
        modifier = Modifier.padding(bottom = 24.dp).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category.id == selectedCategory?.id

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        onClick = { onSelected(category) },
                        role = Role.RadioButton
                    )
                    // Opcional: Puedes agregar padding aquí para que el área táctil sea más cómoda
                    .padding(end = 8.dp)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = category.displayName,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ModeButton(
    title: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(80.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Text(
                text = description,
                fontSize = 12.sp,
                color = LocalContentColor.current.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
