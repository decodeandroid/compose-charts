package com.decodeandroid.compose_charts.style

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Stable
data class DonutChartStyle(
    val percentageSize: TextUnit = 12.sp,
    val centerText:String="",
    val visibility: DonutChartVisibility = DonutChartVisibility(),
    val colors: PieChartColors = PieChartColors(),
    val textStyle: TextStyle = TextStyle(),
)