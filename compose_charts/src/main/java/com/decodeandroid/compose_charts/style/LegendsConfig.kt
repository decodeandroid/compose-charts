package com.decodeandroid.compose_charts.style

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * LegendsConfig data class params used in config label in graph.
 * @param legendLabelList: stackLabelList is used to show labels with colors
 * @param gridColumnCount : Column Count for stackLabel grid
 * @param gridPaddingHorizontal : Horizontal padding for stackLabel grid
 * @param gridPaddingVertical :  Vertical padding for stackLabel grid
 * @param spaceBWLabelAndColorBox: Space between Label and ColorBox for stackLabel grid item
 * @param colorBoxSize: Blend mode for the groupSeparator
 * @param textStyle: TextStyle for label
 *  */
data class LegendsConfig(
    val legendLabelList: List<LegendLabel> = emptyList(),
    val gridColumnCount: Int = 1,
    val gridPaddingHorizontal: Dp = 8.dp,
    val gridPaddingVertical: Dp = 8.dp,
    val colorBoxSize: Dp = 20.dp,
    val textStyle: TextStyle = TextStyle(),
    val spaceBWLabelAndColorBox: Dp = 5.dp,
    val legendsArrangement: Arrangement.Horizontal = Arrangement.Center,
    val legendAxis: LegendAxis = LegendAxis.HORIZONTAL,
    val legendShape: LegendShape = LegendShape.Circle
)

enum class LegendAxis{
    VERTICAL,HORIZONTAL
}

enum class LegendShape{
    Triangle,Circle,RoundCorner
}

/**
 * LegendLabel data class params used in drawing label in graph.
 * @param color : Color of label.
 * @param name : Name of label.
 * @param brush : Used for gradient color
 *  */
data class LegendLabel(
    val color: Color = Color.Black,
    val name: String,
    val brush: Brush? = null
)
