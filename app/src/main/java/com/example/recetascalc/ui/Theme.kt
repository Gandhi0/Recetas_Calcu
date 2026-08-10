package com.example.recetascalc.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Background = Color(0xFF0A0A0A)
val Card = Color(0xFF1B1B1B)
val Field = Color(0xFF262626)
val Accent = Color(0xFFC79A5E)
val TextSec = Color(0xFF9C9C9C)
val Danger = Color(0xFFE5484D)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Background,
            surface = Card,
            primary = Accent,
            onPrimary = Color.Black,
            onSurface = Color.White,
            onBackground = Color.White,
            outline = Color(0xFF333333)
        ),
        content = content
    )
}

@Composable
fun transparentField(): TextFieldColors = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = Accent
)

fun parseDouble(s: String): Double = s.replace(',', '.').trim().toDoubleOrNull() ?: 0.0

fun fmt(d: Double): String {
    val v = kotlin.math.round(d * 100) / 100
    return if (v == Math.floor(v)) v.toLong().toString()
    else String.format(java.util.Locale.US, "%.2f", v).trimEnd('0')
}
