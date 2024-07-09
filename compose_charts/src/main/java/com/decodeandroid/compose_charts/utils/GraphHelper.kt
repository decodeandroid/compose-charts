package com.decodeandroid.compose_charts.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal object GraphHelper {

    internal val DEFAULT_GRAPH_SIZE = 300.dp

    fun centerOf(offset1: Offset, offset2: Offset): Offset {
        val xOffset = (offset1.x + offset2.x) / 2
        val yOffset = (offset1.y + offset2.y) / 2
        return Offset(xOffset, yOffset)
    }

    internal fun getOffsetOfAngle(angle: Float, radius: Float, pieSize: Size): Offset {
        val pieCenter = Offset(x = pieSize.width/2, y = pieSize.height/2)
        return Offset(
            x = (cos(Math.toRadians(angle.toDouble())) * radius + pieCenter.x).toFloat(),
            y = (sin(Math.toRadians(angle.toDouble())) * radius + pieCenter.y).toFloat()
        )
    }

    internal fun getAngleFromOffset(offset: Offset, radius: Float): Float {
        val angle = Math.toDegrees(
            atan2(
                offset.y - radius.toDouble(),
                offset.x - radius.toDouble()
            )
        )

        return angle.toFloat()
    }

    internal fun DrawScope.centeredText(
        speed: String,
        textColor: Color,
        textMeasurer: TextMeasurer,
        textOffset: Offset
    ) {

        val textLayoutResult = textMeasurer.measure(
            text = speed,
            style = TextStyle.Default.copy(lineHeight = TextUnit(0.0f, TextUnitType.Sp))
        )
        val textWidth = textLayoutResult.size.width
        val textHeight = textLayoutResult.size.height

        drawContext.canvas.save()
        // Translate to the text offset point, adjusting for vertical centering.
        drawContext.canvas.translate(
            textOffset.x - textWidth / 2,
            textOffset.y - textHeight / 2
        )

        drawText(textLayoutResult, color = textColor)

        drawContext.canvas.restore()
    }

    /*
    * to create random color for our graph
    * @Return Color
    * */
    internal fun getRandomColor(): Color {
        return Color(
            red = (0..255).random(),
            blue =  (0..255).random(),
            green =  (0..255).random()
        )
    }


}