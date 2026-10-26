package com.pablo.testoposiciones.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

// Iconos de Material Icons (24dp, relleno) a partir de su path SVG. Así no hace falta
// la dependencia de iconos extendidos para unos pocos.
private fun materialIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes(pathData),
        fill = SolidColor(Color.Black)
    ).build()

val shuffle: ImageVector by lazy {
    materialIcon(
        "shuffle",
        "M10.59,9.17L5.41,4 4,5.41l5.17,5.17 1.42,-1.41zM14.5,4l2.04,2.04L4,18.59 5.41,20 17.96,7.46 20,9.5L20,4h-5.5z" +
            "M14.83,13.41l-1.41,1.41 3.13,3.13L14.5,20L20,20v-5.5l-2.04,2.04 -3.13,-3.13z"
    )
}

val view_agenda: ImageVector by lazy {
    materialIcon(
        "view_agenda",
        "M20,13H3c-0.55,0 -1,0.45 -1,1v6c0,0.55 0.45,1 1,1h17c0.55,0 1,-0.45 1,-1v-6c0,-0.55 -0.45,-1 -1,-1z" +
            "M20,3H3c-0.55,0 -1,0.45 -1,1v6c0,0.55 0.45,1 1,1h17c0.55,0 1,-0.45 1,-1V4c0,-0.55 -0.45,-1 -1,-1z"
    )
}

val format_list_numbered: ImageVector by lazy {
    materialIcon(
        "format_list_numbered",
        "M2,17h2v0.5H3v1h1v0.5H2v1h3v-4H2v1zM3,8h1V4H2v1h1v3zM2,11h1.8L2,13.1v0.9h3v-1H3.2L5,10.9V10H2v1z" +
            "M7,5v2h14V5H7zM7,19h14v-2H7v2zM7,13h14v-2H7v2z"
    )
}

val replay: ImageVector by lazy {
    materialIcon(
        "replay",
        "M12,5V1L7,6l5,5V7c3.31,0 6,2.69 6,6s-2.69,6 -6,6 -6,-2.69 -6,-6H4c0,4.42 3.58,8 8,8s8,-3.58 8,-8 -3.58,-8 -8,-8z"
    )
}

val close: ImageVector by lazy {
    materialIcon(
        "close",
        "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z"
    )
}

val check: ImageVector by lazy {
    materialIcon("check", "M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z")
}

val more_vert: ImageVector by lazy {
    materialIcon(
        "more_vert",
        "M12,8c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2zM12,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z" +
            "M12,16c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z"
    )
}

val expand_more: ImageVector by lazy {
    materialIcon("expand_more", "M16.59,8.59L12,13.17 7.41,8.59 6,10l6,6 6,-6z")
}

val schedule: ImageVector by lazy {
    materialIcon(
        "schedule",
        "M11.99,2C6.47,2 2,6.48 2,12s4.47,10 9.99,10C17.52,22 22,17.52 22,12S17.52,2 11.99,2zM12,20c-4.42,0 -8,-3.58 -8,-8s3.58,-8 8,-8 8,3.58 8,8 -3.58,8 -8,8z" +
            "M12.5,7H11v6l5.25,3.15 0.75,-1.23 -4.5,-2.67z"
    )
}
