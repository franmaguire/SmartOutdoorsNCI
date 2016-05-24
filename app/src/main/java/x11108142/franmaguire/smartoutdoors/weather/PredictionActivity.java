package x11108142.franmaguire.smartoutdoors.weather;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import x11108142.franmaguire.smartoutdoors.R;

/**
 * The network ready method, the update screen method and run on UI thread methods are from
 * teamtreehouse.com tutorials, StormyWeather Application, https://teamtreehouse.com/community/syllabus:1042
 *
 * */

public class PredictionActivity extends AppCompatActivity {


    public static final String LOG_EVENT = PredictionActivity.class.getSimpleName();
    public static final String DARK_SKY_API_KEY = "ee9715cfe23ec53ff4e32085e7104db2";


    @Bind(R.id.apparentTempLabel) TextView mApparentTempLabel;
    @Bind(R.id.timeLabel) TextView mTimeLabel;
    @Bind(R.id.updateScreenLabel) ImageView mUpdateScreenLabel;
//    @Bind(R.id.degreeLabel) TextView mDegreeLabel;
    @Bind(R.id.stormDistanceLabel) TextView mStormDistanceLabel;
    @Bind(R.id.precipitationTypeLabel) TextView mPrecipitationTypeLabel;
    @Bind(R.id.windSpeedLabel) TextView mWindSpeedLabel;
    @Bind(R.id.destinationLabel) TextView mDestinationLabel;
    @Bind(R.id.weatherIconImageView) ImageView mWeatherIconImageView;
    @Bind(R.id.networkRequestProgressBar) ProgressBar mNetworkRequestProgressBar;

    private Weather mWeather;
    private String unixTimeString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        //final double destinationParam = extras.getDouble("predictionDestination");
        final String destinationTime = extras.getString("routeDuration");
        final String locationAddress = extras.getString("destinationAddress");
        final double latitudeDestination = extras.getDouble("latitude");
        final double longitudeDestination = extras.getDouble("longitude");
        final long unixTimeParam = extras.getLong("unixTime");

        //time for journey
        unixTimeString = convertUnixTime(unixTimeParam);

        mNetworkRequestProgressBar.setVisibility(View.INVISIBLE);
        mUpdateScreenLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWeatherRequest(latitudeDestination, longitudeDestination, unixTimeParam);
            }
        });


        mDestinationLabel.setText(locationAddress);

        getWeatherRequest(latitudeDestination, longitudeDestination, unixTimeParam);

    }

    private void getWeatherRequest(double latitude, double longitude, long unixTime) {
        //result returned in metric
        String metric = "?units=si";
        long future = unixTime;
        String forecastUrl = "https://api.forecast.io/forecast/"
                +DARK_SKY_API_KEY+"/"
                +latitude
                +","
                +longitude
                +","
                +future
                +metric;
        Log.i(LOG_EVENT, "The forecast request was made with the following url : " + forecastUrl);
        if(isThereAConnection()) {

            restartRequest();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            restartRequest();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            restartRequest();
                        }
                    });

                    try {
                        String jsonData = response.body().string();
                        Log.v(LOG_EVENT, jsonData);
                        if (response.isSuccessful()) {

                            mWeather = getWeatherPrediction(jsonData);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    screenUpdate();
                                }
                            });

                        } else {
                            alertUserAboutError();
                        }

                    }
                    catch (IOException e) {
                        Log.e(LOG_EVENT, "Exception caught: ", e);
                    }
                    catch (JSONException e){
                        Log.e(LOG_EVENT, "Exception caught: ", e);
                    }
                }
            });
        }
        else
        {
            Toast.makeText(this, R.string.network_unavailable_message, Toast.LENGTH_LONG).show();
        }
        Log.d(LOG_EVENT, "Main UI Code is running");
    }


    private void restartRequest(){

        if (mNetworkRequestProgressBar.getVisibility()==View.INVISIBLE) {
            mNetworkRequestProgressBar.setVisibility(View.VISIBLE);
            mUpdateScreenLabel.setVisibility(View.INVISIBLE);
        }
        else{
            mNetworkRequestProgressBar.setVisibility(View.INVISIBLE);
            mUpdateScreenLabel.setVisibility(View.VISIBLE);

        }
    }

    private void screenUpdate() {
        Prediction prediction = mWeather.getPrediction();
        mApparentTempLabel.setText(prediction.getApparentTemp() +"");
        mPrecipitationTypeLabel.setText(prediction.getPrecipitationIntensity()+" " + prediction.getPrecipitationType() +"");
        mStormDistanceLabel.setText(prediction.getStormDistance() + "");
        mWindSpeedLabel.setText(prediction.getWindSpeed()+ " kph");
        Drawable drawable = ContextCompat.getDrawable(this, prediction.getWeatherIconId());
        mWeatherIconImageView.setImageDrawable(drawable);

    }

    private Weather getWeatherPrediction(String jsonData) throws JSONException{

        Weather weather = new Weather();
        weather.setPrediction(getPrediction(jsonData));
        return weather;
    }
    private String convertUnixTime(long unixTime){

        Date date = new Date(unixTime*1000L);
        SimpleDateFormat simpleTime = new SimpleDateFormat("HH:MM");
        simpleTime.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedTime = simpleTime.format(date);
        return formattedTime;
    }

    private Prediction getPrediction (String jsonData) throws JSONException {

        JSONObject jsonPrediction = new JSONObject(jsonData);
        String timezone = jsonPrediction.getString("timezone");
        Log.i(LOG_EVENT, "From JSON: "+ timezone);

        JSONObject predict = jsonPrediction.getJSONObject("currently");

        Prediction prediction = new Prediction();

        try {
            prediction.setStormDistance(predict.getInt("nearestStormDistance"));
        } catch (JSONException error){
            Log.e(LOG_EVENT, "Error parsing the Forecast response for storm distance");
            prediction.setStormDistance(4000);
        }
        try {
            prediction.setWeatherIcon(predict.getString("icon"));
        } catch (JSONException error){
            Log.e(LOG_EVENT, "Error parsing the Forecast response for weather icon");
            prediction.setWeatherIcon("rain");
        }
        try {
            prediction.setPrecipitationIntensity(predict.getDouble("precipIntensity"));
        } catch (JSONException error){
            Log.e(LOG_EVENT, "Error parsing the Forecast response for precipitation intensity");
            prediction.setPrecipitationIntensity(0);
        }
        try {
            prediction.setPrecipitationType(predict.getString("precipType"));
        } catch (JSONException error){
            Log.e(LOG_EVENT, "Error parsing the Forecast response for precipitation type");
            prediction.setPrecipitationType("clear ");
        }
        try {
            prediction.setWindSpeed(predict.getInt("windSpeed"));
        } catch (JSONException error){
            Log.e(LOG_EVENT, "Error parsing the Forecast response for wind speed");
            prediction.setWindSpeed(5);
        }
        try {
            prediction.setApparentTemp(predict.getDouble("apparentTemperature"));
        } catch (JSONException error){
            Log.e(LOG_EVENT, "Error parsing the Forecast response for apparent temperature");
            prediction.setApparentTemp(10);
        }

            prediction.setTimeZone(timezone);


        return prediction;
    }

    private boolean isThereAConnection() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "alert_dialog");
    }

}
