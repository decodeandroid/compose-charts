package com.decodeandroid.compose_charts

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decodeandroid.compose_charts.utils.GraphHelper.DEFAULT_GRAPH_SIZE
import com.decodeandroid.compose_charts.utils.GraphHelper.centerOf
import com.decodeandroid.compose_charts.utils.GraphHelper.centeredText
import com.decodeandroid.compose_charts.utils.GraphHelper.getOffsetOfAngle
import com.decodeandroid.compose_charts.model.PieChartInput
import com.decodeandroid.compose_charts.style.LegendAxis
import com.decodeandroid.compose_charts.style.LegendLabel
import com.decodeandroid.compose_charts.style.Legends
import com.decodeandroid.compose_charts.style.LegendsConfig
import com.decodeandroid.compose_charts.style.PieChartStyle
import com.decodeandroid.compose_charts.ui.gray
import com.decodeandroid.compose_charts.ui.white
import com.decodeandroid.compose_charts.utils.AutoResizeText
import com.decodeandroid.compose_charts.utils.FontSizeRange
import kotlin.math.atan2

@Composable
fun PieChartImpl(
    modifier: Modifier = Modifier,
    dataList: List<PieChartInput>,
    pieChartStyle: PieChartStyle = PieChartStyle(),
    legendsConfig: LegendsConfig,
    textColor: Color = Color.White,
    cardColor: Color = Color.LightGray,
    onSliceClick: ((PieChartInput) -> Unit)? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(cardColor)

    ) {
        val legendsList = mutableListOf<LegendLabel>()
        dataList.forEach { slice ->
            legendsList.add(LegendLabel(slice.color, slice.label))
        }

        if (legendsConfig.legendAxis == LegendAxis.VERTICAL) {
            Column(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Legends(
                    legendsConfig = legendsConfig.copy(
                        gridColumnCount = 3,
                        legendLabelList = legendsList
                    )
                )
                Spacer(modifier = Modifier.height(5.dp))
                PieChart(modifier = Modifier.align(Alignment.CenterHorizontally),
                    pieDataPoints = dataList,
                    style = pieChartStyle,
                    textColor = textColor,
                    centerText = pieChartStyle.centerText,
                    onSliceClick = {
                        if (onSliceClick != null) {
                            onSliceClick(it)
                        }
                    })

            }
        } else {
            Row(
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                PieChart(modifier = Modifier
                    .weight(3f)
                    .align(Alignment.CenterVertically),
                    pieDataPoints = dataList,
                    style = pieChartStyle,
                    textColor = textColor,
                    centerText = pieChartStyle.centerText,
                    onSliceClick = {
                        if (onSliceClick != null) {
                            onSliceClick(it)
                        }
                    })

                Legends(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    legendsConfig = legendsConfig.copy(
                        gridColumnCount = 1,
                        legendLabelList = legendsList
                    )
                )


            }
        }


    }
}

