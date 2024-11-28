package com.example.healthmonitor

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val heartRateEntries = mutableListOf<Entry>()
    private val bodyTempEntries = mutableListOf<Entry>()
    private var currentDay = 0

    private lateinit var lineChart: LineChart
    private lateinit var tvHeartRate: TextView
    private lateinit var tvBodyTemp: TextView
    private val updateInterval = 1000L // 1 second

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lineChart = findViewById(R.id.lineChart)
        tvHeartRate = findViewById(R.id.tvHeartRate)
        tvBodyTemp = findViewById(R.id.tvBodyTemp)
        setupLineChart()

        // Start updating values using Coroutine
        startUpdatingValues()
    }

    private fun startUpdatingValues() {
        coroutineScope.launch {
            while (isActive) {
                updateValues()
                delay(updateInterval)
            }
        }
    }

    private fun updateValues() {
        // Simulate random heart rate and body temp
        val heartRate = Random.nextInt(60, 100)
        val bodyTemp = Random.nextDouble(36.0, 37.5)

        // Update TextViews
        tvHeartRate.text = "Heart Rate: $heartRate bpm"
        tvBodyTemp.text = "%.1f °C".format(bodyTemp)

        // Update Chart Data
        currentDay++
        heartRateEntries.add(Entry(currentDay.toFloat(), heartRate.toFloat()))
        bodyTempEntries.add(Entry(currentDay.toFloat(), bodyTemp.toFloat()))

        val heartRateDataSet = LineDataSet(heartRateEntries, "Heart Rate").apply {
            color = ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark)
            setCircleColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark))
        }

        val bodyTempDataSet = LineDataSet(bodyTempEntries, "Body Temp").apply {
            color = ContextCompat.getColor(this@MainActivity, android.R.color.holo_blue_dark)
            setCircleColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_blue_dark))
        }

        val data = LineData(heartRateDataSet, bodyTempDataSet)
        lineChart.data = data
        lineChart.invalidate() // Refresh chart
    }

    private fun setupLineChart() {
        lineChart.apply {
            description.isEnabled = false
            legend.apply {
                form = Legend.LegendForm.LINE
            }
            xAxis.apply {
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "Day ${value.toInt()}"
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel the coroutine job to avoid memory leaks
    }
}
