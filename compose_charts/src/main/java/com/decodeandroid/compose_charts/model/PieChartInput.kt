package com.decodeandroid.compose_charts.model

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.graphics.Color
import com.decodeandroid.compose_charts.utils.GraphHelper.getRandomColor

data class PieChartInput(
    val color: Color = getRandomColor(),
    val value: Int,
    val label: String,
    val isTapped: Boolean = false,
    val animation: Animatable<Float, AnimationVector1D> = Animatable(0f),
    val targetSweepAngle: Float = 0f,
    val startAngle: Float = 0f,
    val endAngle: Float = 0f,
)