@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    innerRadius: Float = 100f,
    transparentWidth: Float = 50f,
    pieDataPoints: List<PieChartInput>,
    centerText: String = "Text",
    onSliceClick: ((PieChartInput) -> Unit)? = null,
    style: PieChartStyle = PieChartStyle(),
    textColor: Color
) {
    //for tapping
    var circleCenter by remember {
        mutableStateOf(Offset.Zero)
    }

    var inputList by remember {
        mutableStateOf(pieDataPoints)
    }
    var isCenterTapped by remember {
        mutableStateOf(false)
    }

    val textMeasurer = rememberTextMeasurer()

    val gapDegrees = 1f
    val numberOfGaps = pieDataPoints.size
    val remainingDegrees = 360f - (gapDegrees * numberOfGaps)

    val tapDetectModifier = if (onSliceClick == null) Modifier else {
        Modifier
            .pointerInput(Unit) {
                detectTapGestures { offset: Offset ->
                    val angle = Math.toDegrees(
                        atan2(
                            offset.y - circleCenter.y,
                            offset.x - circleCenter.x
                        ).toDouble()
                    )
                    val tapAngleInDegrees = if (angle < 0) angle + 360 else angle

                    val centerClicked = if (tapAngleInDegrees < 90) {
                        //means y -ve and x +ve region
                        offset.x < circleCenter.x + innerRadius && offset.y < circleCenter.y + innerRadius
                    } else if (tapAngleInDegrees < 180) {
                        //means y -ve and x -ve region
                        offset.x > circleCenter.x - innerRadius && offset.y < circleCenter.y + innerRadius
                    } else if (tapAngleInDegrees < 270) {
                        //means y +ve and x +ve region
                        offset.x > circleCenter.x - innerRadius && offset.y > circleCenter.y - innerRadius
                    } else {
                        //means y +ve and x +ve region
                        offset.x < circleCenter.x + innerRadius && offset.y > circleCenter.y - innerRadius
                    }

                    if (centerClicked) {
                        inputList = inputList.map {
                            it.copy(isTapped = !isCenterTapped)
                        }
                        isCenterTapped = !isCenterTapped
                    } else {
                        val anglePerValue = 360f / inputList.sumOf {
                            it.value
                        }
                        var currAngle = 0f
                        inputList.forEach { pieChartInput ->

                            currAngle += pieChartInput.value * anglePerValue
                            if (tapAngleInDegrees < currAngle) {
                                val description = pieChartInput.label
                                inputList = inputList.map {
                                    if (description == it.label) {
                                        onSliceClick(it)
                                        it.copy(isTapped = !it.isTapped)
                                    } else {
                                        it.copy(isTapped = false)
                                    }
                                }
                                return@detectTapGestures
                            }
                        }
                    }
                }
            }
    }

    val defaultModifier = Modifier
        .size(size = DEFAULT_GRAPH_SIZE)
        .then(tapDetectModifier)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = modifier
                .then(defaultModifier)
                .fillMaxSize()

        ) {
            val width = size.width
            val height = size.height

            circleCenter = Offset(x = width / 2f, y = height / 2f)
            val minWidthAndHeight = listOf(this.size.width, this.size.height).minOf { it }

            /**
             * Don't use [center] or [size] of canvas
             * Only use [pieCenter] and [pieSize] because we are maintaining same width and height
             * for the Pie
             */
            val pieSize = Size(minWidthAndHeight, minWidthAndHeight)
            val radius = minWidthAndHeight / 2

            val totalValue = pieDataPoints.sumOf {
                it.value
            }

            val anglePerValue = remainingDegrees / totalValue
            // val anglePerValue = 360f / totalValue
            var currentStartAngle = 0f

            inputList.forEach { pieChartInput ->
                val scale = if (pieChartInput.isTapped) 0.84f else 0.8f
                val angleToDraw = pieChartInput.value * anglePerValue

                scale(scale) {
                    drawArc(
                        color = pieChartInput.color,
                        startAngle = currentStartAngle, sweepAngle = angleToDraw, useCenter = true,
                        size = Size(
                            width = radius * 2f,
                            height = radius * 2f
                        ),
                        topLeft = Offset(
                            (width - radius * 2f) / 2f,
                            (height - radius * 2f) / 2f
                        )
                    )
                    currentStartAngle += angleToDraw + gapDegrees
                }

                var rotateAngle = currentStartAngle - gapDegrees - angleToDraw / 2f - 90f
                var factor = .95f
                if (rotateAngle > 90f) {
                    rotateAngle = (rotateAngle + 180).mod(360f)
                    factor = -.90f
                }

                if (pieChartInput.isTapped) {
                    rotate(rotateAngle) {
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                "${pieChartInput.label}: ${pieChartInput.value}",
                                circleCenter.x,
                                circleCenter.y + radius * factor,
                                Paint().apply {
                                    textSize = 15.sp.toPx()
                                    textAlign = Paint.Align.CENTER
                                    color = textColor.toArgb()
                                    isFakeBoldText = true
                                }
                            )
                        }
                    }
                }

                val percentage = (pieChartInput.value / totalValue.toFloat() * 100).toInt()

                /**
                 * Drawing label inside slices
                 */
                if (style.visibility.isLabelVisible) {
                    val midAngle = currentStartAngle - gapDegrees - angleToDraw / 2f
                    val midOffSet =
                        getOffsetOfAngle(angle = midAngle, radius = radius, pieSize = pieSize)

                    val centerOfSlice = centerOf(midOffSet, circleCenter)

                    if (percentage > 3) {
                        centeredText(
                            "$percentage %",
                            textColor,
                            textMeasurer,
                            centerOfSlice
                        )
                    }
                }

                if (style.visibility.isCenterCircleVisible) {
                    drawContext.canvas.nativeCanvas.apply {
                        drawCircle(
                            circleCenter.x,
                            circleCenter.y,
                            innerRadius,
                            Paint().apply {
                                color = white.copy(alpha = 0.6f).toArgb()
                                setShadowLayer(10f, 0f, 0f, gray.toArgb())
                            }
                        )
                    }

                    drawCircle(
                        color = white.copy(0.2f),
                        radius = innerRadius + transparentWidth / 2f
                    )
                }


            }

        }

        if (style.visibility.isCenterTextVisible) {
            //split the text to concatenate each word for new line if required
            val list = centerText.split(" ")
            var text = ""
            list.forEach {
                text += "$it \n"
            }

            Box(
                modifier = Modifier
                    .size(innerRadius.times(.6).dp)
                    .align(Alignment.Center),

                contentAlignment = Alignment.Center
            ) {
                AutoResizeText(
                    text = centerText,
                    maxLines = 4,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(start = 10.dp),
                    fontSizeRange = FontSizeRange(
                        min = 10.sp,
                        max = 20.sp,
                    ),
                    overflow = TextOverflow.Ellipsis,
                    style = style.textStyle,
                )
            }
        }


    }


}


