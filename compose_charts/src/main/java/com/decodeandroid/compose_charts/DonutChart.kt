package com.decodeandroid.compose_charts

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decodeandroid.compose_charts.model.PieChartInput
import com.decodeandroid.compose_charts.ui.Pink80
import com.decodeandroid.compose_charts.ui.gray
import com.decodeandroid.compose_charts.ui.green
import com.decodeandroid.compose_charts.ui.white
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewDonutChart(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gray)
            .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Your Data Values",
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold,
            color = white,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 30.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            PieChart(
                modifier = Modifier
                    .size(400.dp),
                pieDataPoints = listOf(
                    PieChartInput(
                        color = Color(0xFF9400D3),
                        value = 29,
                        label = "Data 1"
                    ),
                    PieChartInput(
                        color = Color(0xFF42A1D5),
                        value = 21,
                        label = "Data 2"
                    ),
                    PieChartInput(
                        color = Color(0xFF8D9311),
                        value = 32,
                        label = "Data 3"
                    ),
                    PieChartInput(
                        color = Color(0xFFFF7F00),
                        value = 18,
                        label = "Data 4"
                    ),
                    PieChartInput(
                        color = green,
                        value = 12,
                        label = "Data 5"
                    ),
                    PieChartInput(
                        color = Pink80,
                        value = 38,
                        label = "Data 6"
                    ),
                ),
                centerText = "One Year",
                onPositionChange = {

                }
            )
        }
    }
}

@Composable
fun PieChart(
    modifier: Modifier = Modifier,
    radius: Float = 400f,
    innerRadius: Float = 200f,
    transparentWidth: Float = 70f,
    pieDataPoints: List<PieChartInput>,
    centerText: String = "",
    onPositionChange: (Int) -> Unit,
    minValue: Int = 0,
    maxValue: Int = 100,
    isProgressAnimated:Boolean=false,
) {
    //for tapping
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

    val gapDegrees = 15f
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
                        durationMillis = 1000,
                        easing = LinearEasing,
                        delayMillis = index * 1000
                    ),
                )
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(true) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragStartedAngle = -atan2(
                                x = circleCenter.y - offset.y,
                                y = circleCenter.x - offset.x
                            ) * (180f / PI).toFloat()
                            dragStartedAngle = (dragStartedAngle + 180f).mod(360f)
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
                            onPositionChange(positionValue)
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            circleCenter = Offset(x = width / 2f, y = height / 2f)

            val totalValue = pieDataPoints.sumOf {
                it.value
            }

            val anglePerValue = remainingDegrees / totalValue
            // val anglePerValue = 360f / totalValue
            var currentStartAngle = 0f

            val style1 = Stroke(
                width = 100f,
                cap = StrokeCap.Round
            )

            rotate(
                degrees = (360f / maxValue) * positionValue.toFloat(),
                pivot = circleCenter
            ) {
                inputList.forEach { pieChartInput ->
                    val scale = if (pieChartInput.isTapped) 0.9f else 0.8f
                    val angleToDraw = pieChartInput.value * anglePerValue

                    scale(scale) {
                        drawArc(
                            color = pieChartInput.color,
                            startAngle =pieChartInput.startAngle
                            //startAngle =currentStartAngle
                            , sweepAngle =pieChartInput.animation.value
                            //, sweepAngle =angleToDraw.toFloat()
                            , useCenter = false,
                            size = Size(
                                width = radius * 2f,
                                height = radius * 2f
                            ),
                            topLeft = Offset(
                                (width - radius * 2f) / 2f,
                                (height - radius * 2f) / 2f
                            ),
                            style = style1
                        )
                        currentStartAngle += angleToDraw + gapDegrees
                    }

                    var rotateAngle = currentStartAngle - gapDegrees - angleToDraw / 2f - 90f
                    var factor = 1.08f
                    if (rotateAngle > 90f) {
                        rotateAngle = (rotateAngle + 180).mod(360f)
                        factor = -1f
                    }

                    val percentage = (pieChartInput.value / totalValue.toFloat() * 100).toInt()

                    drawContext.canvas.nativeCanvas.apply {
                        if (percentage > 3) {
                            rotate(rotateAngle) {
                                drawText(
                                    "$percentage %",
                                    circleCenter.x,
                                    circleCenter.y + (radius * 0.75f) * factor,
                                    Paint().apply {
                                        textSize = 15.sp.toPx()
                                        textAlign = Paint.Align.CENTER
                                        color = white.toArgb()
                                    }
                                )
                            }
                        }
                    }
                    if (pieChartInput.isTapped) {
                        rotate(rotateAngle) {
                            drawContext.canvas.nativeCanvas.apply {
                                drawText(
                                    "${pieChartInput.label}: ${pieChartInput.value}",
                                    circleCenter.x,
                                    circleCenter.y + radius * factor,
                                    Paint().apply {
                                        textSize = 20.sp.toPx()
                                        textAlign = Paint.Align.CENTER
                                        color = white.toArgb()
                                        isFakeBoldText = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

        }

        Text(
            centerText,
            modifier = Modifier
                .width(Dp(innerRadius / 1.5f))
                .padding(25.dp),
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
            color = white
        )

    }
}






