package x11108142.franmaguire.smartoutdoors.gps;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import org.joda.time.DateTime;


/**
 * The methods from "MyLocationDemoActivity" where used in this activity AND the entire class "PermissionUtils" was added to this project
 * available from Google API Demo's found at https://github.com/googlemaps/android-samples
 *
 * The methods from MyLocationDemoActivity were reused as they performed the functionality required
 * The class PermissionUtils was resued, It was a utility class that manages access to runtime permissions
 *
 * The class "SimpleLocation" is from delight.im <info@delight.im>, a library to access location features of Google Maps API V2
 *
 *
 * */

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.model.TimeInfo;
import com.akexorcist.googledirection.model.TransitDetail;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import x11108142.franmaguire.smartoutdoors.weather.PredictionActivity;
import x11108142.franmaguire.smartoutdoors.R;

public class MapsActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        DirectionCallback {

    private static final String PREFERENCES_DATA = "x11108142.franmaguire.smartoutdoors";
    private static final String KEY_LOCATIONDATA = "key_gps_latlng";
    private static final String SERVER_API_KEY = "AIzaSyD3DkfS6KgbwVoEIsqI4bOThO0dBOzf3C0";
    private static final String PREDICTION_START = "prediction_start";//
    private static final String PREDICTION_LOCATION = "prediction_location";//
    private static final String LOG_EVENT = MapsActivity.class.getSimpleName(); ;

    private static LatLng mRouteDestinationLatLng;//
    private static LatLng mRouteCurrentPositionLatLng;//
    private static String mRouteDuration;//time to pass to weather activity
    private static boolean DESTINATION_LOCATION_SELECTED = false;
    private static boolean CURRENT_LOCATION_SELECTED = false;

    private long mRoutePredictionTime; // convert akexorcist response to unix time

    private Marker mDestinationMarker;

    private GoogleMap mMap;
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private static String mMappingData;


    /**
     * Current lat and long
     */
    double latitude, longitude;
    SupportMapFragment mFragment;
    private SimpleLocation mLocation;
    double latDestination, longDestination;

    //DISTANCE BETWEEN LOCATIONS AND THE DESTINATION ADDRESS

    String distance = "";
    String destination = "";
    String mRequestAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // construct a new instance
        mLocation = new SimpleLocation(this);
        // if we can't access the location yet, prompt the user to enable location access
        if (!mLocation.hasLocationEnabled()) {

            SimpleLocation.openSettings(this);
        }

        mSharedPreferences = getSharedPreferences(PREFERENCES_DATA, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mMappingData = mSharedPreferences.getString(KEY_LOCATIONDATA,"");

    }


