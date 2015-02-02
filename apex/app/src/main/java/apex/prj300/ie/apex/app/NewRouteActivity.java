package apex.prj300.ie.apex.app;

import java.sql.Time;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import apex.prj300.ie.apex.app.classes.db.ResultsDB;
import apex.prj300.ie.apex.app.classes.db.RouteDB;
import apex.prj300.ie.apex.app.classes.db.UserDB;
import apex.prj300.ie.apex.app.classes.enums.Grade;
import apex.prj300.ie.apex.app.classes.enums.HttpMethod;
import apex.prj300.ie.apex.app.classes.enums.Terrain;
import apex.prj300.ie.apex.app.classes.methods.JSONParser;
import apex.prj300.ie.apex.app.classes.models.Results;
import apex.prj300.ie.apex.app.classes.models.Route;
import apex.prj300.ie.apex.app.classes.models.User;

import static android.view.View.*;
import static com.google.android.gms.common.api.GoogleApiClient.*;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;


public class NewRouteActivity extends FragmentActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    // progress dialog for AsyncTask
    private ProgressDialog mProgressDialog;
    /**
     * JSONParser that will parse data to send to server
     */
    JSONParser jsonParser = new JSONParser();
    Gson gson = new Gson();
    // boolean to handle network state
    boolean isConnected = false;

    /**
     * Static Variables
     */
    protected static final String TAG_CONTEXT = "NewRouteActivity";
    protected static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static int indicator = 0;
    private static String message;

    // Desired interval for location updates
    public static final long UPDATE_INTERVAL_MS = 1000;
    // Fastest rate for location updates
    public static final long FASTEST_UPDATE_INTERVAL_MS = UPDATE_INTERVAL_MS / 2;


    // Google Map
    protected GoogleMap mMap;
    // Entry point to Play Services
    protected GoogleApiClient mGoogleApiClient;
    // Stores parameters for requests to FusedLocationProviderApi
    protected LocationRequest mLocationRequest;
    // Represents a Geographical location
    protected Location mLocation;
    protected static double mLatitude; // current latitude
    protected static double mLongitude; // current longitude
    // Tracks status of location updates request
    protected Boolean mRequestingLocationUpdates;
    // Tracks whether route should be recorded or not
    protected Boolean mRecordRoute;

    // Stores user's geographical points
    protected static List<LatLng> mLatLngs = new ArrayList<>();
    // Displays user's geographical points
    protected Polyline line;

    // Tracks distance covered
    protected static float mDistance;
    protected static float mTotalDistance;

    // Tracks time elapsed
    protected Long mStartTime; // (milliseconds)
    protected Long mEndTime;
    protected Long mTimeDifference;

    /**
     * User Model properties (as needed)
     */
    protected static User mUser = new User();
    protected static int mId;

    /**
     * Route Model Properties
     */
    protected static Grade routeGrade;
    protected static Terrain routeTerrain;
    protected List<Double> routeLats = new ArrayList<>();
    protected List<Double> routeLngs = new ArrayList<>();
    protected static float routeDistance;
    protected Date dateCreated;

    /**
     * Results Model Properties
     */
    protected int routeId;
    protected float mMaxSpeed;
    // protected float routeDistance;
    protected float mAvgSpeed;
    protected List<Float> mSpeeds;
    protected Time mTime;
    protected List<Long> mTimes = new ArrayList<>();
    // protected Date dateCreated;

    /**
     * UI Widgets
     */
    // Buttons
    protected ImageView mButtonRecord;
    protected ImageView mButtonPause;
    protected ImageView mButtonStop;
    // TextViews


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_route);
        setUpMapIfNeeded();
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        getCurrentUser();

        // Locate UI Widgets
        mButtonRecord = (ImageView) findViewById(R.id.btnRecord);
        mButtonStop = (ImageView) findViewById(R.id.btnStop);

        // Set Location Updates status to false
        mRequestingLocationUpdates = false;
        mRecordRoute = false;
        mTotalDistance = 0;

        // Create Listeners for Record/Stop buttons
        mButtonRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG_CONTEXT, "Start Recording.");
                mStartTime = System.currentTimeMillis(); // start timer
                startUpdatesButtonHandler();
            }
        });
        mButtonStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG_CONTEXT, "Stop recording.");
                trackTime();
                stopUpdatesButtonHandler();
                saveRouteDialog();
            }

        });

        buildGoogleApiClient();

    }

    /**
     * User currently logged in
     */
    private void getCurrentUser() {
        UserDB db = new UserDB(this);

        mUser = db.getUser();
        mId = mUser.getId();
        db.close();
    }

    /**
     * Getting total journey time
     */
    public void trackTime() {
        mEndTime = System.currentTimeMillis();

        mTimeDifference = mEndTime - mStartTime;

        // mTimes.add(mTimeDifference); // logging each time instance

        String time = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(mTimeDifference),
                TimeUnit.MILLISECONDS.toMinutes(mTimeDifference) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(mTimeDifference) % TimeUnit.MINUTES.toSeconds(1));

        mTime = Time.valueOf(time);
        Log.d(TAG_CONTEXT, "Time: " + mTime);

    }

    /**
     * AlertDialog to save or discard new route
     */
    public void saveRouteDialog() {
        // Instantiate an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(NewRouteActivity.this);
        // Set main message
        builder.setMessage(R.string.dialog_save_route)
                // Set up buttons, and what action to take
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG_CONTEXT, "Saving new route");
                        chooseTerrainDialog(); // choose a terrain
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        // Crate builder and display
        builder.create();
        builder.show();
    }

    /**
     * Check to see if there is an internet connection
     * If none return false
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm  = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        isConnected = networkInfo != null
                && networkInfo.isConnected()
                && networkInfo.isAvailable();
        return isConnected;
    }


    public void chooseTerrainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NewRouteActivity.this);
        builder.setTitle(R.string.pick_terrain)
                .setItems(R.array.terrains_array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                routeTerrain = Terrain.Road;
                            case 1:
                                routeTerrain = Terrain.Gravel;
                            case 2:
                                routeTerrain = Terrain.Dirt;
                        }
                        Log.d(TAG_CONTEXT, "Terrain selected - " + routeTerrain);
                        calculateGrade();
                        saveNewRoute();
                    }
                });
        builder.create();
        builder.show();
    }

    /**
     * Calculate route's grade
     */
    private void calculateGrade() {

    }

    /**
     * Saving route to SQLite Database
     */
    public void saveNewRoute() {
        RouteDB db = new RouteDB(this);

        // getting today's date
        java.util.Date utilDate = new java.util.Date();
        // converting to sql.date
        dateCreated = new java.sql.Date(utilDate.getTime());

        routeGrade = Grade.A;

        // build route properties from results
        Route newRoute = new Route(mId, routeGrade, routeTerrain, routeLats, routeLngs, mTotalDistance, dateCreated);
        db.addRoute(newRoute);
        db.close();

        Log.d(TAG_CONTEXT, "Route Saved Locally");
        Log.i(TAG_CONTEXT, "Time: " + mTime);
        Log.i(TAG_CONTEXT, "User: " + mId
                + ", Route Grade: " + Grade.A
                + ", Route Terrain: " + routeTerrain
                + ", Distance: " + mTotalDistance
                + ", Date Created: " + dateCreated);

        saveResults();
    }

    /**
     * Saving results
     */
    public void saveResults() {
        ResultsDB db = new ResultsDB(this);
        Results newResult = new Results(mTotalDistance, mMaxSpeed, mAvgSpeed, mTime, dateCreated);
        db.addResult(newResult);
        db.close();
        Log.i(TAG_CONTEXT, "Total Distance: " + mTotalDistance
                + ", Max Speed: " + mMaxSpeed
                + ", Average Speed: " + mAvgSpeed
                + ", Time: " + mTime
                + ", Date Created: " + dateCreated);

        isNetworkAvailable(); // check for connection
        Log.d(TAG_CONTEXT, "Network availability = " + isConnected);
        if (isConnected) {
            new SaveNewRoute().execute();
        } else {
            Log.d(TAG_CONTEXT, "No network connection!");
            Toast.makeText(getApplicationContext(),
                    "No network connection!", Toast.LENGTH_LONG).show();
        }
    }

    private void setUpMapIfNeeded() {
        if(mMap != null) {
            return;
        }
        mMap = ((SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.map))
                .getMap();
        if(mMap != null) {
            setUpMap();
        }
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
    }


    /**
     * Build entry point params for Play Services
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG_CONTEXT, "Building GoogleApiClient...");
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
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets desired interval for active location updates
        mLocationRequest.setInterval(UPDATE_INTERVAL_MS);

        // Sets fastest interval for location updates
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    private void startUpdatesButtonHandler() {
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
     * Removes location updates from the FusedLocationApi
     */
    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.
                removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Requests location updates from FusedLocationApi
     */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_route, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
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

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG_CONTEXT, "Connected to GoogleApiClient");

        if (mLocation == null) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // If user presses Start button before GoogleApiClient connects
        // set mRequestingLocationUpdates to true
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Connection to Play Services lost
        // Try to re-connect
        Log.i(TAG_CONTEXT, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        trackDistance();
        trackTime();
        trackSpeed();
        saveLocation();
        updateUI();
    }

    /**
     * Tracking speed
     * Average, Max and Instantaneous
     */
    private void trackSpeed() {
        // float mCurrentSpeed = Float.NaN;
        /*
        if(!mTimes.isEmpty()) {
            long lastTime = mTimes.get(mTimes.size() -1);

            (Math.pow(Math.sqrt(mLatitude - lastLat), 2) + Math.pow(Math.sqrt(mLongitude - lastLong), 2));
        }*/

        // Avg speed formula
        float avgSpeed = (mTotalDistance * 360000 / mTimeDifference);

        // Format to two decimal places
        mAvgSpeed = Float.valueOf(String.format("%.2f", avgSpeed));

        Log.i(TAG_CONTEXT, "Avg Speed: " + mAvgSpeed);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        stopLocationUpdates();
        Toast.makeText(getApplicationContext(),
                "Lost connection to network!", Toast.LENGTH_SHORT).show();
        Log.i(TAG_CONTEXT, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }


    /**
     * Track total distance covered
     */
    public void trackDistance() {
        if (!mLatLngs.isEmpty()) {
            Location lastLocation = new Location("LastLocation");
            double x = mLatLngs.get(mLatLngs.size() - 1).latitude; // previous latitude point in array
            double y = mLatLngs.get(mLatLngs.size() - 1).longitude; // previous longitude point in array

            // set previous location parameters
            lastLocation.setLatitude(x);
            lastLocation.setLongitude(y);

            if (mLocation != lastLocation) {
                // find distance between two neighbouring points
                // add to total distance
                mDistance = lastLocation.distanceTo(mLocation); // metres
                mTotalDistance += mDistance; // total metres
                Log.i(TAG_CONTEXT, "Distance (m): " + mTotalDistance);
            }
        }
    }

    /**
     * Save location to array
     */
    public void saveLocation() {
        LatLng latLong = new LatLng(mLatitude, mLongitude);
        // save points to array
        Log.i(TAG_CONTEXT, "Saving points: " + latLong);
        mLatLngs.add(new LatLng(mLatitude, mLongitude));
        routeLats.add(mLatitude);
        routeLngs.add(mLongitude);
    }

    /**
     * Update UI in real-time
     * Follow User as they move
     * Show distance covered
     */
    public void updateUI() {
        LatLng latLng = new LatLng(mLatitude, mLongitude);
        // plot points on UI
        line = mMap.addPolyline(new PolylineOptions()
                .addAll(mLatLngs)
                .width(6f)
                .color(Color.BLUE)
                .geodesic(true));

        // move camera to updated position on map
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        mMap.animateCamera(cameraUpdate);

        /*
        if(mTotalDistance < 1000) {
            // display in metres
            mTextDistance.setText(String.format("%.1f (m)", mTotalDistance));
        }
        else {
            // display in kilometres
            mTextDistance.setText(String.format("%.2f (km)", mTotalDistance/1000));
        }*/

    }

    private class SaveNewRoute extends AsyncTask<Route, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(NewRouteActivity.this);
            mProgressDialog.setMessage("Saving new route...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected Integer doInBackground(Route... args) {

            try {
                List<NameValuePair> params = new ArrayList<>();
                // Route parameters
                params.add(new BasicNameValuePair("user_id", String.valueOf(mId)));
                params.add(new BasicNameValuePair("grade", String.valueOf(routeGrade)));
                params.add(new BasicNameValuePair("terrain", String.valueOf(routeTerrain)));
                params.add(new BasicNameValuePair("latitudes", gson.toJson(routeLats)));
                params.add(new BasicNameValuePair("longitudes", gson.toJson(routeLngs)));
                params.add(new BasicNameValuePair("distance", String.valueOf(mTotalDistance)));
                // Results parameters
                params.add(new BasicNameValuePair("max_speed", String.valueOf(mMaxSpeed)));
                params.add(new BasicNameValuePair("avg_speed", String.valueOf(mAvgSpeed)));
                params.add(new BasicNameValuePair("time", String.valueOf(mTime)));

                JSONObject json = jsonParser.makeHttpRequest(getString(R.string.create_route_url), HttpMethod.POST, params);

                indicator = json.getInt(TAG_SUCCESS);
                message = json.getString(TAG_MESSAGE);

            } catch (JSONException e) {
                Log.d(TAG_CONTEXT, "JSONException " + e.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Something went wrong!", Toast.LENGTH_LONG).show();
            }
            return indicator;
        }

        protected void onPostExecute(Integer result) {
            Log.d(TAG_CONTEXT, "Success: " + indicator + ", Message: " + message);
            // dismiss Progress Dialog
            mProgressDialog.dismiss();
            // check indicator
            if(result == 1) {
                Log.i(TAG_CONTEXT, "Insert successful!");
                Toast.makeText(getApplicationContext(), "Route saved!", Toast.LENGTH_SHORT).show();
            } else {
                Log.i(TAG_CONTEXT, "Insert successful!");
                Toast.makeText(getApplicationContext(), "Failed to save route!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}