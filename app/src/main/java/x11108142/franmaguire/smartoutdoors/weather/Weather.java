package x11108142.franmaguire.smartoutdoors.weather;

import x11108142.franmaguire.smartoutdoors.R;

public class Weather {

    private Prediction mPrediction;

    public Prediction getPrediction (){
        return mPrediction;
    }

    public void setPrediction(Prediction prediction){
       mPrediction = prediction;

    }

    public static int getWeatherIconId(String weatherIconString){
        int weatherIconId = R.mipmap.clear_day;

        if (weatherIconString.equals("clear-day")) {
            weatherIconId = R.mipmap.clear_day;
        }
        else if (weatherIconString.equals("clear-night")) {
            weatherIconId = R.mipmap.clear_night;
        }
        else if (weatherIconString.equals("rain")) {
            weatherIconId = R.mipmap.rain;
        }
        else if (weatherIconString.equals("snow")) {
            weatherIconId = R.mipmap.snow;
        }
        else if (weatherIconString.equals("sleet")) {
            weatherIconId = R.mipmap.sleet;
        }
        else if (weatherIconString.equals("wind")) {
            weatherIconId = R.mipmap.wind;
        }
        else if (weatherIconString.equals("fog")) {
            weatherIconId = R.mipmap.fog;
        }
        else if (weatherIconString.equals("cloudy")) {
            weatherIconId = R.mipmap.cloudy;
        }
        else if (weatherIconString.equals("partly-cloudy-day")) {
            weatherIconId = R.mipmap.partly_cloudy;
        }
        else if (weatherIconString.equals("partly-cloudy-night")) {
            weatherIconId = R.mipmap.cloudy_night;
        }
        return weatherIconId;

    }
}
