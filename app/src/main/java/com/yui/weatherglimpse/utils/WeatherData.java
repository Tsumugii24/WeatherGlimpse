package com.yui.weatherglimpse.utils;

import java.io.Serializable;

public class WeatherData implements Serializable{
    public double maxTemp;
    public double minTemp;
    public double pressure;
    public double windSpeed;
    public double humidity;

    public WeatherData(double maxTemp, double minTemp, double pressure, double windSpeed, double humidity) {
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.humidity = humidity;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public double getPressure() {
        return pressure;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getHumidity() {
        return humidity;
    }
}