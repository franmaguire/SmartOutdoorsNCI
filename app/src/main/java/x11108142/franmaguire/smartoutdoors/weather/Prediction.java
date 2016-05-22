package x11108142.franmaguire.smartoutdoors.weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Prediction {

    private String mWeatherIcon; //micon
    private String mTimeZone;
    private long mUnixTime; //mtime
    private double mApparentTemp; //temp
    private int mStormDistance = 0;
    private String mStormMessage;
    private String mPrecipitationType = "";
    private double mPrecipitationIntensity = 0.00;
    private String mPrecipitationPrefix ="";
    private final double mPrecipLight = 0.017;
    private final double mPrecipModerate = 0.1;





    public int getWindSpeed() {
        return mWindSpeed;
    }

    public void setWindSpeed(int windSpeed) {
        mWindSpeed = windSpeed;
    }

    public int getWeatherIconId() {
        return Weather.getWeatherIconId(mWeatherIcon);
    }

    public void setWeatherIcon(String weatherIcon) {
        mWeatherIcon = weatherIcon;
    }

    public String getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
    }

    public int getApparentTemp() {
        return (int)Math.round(mApparentTemp);
    }

    public void setApparentTemp(double apparentTemp) {
        mApparentTemp = apparentTemp;
    }

    public String getStormDistance() {
        String stormDistance = "";
        String stormError = "";
        String distanceFlag =" km";
        if (mStormDistance == 0){
            stormDistance = "0";
        } else if(mStormDistance > 0 && mStormDistance < 4000){
            stormDistance = mStormDistance + "";
        } else if(mStormDistance == 4000){
            stormError = "None";
            distanceFlag = "";
        }

        return mStormMessage = stormError + stormDistance + distanceFlag;
     }

    public void setStormDistance(int stormDistance) {

        mStormDistance = stormDistance;
    }

    public String getPrecipitationIntensity(){
        String precipitationString = "";
        if(mPrecipitationIntensity==0){
            precipitationString = "dry";
        } else if(mPrecipitationIntensity > 0 || mPrecipitationIntensity < mPrecipLight){
            precipitationString = "light";
        } else if (mPrecipitationIntensity>mPrecipLight || mPrecipitationIntensity<mPrecipModerate){
            precipitationString = "moderate";
        } else if (mPrecipitationIntensity>mPrecipModerate){
            precipitationString = "heavy";
        }
        mPrecipitationPrefix = precipitationString;
        return mPrecipitationPrefix;
    }

    public void setPrecipitationIntensity(double precipIntensity ){
        mPrecipitationIntensity = precipIntensity;
    }


    public String getPrecipitationType() {

        return mPrecipitationType;
    }

    public void setPrecipitationType(String precipitationType) {

        mPrecipitationType = precipitationType;
    }

    private int mWindSpeed;

}
