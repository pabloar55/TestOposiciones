package com.pablo.testoposiciones.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import testoposiciones.shared.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun QuestionIllustration(path: String?) {
    if (path == null) return
    val image by produceState<Result<ImageBitmap>?>(initialValue = null, key1 = path) {
        value = null
        value = try {
            Result.success(Res.readBytes(path).decodeToImageBitmap())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
    var expanded by remember(path) { mutableStateOf(false) }
    val bitmap = image?.getOrNull()

    Column(Modifier.padding(bottom = 24.dp)) {
        when {
            image == null -> LinearProgressIndicator(Modifier.fillMaxWidth())
            bitmap == null -> Text(
                "No se ha podido cargar la ilustración de esta pregunta.",
                color = MaterialTheme.colorScheme.error
            )
            else -> {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Ilustración de la pregunta. Pulsa para ampliar.",
                    modifier = Modifier
                        .widthIn(max = 680.dp)
                        .width((bitmap.width / 2f).dp)
                        .background(Color.White)
                        .clickable(onClickLabel = "Ampliar ilustración") { expanded = true }
                )
                TextButton(onClick = { expanded = true }) { Text("Ampliar imagen") }
            }
        }
    }

    if (expanded && bitmap != null) {
        Dialog(
            onDismissRequest = { expanded = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    TextButton(onClick = { expanded = false }) { Text("Cerrar imagen") }
                    Text("Desliza para ver toda la imagen.")
                    Box(
                        Modifier.weight(1f).fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Ilustración ampliada de la pregunta",
                            modifier = Modifier.width(900.dp).background(Color.White)
                        )
                    }
                }
            }
        }
    }
}
