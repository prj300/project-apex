package apex.prj300.ie.apex.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import apex.prj300.ie.apex.app.classes.db.RouteDB;
import apex.prj300.ie.apex.app.classes.enums.Grade;
import apex.prj300.ie.apex.app.classes.enums.Terrain;
import apex.prj300.ie.apex.app.classes.models.LatLong;
import apex.prj300.ie.apex.app.classes.models.Route;

import static android.view.View.OnClickListener;
import static com.google.android.gms.common.api.GoogleApiClient.*;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;

public class RecordRouteActivity extends FragmentActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "record-route-activity";
    protected static final String TAG_DIALOG_MESSAGE = "Do you wish to save this route?";
    protected static final String TAG_DIALOG_TITLE = "Save";
    protected static final String TAG_DIALOG_SAVE = "OK";
    protected static final String TAG_DIALOG_DISCARD = "No thanks";

    /**
     * Desired interval for location updates
     */
    public static final long UPDATE_INTERVAL_IN_MS = 1000;

    /**
     * Fastest rate for location updates
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MS = UPDATE_INTERVAL_IN_MS / 2;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    /**
     * Entry point to Google Play Services
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to FusedLocationProviderApi
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents geographical location
     */
    protected Location mCurrentLocation;

    // UI Widgets
    protected Button mButtonRecord;
    protected Button mButtonStop;
    protected TextView mTextDistance;

    /**
     * Tracks status of location updates request
     */
    protected Boolean mRequestingLocationUpdates;

    protected Boolean mRecordRoute;

    /**
     * Tracks distance covered
     */
    protected float mDistance;
    protected float mTotalDistance = 0;

    /**
     * Timer
     */
    protected Long mStartTime;
    protected Long mEndTime;
    protected Long mTimeDifference;
    protected Date mTotalTime;

    /**
     * Current date
     */
    protected Calendar mCalendar = Calendar.getInstance();
    Date today = mCalendar.getTime();

    /**
     * ArrayList to save LatLng points
     */
    protected List<LatLng> mRoute = new ArrayList<>();

    protected Polyline line;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_route);
        setUpMapIfNeeded();

        // Locate the UI widgets
        mButtonRecord = (Button) findViewById(R.id.btnRecord);
        mButtonStop = (Button) findViewById(R.id.btnStop);
        mTextDistance = (TextView) findViewById(R.id.txtDistance);

        mRequestingLocationUpdates = false;
        mRecordRoute = false;

        // Register listeners for buttons
        mButtonRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Start Recording");
                mStartTime = System.currentTimeMillis(); // start time
                startUpdatesButtonHandler();
            }
        });

        mButtonStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Stop Recording");
                mEndTime = System.currentTimeMillis(); // end time
                mTimeDifference = ((mEndTime - mStartTime) / 1000);
                mTotalTime = new Date(mTimeDifference);
                stopUpdatesButtonHandler();
                saveRouteDialog();
            }
        });

        // Build GoogleApiClient
        buildGoogleApiClient();

    }

    /**
     * Build dialog box asking user if they wish to save route
     */
    private void saveRouteDialog() {
        // Instantiate Alert Dialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(TAG_DIALOG_MESSAGE)
                .setTitle(TAG_DIALOG_TITLE);

        // Save route
        builder.setPositiveButton(TAG_DIALOG_SAVE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveRoute();
            }
        });
        // Discard
        builder.setNegativeButton(TAG_DIALOG_DISCARD, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO
            }
        });
        // Create AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Save route to SQLite
     */
    private void saveRoute() {
        RouteDB db = new RouteDB(this);
        // drop previous data
        db.resetTables();
        // Gson converts objects to JSON
        Gson gson = new Gson();
        String jsonRoute = gson.toJson(mRoute);
        Route route = new Route(1, Grade.A, Terrain.Dirt, jsonRoute, mTotalDistance, mTotalTime, today);
        db.addRoute(route);
        Log.i(TAG, "Route Saved Locally");
    }


    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up location request boundaries
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets desired interval for active location updates
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MS);

        // Sets fastest interval for location updates
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void startUpdatesButtonHandler() {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates. Does nothing if
     * updates were not previously requested.
     */
    public void stopUpdatesButtonHandler() {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mButtonRecord.setEnabled(false);
            mButtonStop.setEnabled(true);
        } else {
            mButtonRecord.setEnabled(true);
            mButtonStop.setEnabled(false);
        }
    }

    /**
     * Requests location updates from FusedLocationApi
     */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this
        );
    }

    /**
     * Removes location updates from the FusedLocationApi
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates but don't disconnect the GoogleApiClient
        // stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        */
    }

    /**
     * Runs when GoogleApiClient is successfully connected
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");

        if(mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // If user presses Start button before GoogleApiClient connects
        // set mRequestingLocationUpdates to true
        if(mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // Connection to Play Services lost
        // Try to re-connect
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    /**
     * Callback that fires when location changes
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.i(TAG, "Current Location: " + latLng);
        trackDistance(location);
        saveLocation(location);
        updateUI(location);
    }

    /**
     * Track total distance covered
     */
    private void trackDistance(Location location) {
        if(!mRoute.isEmpty()) {
            Location lastLocation = new Location("LastLocation");
            double latA = mRoute.get(mRoute.size()-1).latitude; // previous latitude point in array
            double lngA = mRoute.get(mRoute.size()-1).longitude; // previous longitude point in array

            // set previous location parameters
            lastLocation.setLatitude(latA);
            lastLocation.setLongitude(lngA);

            // find distance between two neighbouring points
            // add to total distance
            mDistance = lastLocation.distanceTo(location); // metres
            mTotalDistance += mDistance; // total metres
            Log.i(TAG, "Distance (m): " + mTotalDistance);

        }
    }

    /**
     * Save location to array
     */
    private void saveLocation(Location location) {
        LatLng latLong = new LatLng(location.getLatitude(), location.getLongitude());
        // save points to array
        Log.i(TAG, "Saving points: " + latLong);
        mRoute.add(latLong);
    }


    /**
     * Update UI in real-time
     * Follow User as they move
     * Show distance covered
     */
    private void updateUI(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        // plot points on UI
        line = mMap.addPolyline(new PolylineOptions()
                .addAll(mRoute)
                .width(6f)
                .color(Color.BLUE)
                .geodesic(true));

        // move camera to updated position on map
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        mMap.animateCamera(cameraUpdate);

        if(mTotalDistance < 1000) {
            // display in metres
            mTextDistance.setText(String.format("%.1f (m)", mTotalDistance));
        }
        else {
            // display in kilometres
            mTextDistance.setText(String.format("%.2f (km)", mTotalDistance/1000));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        stopLocationUpdates();
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

}
