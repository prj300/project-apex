package apex.prj300.ie.apex.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import apex.prj300.ie.apex.app.classes.db.RouteDB;
import apex.prj300.ie.apex.app.classes.db.UserDB;
import apex.prj300.ie.apex.app.classes.enums.Grade;
import apex.prj300.ie.apex.app.classes.enums.Terrain;
import apex.prj300.ie.apex.app.classes.methods.JSONParser;
import apex.prj300.ie.apex.app.classes.models.Route;
import apex.prj300.ie.apex.app.classes.models.Results;
import apex.prj300.ie.apex.app.classes.models.User;

import static android.view.View.OnClickListener;
import static com.google.android.gms.common.api.GoogleApiClient.*;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;

public class RecordRouteActivity extends FragmentActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    JSONParser jsonParser = new JSONParser();
    Gson gson = new Gson();
    private JSONObject json;

    /**
     * Static Variables
     */
    protected static final String TAG = "RecordRouteActivity";
    protected static final String TAG_DIALOG_MESSAGE = "Do you wish to save this route?";
    protected static final String TAG_DIALOG_TITLE = "Save";
    protected static final String TAG_DIALOG_SAVE = "OK";
    protected static final String TAG_DIALOG_DISCARD = "No thanks";
    protected static final String TAG_DIALOG_TERRAIN = "No thanks";

    // Desired interval for location updates
    public static final long UPDATE_INTERVAL_IN_MS = 1000;

    // Fastest rate for location updates
    public static final long FASTEST_UPDATE_INTERVAL_IN_MS = UPDATE_INTERVAL_IN_MS / 2;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    // Entry point to Google Play Services
    protected GoogleApiClient mGoogleApiClient;

    // Stores parameters for requests to FusedLocationProviderApi;
    protected LocationRequest mLocationRequest;

    // Represents geographical location
    protected Location mCurrentLocation;

    // UI Widgets
    protected Button mButtonRecord;
    protected Button mButtonStop;
    protected TextView mTextDistance;

    // Tracks status of location updates request
    protected Boolean mRequestingLocationUpdates;

    protected Boolean mRecordRoute;

    // Tracks distance covered
    protected float mDistance;
    protected float mTotalDistance = 0;

    // Track currentSpeed
    protected double mCurrentSpeed;

    // Timer
    protected Long mStartTime;
    protected Long mEndTime;
    protected Long mTimeDifference;
    // protected Date mTotalTime;

    // Current date
    protected static Calendar mCalendar = Calendar.getInstance();

    // ArrayList to save LatLng points
    protected List<LatLng> mRoute = new ArrayList<>();

    protected Polyline line;

    /**
     * Route Model properties
     */
    protected Grade rGrade;
    protected Terrain rTerrain;
    protected List<Double> rLatPoints = new ArrayList<>();
    protected List<Double> rLngPoints = new ArrayList<>();
    protected Float rDistance = mTotalDistance;
    protected Date rDateCreated;

    /**
     * Route Result Model properties
     */
    // protected int mUserId;
    protected int rRouteId;
    protected int mExperience;
    // protected float mDistance;
    protected float mMaxSpeed = 0;
    protected float mAvgSpeed = 0;
    protected Date mTotalTime;
    protected int mCaloriesLost;
    // protected Date DateCreated;

    /**
     * User Model properties/context
     */
    protected User mUser;
    protected int mId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_route);
        setUpMapIfNeeded();
        getUserContext();
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
                Log.i(TAG, "Total time: " + mTimeDifference);
                stopUpdatesButtonHandler();
                createSaveRouteDialog();
            }
        });

        // Build GoogleApiClient
        buildGoogleApiClient();

    }

    private void getUserContext() {
        // open UserDB connection
        UserDB db = new UserDB(this);

        // Get User
        mUser = db.getUser();
        mId = mUser.getId();

        // close connection
        db.close();
    }

    /**
     * Build dialog box asking user if they wish to save route
     */
    private void createSaveRouteDialog() {
        // Instantiate Alert Dialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(TAG_DIALOG_MESSAGE)
                .setTitle(TAG_DIALOG_TITLE);

        // Save route
        builder.setPositiveButton(TAG_DIALOG_SAVE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createTerrainDialog();
                saveRoute();
                saveResults();
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

    private void saveResults() {
        UserDB db = new UserDB(this);
        User user = db.getUser();

        rRouteId = 1;
        mExperience = 50;
        mDistance = mTotalDistance;
        Results results;
    }

    /**
     * Dialog box to decide a route's terrain
     */
    private void createTerrainDialog() {
        // Create String list and store Terrain enum items
        List<String> terrainItems = new ArrayList<>();
        terrainItems.add(Terrain.Dirt.toString());
        terrainItems.add(Terrain.Road.toString());
        terrainItems.add(Terrain.Gravel.toString());
        final CharSequence[] terrains = terrainItems.toArray(new CharSequence[terrainItems.size()]);

        // Instantiate Alert Dialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(TAG_DIALOG_TERRAIN)
                .setItems(terrains, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choice) {
                        // switch statement
                        switch (choice) {
                            case 0:
                                rTerrain = Terrain.Dirt;
                                break;
                            case 1:
                                rTerrain = Terrain.Road;
                                break;
                            case 2:
                                rTerrain = Terrain.Gravel;
                                break;
                        }
                    }
                });
    }

    /**
     * Save route to SQLite
     */
    private void saveRoute() {
        rDateCreated = mCalendar.getTime();
        RouteDB db = new RouteDB(this);
        // drop previous data
        db.resetTables();
        Route route = new Route(mId, Grade.A, rTerrain, rLatPoints, rLngPoints, mTotalDistance, rDateCreated);
        db.addRoute(route);
        Log.i(TAG, "Route Saved Locally:" + route);
    }

    /**
     * Build Entry Point to Play Services
     */
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
        trackDistance();
        trackSpeed();
        saveLocation();
        updateUI();
    }

    /**
     * Formula to track speed
     */

    private void trackSpeed() {
        // formula taken from StackOverflow - http://bit.ly/1zQgFdz
        double R = 6371000;
        if(!mRoute.isEmpty()) {
            double x1 = mRoute.get(mRoute.size() - 1).latitude; // previous latitude point in array
            double y1 = mRoute.get(mRoute.size() - 1).longitude; // previous longitude point in array
            double x2 = mCurrentLocation.getLatitude();
            double y2 = mCurrentLocation.getLongitude();
            // set previous location parameters
            double dLat = x2 - x1;
            double dLng = y2 - y1;
            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(x1) * Math.cos(x2) *
                            Math.sin(dLng/2) * Math.sin(dLng/2);
            mCurrentSpeed = 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

            // avg speed
            long currentTime = System.currentTimeMillis();
            mTimeDifference = ((currentTime - mStartTime) * 1000); // TODO: Fix conversions
            mAvgSpeed = mTotalDistance/mTimeDifference;

            Log.i(TAG, "Speed: " + mCurrentSpeed);
            Log.i(TAG, "Average Speed: " + mAvgSpeed);
        }

        // max speed
        if(mCurrentSpeed > mMaxSpeed) {
            mMaxSpeed = (float) mCurrentSpeed;
            Log.i(TAG, "Max Speed: " + mMaxSpeed);
        }

    }

    /**
     * Track total distance covered
     */
    private void trackDistance() {
        if(!mRoute.isEmpty()) {
            Location lastLocation = new Location("LastLocation");
            double x = mRoute.get(mRoute.size()-1).latitude; // previous latitude point in array
            double y = mRoute.get(mRoute.size()-1).longitude; // previous longitude point in array

            // set previous location parameters
            lastLocation.setLatitude(x);
            lastLocation.setLongitude(y);

            if(mCurrentLocation != lastLocation) {
                // find distance between two neighbouring points
                // add to total distance
                mDistance = lastLocation.distanceTo(mCurrentLocation); // metres
                mTotalDistance += mDistance; // total metres
                Log.i(TAG, "Distance (m): " + mTotalDistance);
            }

        }
    }

    /**
     * Save location to array
     */
    private void saveLocation() {
        LatLng latLong = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        // save points to array
        Log.i(TAG, "Saving points: " + latLong);
        mRoute.add(latLong);

        rLatPoints.add(mCurrentLocation.getLatitude());
        rLngPoints.add(mCurrentLocation.getLongitude());
    }


    /**
     * Update UI in real-time
     * Follow User as they move
     * Show distance covered
     */
    private void updateUI() {
        LatLng latLng = new LatLng(
                mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
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