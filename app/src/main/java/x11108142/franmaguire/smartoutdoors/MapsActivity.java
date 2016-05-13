package x11108142.franmaguire.smartoutdoors;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

/**
 * The methods from "MyLocationDemoActivity" where used in this activity AND the entire class "PermissionUtils" was added to this project
 * available from Google API Demo's found at https://github.com/googlemaps/android-samples
 *
 * The methods from MyLocationDemoActivity were reused as they performed the functionality required
 * The class PermissionUtils was resued, It was a utility class that manages access to runtime permissions
 *
 *
 *
 * */

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback, DirectionCallback {

    private static final String PREFERENCES_DATA = "x11108142.franmaguire.smartoutdoors";
    private static final String KEY_LOCATIONDATA = "key_gps_latlng";
    private static final String SERVER_API_KEY = "AIzaSyA_WrpoePiNDpgI79qubAQBvQxiUxlAzwA";
    private static LatLng mRouteDestinationLatLng;
    private static LatLng mRouteCurrentPositionLatLng;
    private static boolean DESTINATION_LOCATION_SELECTED = false;
    private static boolean CURRENT_LOCATION_SELECTED = false;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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
        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();


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
        Toast.makeText(this, "Success with status : " + direction.getStatus(), Toast.LENGTH_SHORT).show();
        if (direction.isOK()) {

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED));
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

}
