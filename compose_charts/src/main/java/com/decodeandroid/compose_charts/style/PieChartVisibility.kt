package com.decodeandroid.compose_charts.style

import androidx.compose.runtime.Stable


@Stable
data class PieChartVisibility(
    val isLabelVisible: Boolean = false,
    val isCenterCircleVisible: Boolean = false,
    val isCenterTextVisible: Boolean = false,
)
