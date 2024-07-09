package com.decodeandroid.chartcompose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decodeandroid.compose_charts.style.LegendAxis
import com.decodeandroid.compose_charts.style.LegendsConfig
import com.decodeandroid.compose_charts.PieChart
import com.decodeandroid.compose_charts.model.PieChartInput
import com.decodeandroid.compose_charts.style.PieChartStyle
import com.decodeandroid.compose_charts.style.PieChartVisibility

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Composable Graphs")
                    },
                )
            },
            content = { paddingValue ->
                BodyContent(paddingValue)
            }
        )
    }
}

@Composable
fun BodyContent(paddingValue: PaddingValues) {

    val context = LocalContext.current

    LazyColumn(modifier = Modifier.padding(top = 80.dp),
        contentPadding = paddingValue,
        content = {

            item(key = "item-1") {
                val dataList = listOf(
                    PieChartInput(
                        value = 35,
                        label = "Data 1"
                    ),
                    PieChartInput(
                        value = 21,
                        label = "Data 2"
                    ),
                    PieChartInput(
                        value = 32,
                        label = "Data 3"
                    ),
                    PieChartInput(
                        value = 18,
                        label = "Data 4"
                    ),
                    PieChartInput(
                        value = 12,
                        label = "Data 5"
                    ),
                    PieChartInput(
                        value = 38,
                        label = "Data 6"
                    )
                )

                val pieChartStyle = PieChartStyle(
                    visibility = PieChartVisibility(
                        isLabelVisible = false,
                        isCenterCircleVisible = true,
                        isCenterTextVisible = true
                    ),
                    labelSize = 10.sp,
                    centerText = "Pie Chart New"
                )

                val legendsConfig = LegendsConfig(
                    legendsArrangement = Arrangement.Start,
                    textStyle = TextStyle(),
                    legendAxis = LegendAxis.HORIZONTAL,
                    legendShape = RoundedCornerShape(5.dp)
                )

                PieChart(
                    dataList = dataList,
                    pieChartStyle = pieChartStyle,
                    legendsConfig = legendsConfig
                ) {
                    Toast.makeText(context, it.label, Toast.LENGTH_SHORT).show()
                }

            }

        }
    )

}

@Preview
@Composable
fun MainAppPreview() {
    MainApp()
}

