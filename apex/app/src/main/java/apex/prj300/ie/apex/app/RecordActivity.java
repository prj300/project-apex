package apex.prj300.ie.apex.app;

import android.gesture.OrientedBoundingBox;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.common.api.GoogleApiClient.*;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import static com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;

public class RecordActivity extends FragmentActivity implements LocationListener,
        OnConnectionFailedListener, ConnectionCallbacks,
        OnMyLocationButtonClickListener, View.OnClickListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mLocationClient;
    private OnConnectionFailedListener mOnConnectionFailedListener;
    private ConnectionCallbacks mConnectionCallbacks;
    private static final LocationRequest mLocationRequest = LocationRequest.create()
            .setInterval(20)                // 20ms
            .setFastestInterval(16)         // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    // ArrayList to save LatLng points
    List<LatLng> route = new ArrayList<>();
    Polyline line;

    OrientationEventListener mOrientationEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        wakeUpLocationClient();
        mLocationClient.connect();
    }

    /*
    private void wakeUpLocationClient() {
        if(mLocationClient == null) {
            mLocationClient = new LocationClient(getApplicationContext(),
                    this,       // Connection Callbacks
                    this);      // OnConnectionFailedListener
        }
    }
    */

    private void wakeUpLocationClient() {
        if(mLocationClient == null) {
            mLocationClient = new Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mOnConnectionFailedListener)
                    .build();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mLocationClient != null) {
            mLocationClient.disconnect();
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        gotoMyLocation(lat, lng);
        saveRoute(lat, lng);
    }

    /**
     * Save co-ordinates to array and draw route
     */
    private void saveRoute(double lat, double lng) {
        // add new co-ordinates to array
        route.add(new LatLng(lat, lng));
        // Log results
        Log.d("Latitude: ", String.valueOf(lat));
        Log.d("Longitude: ", String.valueOf(lng));
        // draw route dynamically
        line = mMap.addPolyline(new PolylineOptions()
                .addAll(route)
                .width(6f)
                .color(Color.BLUE)
                .geodesic(true));

    }

    /**
     * Follow user as they move
     */
    private void gotoMyLocation(double lat, double lng) {
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(lat, lng))
                        .bearing(0)
                        .zoom(15.5f)
                        .tilt(25)
                        .build()
        ), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                //TODO
            }

            @Override
            public void onCancel() {
                //TODO
            }
        });
    }

    private void changeCamera(CameraUpdate update, GoogleMap.CancelableCallback callback) {
        mMap.moveCamera(update);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}
