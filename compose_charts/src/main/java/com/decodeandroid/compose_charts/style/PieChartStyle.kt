package com.decodeandroid.compose_charts.style

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Stable
data class PieChartStyle(
    val labelSize: TextUnit = 12.sp,
    val percentageSize: TextUnit = 12.sp,
    val centerText:String="",
    val visibility: PieChartVisibility = PieChartVisibility(),
    val colors: PieChartColors = PieChartColors(),
)
