import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.pablo.testoposiciones.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TestOposiciones"
            packageVersion = "1.0.1"

            windows {
                // Crea una entrada en el menú de inicio dentro de este grupo
                menuGroup = "TestOposiciones"
                // Crea también un acceso directo en el escritorio
                shortcut = true
                // Permite al usuario elegir el directorio de instalación
                dirChooser = true
                // UUID estable: en futuras actualizaciones el MSI reemplaza la
                // versión anterior en lugar de instalarla en paralelo.
                // Genera uno UNA sola vez y no lo cambies más.
                upgradeUuid = "6969483c-a30b-4ec1-abd9-3eba6e2b39ec"
            }
        }
    }
}