    /**
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap map) {
        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);
        map.setOnMarkerClickListener(this);
        mMap = map;
        map.setOnMyLocationButtonClickListener(this);
        enableMyLocation();

        //SET THE CURRENT INFORMATION TO THE PREFERENCES

        mSharedPreferences.edit().putString("lat", String.valueOf(latitude)).commit();
        mSharedPreferences.edit().putString("long", String.valueOf(longitude)).commit();
    }
    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
        onMyLocationButtonClick();

    }
    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
       //latDestination = mLocation.getLatitude();
       //longDestination = mLocation.getLongitude();
        return false;

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


    public void onMapClick(LatLng point) {
        onMapReset();
        mDestinationMarker = mMap.addMarker(new MarkerOptions()
                .position(point)
                .title("Start")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        //Toast.makeText(this,"OnMapClick Clicked" + point, Toast.LENGTH_SHORT).show();
        CURRENT_LOCATION_SELECTED = true;
        mRouteCurrentPositionLatLng = point;

    }
    public void onMapLongClick(LatLng point) {

        if (CURRENT_LOCATION_SELECTED == false) {

            Toast.makeText(this, "select a starting position", Toast.LENGTH_SHORT).show();

        } else if (CURRENT_LOCATION_SELECTED == true && DESTINATION_LOCATION_SELECTED == false) {
            mDestinationMarker = mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Destination")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            DESTINATION_LOCATION_SELECTED = true;
            //Toast.makeText(this, "OnMapLongClick Clicked" + point, Toast.LENGTH_SHORT).show();
            mRouteDestinationLatLng = point;
        } else {
            Toast.makeText(this, "It is not possible to set Two Destinations Resetting map", Toast.LENGTH_LONG).show();
            onMapReset();

        }
        latDestination = point.latitude;
        longDestination = point.longitude;
    }



    public boolean onMarkerClick(Marker marker){

        requestDirection();

        // returning false allows the methods in the event to the called but prevents the default activity of a button click
        // allowing destination/ current location labels to appear over marker
        return false;

    }
    public void requestDirection() {
        GoogleDirection.withServerKey(SERVER_API_KEY)
                .from(mRouteCurrentPositionLatLng)
                .to(mRouteDestinationLatLng)
                .transportMode(TransportMode.DRIVING)
                .execute(this);
    }


    public void onDirectionSuccess(Direction direction, String rawBody) {
        Toast.makeText(this, "MAP Service Response : " + direction.getStatus(), Toast.LENGTH_SHORT).show();
        if (direction.isOK()) {

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED));
            Route route = direction.getRouteList().get(0);
            Leg leg = route.getLegList().get(0);
            String addressInfo = leg.getEndAddress();
            mRequestAddress = addressInfo.toString();
            Info distanceInfo = leg.getDistance();
            String distance = distanceInfo.getText();
            calcRouteDuration(distance);
            //TimeInfo mapRoute = leg.getArrivalTime();
            //List<Step> stepList = leg.getStepList();
            Info durationInfo = leg.getDuration();
            mRouteDuration = durationInfo.getText();
            Toast.makeText(this, "The time to travel is  : " + mRouteDuration, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "The location is  : " + mRequestAddress, Toast.LENGTH_SHORT).show();
            Log.i(LOG_EVENT, "From Unix time stamp: "+ mRoutePredictionTime);


        }
    }

    public void calcRouteDuration(String distance){
        double distanceString = Double.valueOf(distance.substring(0,distance.indexOf(" km")));
        //double distanceString = Integer.parseInt(distance);
        long HOUR_IN_SECONDS = 60*60;
        long millis = System.currentTimeMillis();
        //AKExorcist library doesnt output duration in mins, it outputs as a string in hours and mins
        //eg 4 hours 3 mins.  Whereas google api outputs in mins. Instead of building a parsing function
        // i will create approximate journey time in mins based on average speed of 60 kph over length of journey
        if(distanceString > 0 && distanceString <= 60)
        {
            mRoutePredictionTime = millis/1000;
        }
        else if(distanceString > 60 && distanceString <= 110)
            {
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*2);
        }
        else if(distanceString > 140 && distanceString <= 240)
            {
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*3);
        }
        else if(distanceString > 240 && distanceString <= 300)
            {
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*4);
        }
        else if(distanceString > 300 && distanceString <= 360)
            {
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*5);
        }
        else if(distanceString > 360 && distanceString <= 420)
            {
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*6);
        }
        else if(distanceString > 420 && distanceString <= 480)
            {
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*7);
        }
        else if(distanceString > 480 && distanceString <= 540)
            {
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*8);
        }
        else if(distanceString > 540 && distanceString <= 600)
            {
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*9);
        }
        else if(distanceString > 600 && distanceString <= 660)
            {
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*10);
        }
        else if(distanceString > 660 && distanceString <= 720)
            {
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*11);
        }
        else if(distanceString > 720 && distanceString <= 780)
            {
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*12);
        }
        else if(distanceString > 780 && distanceString <= 840)
            {
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*13);
        }
        else if(distanceString > 840 && distanceString <= 900)
            {
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*14);
        }
        else if(distanceString > 900){
            mRoutePredictionTime = ((millis/1000)+HOUR_IN_SECONDS*15);
        }

    }

    public void onDirectionFailure(Throwable t) {
        Toast.makeText(this, t.getMessage(), Toast.LENGTH_SHORT).show();
    }

    //check if there is any markers on the map
    private boolean mapStatus() {
        if (mMap == null) {
            return false;
        }
        return true;
    }

    // Reset any markers on the map, otherwise they will stack
    // Reset map click values to false
    public void onMapReset() {
        if (!mapStatus()) {
            return;
        }
        mMap.clear();
        CURRENT_LOCATION_SELECTED = false;
        DESTINATION_LOCATION_SELECTED = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        getMenuInflater().inflate(R.menu.main, menu);
        return true;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if (item.getItemId() == R.id.menu_prediction) {

            mSharedPreferences.edit().putString("KEY_DESTINATION", String.valueOf(mRouteDestinationLatLng)).apply();
            mSharedPreferences.edit().putString("KEY_TIME", String.valueOf(mRouteDuration)).apply();

            Intent intent = new Intent(this, PredictionActivity.class);
            //intent.putExtra("predictionDestination", mRouteDestinationLatLng);
            intent.putExtra("routeDuration", mRouteDuration);
            intent.putExtra("destinationAddress", mRequestAddress);
            intent.putExtra("latitude", latDestination);
            intent.putExtra("longitude", longDestination);
            intent.putExtra("unixTime",mRoutePredictionTime);
            startActivity(intent);

        } else if (item.getItemId() == R.id.menu_destination) {

            mMap.clear();

        }
        return true;
    }

    private void startWeatherActivity(String time, LatLng destination){
        Intent intent = new Intent(this, PredictionActivity.class);
        intent.putExtra(PREDICTION_START,time);
        intent.putExtra(PREDICTION_LOCATION, destination);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // make the device update its location
      //  mLocation.beginUpdates();
    }

    @Override
    protected void onPause() {
        // stop location updates (saves battery)
        //mLocation.endUpdates();

        super.onPause();
    }

}
