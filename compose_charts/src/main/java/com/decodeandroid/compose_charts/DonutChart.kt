package com.decodeandroid.compose_charts

import android.graphics.Paint
import android.text.TextPaint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decodeandroid.compose_charts.model.PieChartInput
import com.decodeandroid.compose_charts.style.DonutChartStyle
import com.decodeandroid.compose_charts.style.LegendAxis
import com.decodeandroid.compose_charts.style.LegendLabel
import com.decodeandroid.compose_charts.style.Legends
import com.decodeandroid.compose_charts.style.LegendsConfig
import com.decodeandroid.compose_charts.utils.AutoResizeText
import com.decodeandroid.compose_charts.utils.FontSizeRange
import com.decodeandroid.compose_charts.utils.GraphHelper.DEFAULT_GRAPH_SIZE
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun DonutChartImpl(
    modifier: Modifier = Modifier,
    dataList: List<PieChartInput>,
    donutChartStyle: DonutChartStyle = DonutChartStyle(),
    legendsConfig: LegendsConfig,
    textColor: Color = Color.White,
    cardColor: Color = Color.LightGray,
    isProgressAnimated:Boolean=false,
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

                DonutChart(modifier = Modifier.align(Alignment.CenterHorizontally),
                    pieDataPoints = dataList,
                    chartStyle = donutChartStyle,
                    textColor = textColor,
                    centerText = donutChartStyle.centerText,
                    isProgressAnimated=isProgressAnimated,
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

                DonutChart(modifier = Modifier
                    .weight(3f)
                    .align(Alignment.CenterVertically),
                    pieDataPoints = dataList,
                    chartStyle = donutChartStyle,
                    textColor = textColor,
                    centerText = donutChartStyle.centerText,
                    isProgressAnimated=isProgressAnimated,
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
fun DonutChart(
    modifier: Modifier = Modifier,
    innerRadius: Float = 200f,
    pieDataPoints: List<PieChartInput>,
    chartStyle: DonutChartStyle = DonutChartStyle(),
    textColor: Color,
    centerText: String = "Donut Chart Data",
    onSliceClick: ((PieChartInput) -> Unit)? = null,
    minValue: Int = 0,
    maxValue: Int = 100,
    isProgressAnimated:Boolean=false,
) {
    var circleCenter by remember {
        mutableStateOf(Offset.Zero)
    }

    var inputList by remember {
        mutableStateOf(pieDataPoints)
    }

    //for rotation
    val initialValue = 0

    var positionValue by remember {
        mutableStateOf(initialValue)
    }

    var changeAngle by remember {
        mutableStateOf(0f)
    }

    var dragStartedAngle by remember {
        mutableStateOf(0f)
    }

    var oldPositionValue by remember {
        mutableStateOf(initialValue)
    }

    val gapDegrees :Float
    val strokeStyle:Stroke
    if (isProgressAnimated){
        gapDegrees=20f
        strokeStyle = Stroke(
            width = 80f,
            cap = StrokeCap.Round
        )
    }else{
        gapDegrees=2f
        strokeStyle = Stroke(
            width = 100f,
            cap = StrokeCap.Butt
        )
    }
    val numberOfGaps = pieDataPoints.size
    val remainingDegrees = 360f - (gapDegrees * numberOfGaps)
    val total = pieDataPoints.fold(0f) { acc, pieData -> acc + pieData.value }.div(remainingDegrees)
    var currentSum = 0f

    LaunchedEffect(true) {

        inputList = pieDataPoints.mapIndexed { index, it ->
            val startAngle = currentSum + (index * gapDegrees)
            currentSum += it.value / total
            PieChartInput(
                targetSweepAngle = it.value / total,
                animation = Animatable(0f),
                startAngle = startAngle,
                color = it.color,
                label = it.label,
                value = it.value
            )

        }

        inputList.mapIndexed { index, it ->
            launch {
                it.animation.animateTo(
                    targetValue = it.targetSweepAngle,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = LinearEasing,
                        delayMillis = index * 500
                    ),
                )
            }
        }
    }

    val tapDetectModifier = if (onSliceClick == null) Modifier else {
        Modifier
            .pointerInput(true) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragStartedAngle = -atan2(
                            x = circleCenter.y - offset.y,
                            y = circleCenter.x - offset.x
                        ) * (180f / PI).toFloat()
                        dragStartedAngle = (dragStartedAngle + 180f).mod(360f)

                        /*val clickedSlice = pieSliceList.find { slice ->
                            (touchAngle >= slice.startAngle && touchAngle <= slice.endAngle)
                        }
                        val clickedPieData = data.find { clickedSlice?.label == it.label }
                        clickedPieData?.let { onSliceClick(it) }*/

                    },
                    onDrag = { change, _ ->
                        var touchAngle = -atan2(
                            x = circleCenter.y - change.position.y,
                            y = circleCenter.x - change.position.x
                        ) * (180f / PI).toFloat()
                        touchAngle = (touchAngle + 180f).mod(360f)

                        val currentAngle = oldPositionValue * 360f / (maxValue - minValue)
                        changeAngle = touchAngle - currentAngle

                        positionValue =
                            (oldPositionValue + (changeAngle / (360f / (maxValue - minValue))).roundToInt())

                        //if you want to start draging from a perticular position
                        val lowerThreshold = currentAngle - (360f / (maxValue - minValue) * 5)
                        val higherThreshold = currentAngle + (360f / (maxValue - minValue) * 5)

                        if (dragStartedAngle in lowerThreshold..higherThreshold) {
                            positionValue =
                                (oldPositionValue + (changeAngle / (360f / (maxValue - minValue))).roundToInt())
                        }

                    },
                    onDragEnd = {
                        oldPositionValue = positionValue
                    }
                )
            }
    }

    val defaultModifier = Modifier
        .size(size = DEFAULT_GRAPH_SIZE)
        .then(tapDetectModifier)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = modifier
                .then(defaultModifier)
                .fillMaxSize()
                .align(Alignment.Center)

        ) {
            val width = size.width
            val height = size.height

            circleCenter = Offset(x = width / 2f, y = height / 2f)

            val minWidthAndHeight = listOf(this.size.width, this.size.height).minOf { it }
            val radius = minWidthAndHeight / 2

            val totalValue = pieDataPoints.sumOf {
                it.value
            }

            val anglePerValue = remainingDegrees / totalValue
            var currentStartAngle = 0f

            rotate(
                degrees = (360f / maxValue) * positionValue.toFloat(),
                pivot = circleCenter
            ) {
                inputList.forEach { pieChartInput ->
                    val angleToDraw = pieChartInput.value * anglePerValue

                    val radiusRatio=radius.times(1.2f)

                    if (isProgressAnimated){
                        drawArc(
                            color = pieChartInput.color,
                            startAngle =pieChartInput.startAngle
                            , sweepAngle =pieChartInput.animation.value
                            , useCenter = false,
                            size = Size(
                                width = radiusRatio,
                                height = radiusRatio
                            ),
                            topLeft = Offset(
                                (width - radiusRatio) / 2f,
                                (height - radiusRatio) / 2f
                            ),
                            style = strokeStyle
                        )

                    }else{
                        drawArc(
                            color = pieChartInput.color,
                            startAngle =currentStartAngle
                            , sweepAngle = angleToDraw
                            , useCenter = false,
                            size = Size(
                                width = radiusRatio,
                                height = radiusRatio
                            ),
                            topLeft = Offset(
                                (width - radiusRatio) / 2f,
                                (height - radiusRatio) / 2f
                            ),
                            style = strokeStyle
                        )
                    }
                    currentStartAngle += angleToDraw + gapDegrees

                    var rotateAngle = currentStartAngle - gapDegrees - angleToDraw / 2f - 90f
                    var factor = .89f
                    if (rotateAngle > 90f) {
                        rotateAngle = (rotateAngle + 180).mod(360f)
                        factor = -.82f
                    }

                    val percentage = (pieChartInput.value / totalValue.toFloat() * 100).toInt()

                    /**
                     * Drawing label inside slices
                     */
                    if (chartStyle.visibility.isLabelVisible) {
                        drawContext.canvas.nativeCanvas.apply {
                            if (percentage > 3) {
                                rotate(rotateAngle) {
                                    drawText(
                                        "$percentage %",
                                        circleCenter.x,
                                        circleCenter.y + (radius * 0.71f) * factor,
                                        Paint().apply {
                                            textSize = 15.sp.toPx()
                                            textAlign = Paint.Align.CENTER
                                            color = textColor.toArgb()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    /**
                     * Drawing description outside slices
                     */
                    if (chartStyle.visibility.isDescriptionVisible){
                        drawContext.canvas.nativeCanvas.apply {
                            if (percentage > 3) {
                                rotate(rotateAngle) {
                                    drawText(
                                        "${pieChartInput.label}: ${pieChartInput.value}",
                                        circleCenter.x,
                                        circleCenter.y + radius * factor,
                                        Paint().apply {
                                            textSize =chartStyle.textStyle.fontSize.toPx()
                                            textAlign = Paint.Align.CENTER
                                            color = textColor.toArgb()
                                        }

                                    )
                                }
                            }
                        }
                    }
                }
            }

        }

        if (chartStyle.visibility.isCenterTextVisible) {
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
                    style = chartStyle.textStyle,
                )
            }
        }

    }
}






