package com.decodeandroid.compose_charts.utils

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import kotlin.math.sqrt

class TriangleShape(
    private val cornerRadius: Dp = 15.dp,
    private val tipSize: Dp = 15.dp
): Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val tipSize = with(density) { tipSize.toPx() }
        val cornerRadius = with(density) { cornerRadius.toPx() }

        val path2 = Path().apply {
            val triangleSide = size.width
            val height = triangleSide * (sqrt(3.0) / 2.0).toFloat()
            moveTo(triangleSide / 2f, 0f)
            lineTo(0f, height)
            lineTo(triangleSide, height)
            close()
        }

        val roundedPolygon = RoundedPolygon(
            numVertices = 3,
            radius = size.minDimension / 2,
            centerX = size.width / 2,
            centerY = size.height / 2,
            rounding = CornerRounding(
                size.minDimension / 10f,
                smoothing = 0.1f
            )
        )
        val roundedPolygonPath = roundedPolygon.toPath().asComposePath()

        return Outline.Generic(roundedPolygonPath)
    }
}