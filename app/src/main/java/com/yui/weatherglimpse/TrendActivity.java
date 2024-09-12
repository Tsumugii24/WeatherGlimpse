package com.yui.weatherglimpse;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.yui.weatherglimpse.R;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.yui.weatherglimpse.utils.WeatherData;

import java.util.ArrayList;
import java.util.List;
import com.yui.weatherglimpse.utils.WeatherDataFunction;

public class TrendActivity extends AppCompatActivity {
    private LineChart tempChart, pressureChart, windChart, humidityChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend);

        tempChart = findViewById(R.id.tempChart);
        pressureChart = findViewById(R.id.pressureChart);
        windChart = findViewById(R.id.windChart);
        humidityChart = findViewById(R.id.humidityChart);

        List<WeatherData> weatherData = (List<WeatherData>) getIntent().getSerializableExtra("weatherData");

        setupCharts(weatherData);

        Button backToHomeButton = findViewById(R.id.backToHomeButton);
        backToHomeButton.setOnClickListener(v -> {
            Intent intent = new Intent(TrendActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private LineDataSet createDataSet(List<WeatherData> weatherData, String label, int color, WeatherDataFunction getValue) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < weatherData.size(); i++) {
            float value = (float) getValue.apply(weatherData.get(i));
            entries.add(new Entry(i, value));
            Log.d("TrendActivity", label + " Day " + (i + 1) + ": " + value);
        }
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(color);
        dataSet.setCircleRadius(4f);
        dataSet.setLineWidth(2f);
        return dataSet;
    }

    private void setupCharts(List<WeatherData> weatherData) {
        setupTempChart(weatherData);
        setupPressureChart(weatherData);
        setupWindChart(weatherData);
        setupHumidityChart(weatherData);
    }

    private void setupTempChart(List<WeatherData> weatherData) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(createDataSet(weatherData, "最高温度", Color.RED, WeatherData::getMaxTemp));
        dataSets.add(createDataSet(weatherData, "最低温度", Color.BLUE, WeatherData::getMinTemp));
        
        LineData lineData = new LineData(dataSets);
        tempChart.setData(lineData);
        configureChart(tempChart, "温度 (°C)");
    }

    private void setupPressureChart(List<WeatherData> weatherData) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(createDataSet(weatherData, "气压", Color.GREEN, WeatherData::getPressure));
        
        LineData lineData = new LineData(dataSets);
        pressureChart.setData(lineData);
        configureChart(pressureChart, "气压 (mb)");
    }

    private void setupWindChart(List<WeatherData> weatherData) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(createDataSet(weatherData, "风速", Color.YELLOW, WeatherData::getWindSpeed));
        
        LineData lineData = new LineData(dataSets);
        windChart.setData(lineData);
        configureChart(windChart, "风速 (km/h)");
    }

    private void setupHumidityChart(List<WeatherData> weatherData) {
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(createDataSet(weatherData, "湿度", Color.CYAN, WeatherData::getHumidity));
        
        LineData lineData = new LineData(dataSets);
        humidityChart.setData(lineData);
        configureChart(humidityChart, "湿度 (%)");
    }

    private void configureChart(LineChart chart, String label) {
        // 设置 X 轴
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "Day " + ((int) value / 2 + 1);
            }
        });

        // 设置 Y 轴
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0f);

        // 其他图表设置
        chart.getDescription().setText(label);
        chart.getLegend().setEnabled(true);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        chart.invalidate();
    }
}