package x11108142.franmaguire.smartoutdoors;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String PREFERENCES_DATA = "x11108142.franmaguire.smartoutdoors";
    private static final String KEY_LOCATIONDATA = "key_gps_latlng";
    private static LatLng mRouteDestinationLatLng;
    private static LatLng mRouteCurrentPositionLatLng;

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
        map.setOnMapLongClickListener(this);
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
        onMapReset();
        mDestinationMarker = mMap.addMarker(new MarkerOptions()
                .position(point)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        Toast.makeText(this,"OnMapLongClick Clicked" + point, Toast.LENGTH_SHORT).show();
        mRouteDestinationLatLng = point;
        Toast.makeText(this, ""+mRouteDestinationLatLng, Toast.LENGTH_SHORT).show();
    }

    //check if there is any markers on the map
    private boolean mapStatus() {
        if (mMap == null) {
            return false;
        }
        return true;
    }

    //reset any markers on the map, otherwise they will stack
    public void onMapReset() {
        if (!mapStatus()) {
            return;
        }
        mMap.clear();
    }

}
