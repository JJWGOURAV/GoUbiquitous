package com.example.android.sunshine.app;

/**
 * Created by GOURAV on 08-12-2016.
 */

public class Temperature {

    private int highTemp;
    private int lowTemp;
    private int weatherId;

    public int getHighTemp() {
        return highTemp;
    }

    public int getLowTemp() {
        return lowTemp;
    }

    public int getWeatherId() {
        return weatherId;
    }

    private static Temperature temperature;

    private Temperature(){}

    private Temperature(int highTemp, int lowTemp, int weatherId){
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.weatherId = weatherId;

    }

    public static Temperature getInstance(){
        if(temperature != null){
            return temperature;
        }

        return temperature = new Temperature();
    }

    public synchronized void updateTemp(int highTemp, int lowTemp, int weatherId){
        temperature.highTemp = highTemp;
        temperature.lowTemp = lowTemp;
        temperature.weatherId = weatherId;
    }
}
