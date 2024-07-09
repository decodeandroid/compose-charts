package com.decodeandroid.compose_charts.style

import androidx.compose.runtime.Stable

@Stable
data class DonutChartVisibility(
    val isLabelVisible: Boolean = false,
    val isDescriptionVisible: Boolean = false,
    val isCenterTextVisible: Boolean = false,
)