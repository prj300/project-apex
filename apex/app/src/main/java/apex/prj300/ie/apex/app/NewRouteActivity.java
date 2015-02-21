package apex.prj300.ie.apex.app;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
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
import apex.prj300.ie.apex.app.fragments.MyMapFragment;
import apex.prj300.ie.apex.app.fragments.MyStatsFragment;
import apex.prj300.ie.apex.app.interfaces.PassLocationListener;
import apex.prj300.ie.apex.app.interfaces.PassStatsListener;

import static android.view.View.*;
import static com.google.android.gms.common.api.GoogleApiClient.*;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;


public class NewRouteActivity extends FragmentActivity
        implements ActionBar.TabListener, LocationListener,
        ConnectionCallbacks, OnConnectionFailedListener {

    JSONParser jsonParser = new JSONParser();
    Gson gson = new Gson();
    private ProgressDialog mProgressDialog;

    /**
     * Static variables
     */
    // Activity context
    private static final String TAG_CONTEXT = "NewRouteActivity";

    // Desired interval for location updates
    public static final long UPDATE_INTERVAL_MS = 800;
    // Fastest rate for location updates
    public static final long FASTEST_UPDATE_INTERVAL_MS = UPDATE_INTERVAL_MS / 2;

    protected static final String TAG_SUCCESS = "success";


    /**
     * Variables that will be used and changed within the activity lifecycle
     */
    // Entry point to Play Services
    protected GoogleApiClient mGoogleApiClient;
    // Stores parameters for requests to FusedLocationProviderApi
    protected LocationRequest mLocationRequest;
    // Represents a Geographical location
    protected Location mLocation;
    protected static double mLatitude; // current latitude
    protected static double mLongitude; // current longitude
    // Tracks whether route should be recorded or not
    protected Boolean mRecordRoute;

    /**
     * Route Model Properties
     */
    protected static int routeId;
    protected static Grade routeGrade;
    protected static Terrain routeTerrain;
    protected List<Double> routeLats = new ArrayList<>();
    protected List<Double> routeLngs = new ArrayList<>();
    protected Date dateCreated;

    /**
     * Results Model Properties
     */
    protected static int resultId;
    protected static float mMaxSpeed;
    // protected float routeDistance;
    protected static float mAvgSpeed;
    // protected List<Float> mSpeeds;
    protected static long mTime;

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
    // holds all time instances per location changed
    protected static ArrayList<Long> mTimes = new ArrayList<>();

    // Declare interfaces for passing information between fragments
    protected PassLocationListener mLocationPasser;
    protected PassStatsListener mStatsPasser;

    protected Boolean mRequestingLocationUpdates;

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;

    /**
     * UI Widgets
     */
    protected ImageView mButtonRecord;
    protected ImageView mButtonStop;


    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_new_route);

        // Create the adapter that will return a fragment
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up action bar
        final ActionBar actionBar = getActionBar();
        // Returns to hierarchical parent
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
        }
        // Specify we will display tabs
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up ViewPager, attach adapter and set up a listener
        // for when user swipes between sections
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between sections, select the corresponding tab
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar
                    .newTab()
                    .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }

        // Locate UI widgets
        mButtonRecord = (ImageView) findViewById(R.id.btnRecord);
        mButtonStop = (ImageView) findViewById(R.id.btnStop);

        // Set Location Updates status to false
        mRequestingLocationUpdates = false;
        mRecordRoute = false;
        mTotalDistance = 0;

        mMaxSpeed = 0;

        // Create Listeners for Record/Stop buttons
        mButtonRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_CONTEXT, "Start Recording.");
                mStartTime = System.currentTimeMillis(); // start timer
                startUpdatesButtonHandler();
            }
        });
        mButtonStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Only do these actions if recording is on
                if(mRequestingLocationUpdates) {
                    Log.d(TAG_CONTEXT, "Stop recording.");
                    getTime();
                    stopUpdatesButtonHandler();
                    saveRouteDialog();
                }
            }

        });

        buildGoogleApiClient();

    }

    /**
     * User currently logged in
     */
    private User getUser() {
        UserDB db = new UserDB(this);

        User user = db.getUser();
        db.close();

        return user;
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
     * Getting total journey time in milliseconds
     */
    public void getTime() {
        // current time
        mEndTime = System.currentTimeMillis();

        mTime = mEndTime - mStartTime;

        // mTimes.add(mTimeDifference); // logging each time instance

        /*
        String time = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(mTimeDifference),
                TimeUnit.MILLISECONDS.toMinutes(mTimeDifference) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(mTimeDifference) % TimeUnit.MINUTES.toSeconds(1));
                */
        Log.d(TAG_CONTEXT, "Time: " + mTime);

        // Pass time to StatsFragment
        mStatsPasser.onTimeChanged(mTime);

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
                        clearStats();
                    }
                });
        // Crate builder and display
        builder.create();
        builder.show();
    }

    /**
     * Clear all stats
     * Reset
     */
    private void clearStats() {
        mMaxSpeed = 0;
        mAvgSpeed = 0;
        mTime = 0;
        mTimes.clear();
        mLatLngs.clear();
        routeLats.clear();
        routeLngs.clear();
        line = null;
        mDistance = 0;
    }

    /**
     * Check to see if there is an internet connection
     * If none return false
     */
    private boolean isNetworkAvailable() {
        // Create a connectivity manager and
        // get the service type currently in use (Wi-fi, 3g etc)
        ConnectivityManager cm  = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get network info from the service
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        // return true or false based on connection
        return (networkInfo != null)
                && networkInfo.isConnected()
                && networkInfo.isAvailable();
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
                                break;
                            case 1:
                                routeTerrain = Terrain.Gravel;
                                break;
                            case 2:
                                routeTerrain = Terrain.Dirt;
                                break;
                        }
                        Log.i(TAG_CONTEXT, "Terrain selected - " + routeTerrain);
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

        // For now this is the method for deciding a route's grade
        if(mTotalDistance < 10) {
            routeGrade = Grade.E;
        } else if (mTotalDistance < 20) {
            routeGrade = Grade.D;
        } else if (mTotalDistance < 35) {
            routeGrade = Grade.C;
        } else if (mTotalDistance < 50) {
            routeGrade = Grade.B;
        } else {
            routeGrade = Grade.A;
        }

        isNetworkAvailable(); // check for connection to the internet
        Log.d(TAG_CONTEXT, "Network availability = " + isNetworkAvailable());
        if (isNetworkAvailable()) {
            new SaveNewRoute().execute();
        } else {
            Log.d(TAG_CONTEXT, "No network connection!");
            Toast.makeText(getApplicationContext(),
                    "No network connection!", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Saving route to SQLite Database
     */
    public void saveNewRoute() {
        RouteDB db = new RouteDB(this);
        // clear previous data in tables
        db.resetTables();

        // getting today's date
        java.util.Date utilDate = new java.util.Date();
        // converting to sql.date
        dateCreated = new java.sql.Date(utilDate.getTime());

        // build route properties from results
        Route newRoute = new Route(routeId, getUser().getId(), routeGrade,
                routeTerrain, mTotalDistance, dateCreated);
        db.addRoute(newRoute);
        // now add lat and long points to separate table
        db.addLatsLong(routeId, routeLats, routeLngs);
        db.close();

        Log.d(TAG_CONTEXT, "Route Saved Locally");
        Log.i(TAG_CONTEXT, "Time: " + mTime);
        Log.i(TAG_CONTEXT, "User: " + getUser().getId()
                + ", Route Grade: " + routeGrade
                + ", Route Terrain: " + routeTerrain
                + ", Distance: " + mTotalDistance
                + ", Date Created: " + dateCreated);

    }

    /**
     * Saving results
     */
    public void saveResults() {
        // Connection to ResultsDB
        ResultsDB db = new ResultsDB(this);
        // Add results
        Results newResult = new Results(resultId, getUser().getId(), routeId,
                mTotalDistance, mMaxSpeed, mAvgSpeed, mTime, dateCreated);
        // Insert new result into database
        db.addResult(newResult);
        // close connection
        db.close();
        updateUser();
        Log.d(TAG_CONTEXT, "Result saved");
        Log.i(TAG_CONTEXT, "Result ID: " + resultId);
        Log.i(TAG_CONTEXT, "Route ID: " + routeId);
        Log.i(TAG_CONTEXT, "Distance: " + mTotalDistance);
        Log.i(TAG_CONTEXT, "Max Speed: " + mMaxSpeed);
        Log.i(TAG_CONTEXT, "Average Speed: " + mAvgSpeed);
        Log.i(TAG_CONTEXT, "Time: " + mTime);

    }

    /**
     * Update User table with new results
     */
    private void updateUser() {
        // Connection to database
        UserDB db = new UserDB(this);

        getResultsCount();
        float distance = getUser().getTotalDistance() + mTotalDistance;
        long time = getUser().getTotalTime() + mTime;
        float maxSpeed;
        if(mMaxSpeed > getUser().getMaxSpeed()) {
            maxSpeed = mMaxSpeed;
        } else {
            maxSpeed = getUser().getMaxSpeed();
        }

        User user = new User(getUser().getId(), distance, time, maxSpeed, getAverage(mAvgSpeed));
        // Update User table with new User stats
        db.updateUserStats(getUser().getId(), user);

        // send data to server
        if(isNetworkAvailable()) {
            new UpdateUser(getUser().getId(), distance, time, maxSpeed, getAverage(mAvgSpeed)).execute();
        } else {
            Log.i(TAG_CONTEXT, "Network unavailable");
            Toast.makeText(getApplicationContext(), "No network connection", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Get number of rows in Results Table
     */
    private int getResultsCount() {
        ResultsDB db = new ResultsDB(this);
        return db.rowCount();

    }

    /**
     * Calculate new overall average
     */
    private float getAverage(float avg) {
        ResultsDB db = new ResultsDB(this);

        return ((db.getAverageSpeed() + avg) / (getResultsCount() + 1));
    }

    /**
     * Sets up location request boundaries
     */
    public void createLocationRequest() {
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

    /**
     * Tracking speed
     * Average, Max and Instantaneous
     */
    private void getSpeed() {
        float speed = Float.NaN;

        // calculate instantaneous speed
        if(!mTimes.isEmpty() && (!mLatLngs.isEmpty())) {
            Location lastLocation = new Location("LastLocation");
            double x = mLatLngs.get(mLatLngs.size() - 1).latitude; // previous latitude point in array
            double y = mLatLngs.get(mLatLngs.size() - 1).longitude; // previous longitude point in array

            // set previous location parameters
            lastLocation.setLatitude(x);
            lastLocation.setLongitude(y);

            // speed = distance / time
            speed = ((lastLocation.distanceTo(mLocation)) * 3600000/1000) / (mTime - (mTimes.size()-1));
            Log.d(TAG_CONTEXT, "Current Speed: " + speed);
        }

        // Avg speed formula
        double i = (double) ((mTotalDistance * 3600000)/1000) / mTime;
        mAvgSpeed = (float) i;

        // Format to two decimal places
        mAvgSpeed = Float.valueOf(String.format("%.2f", mAvgSpeed));

        Log.i(TAG_CONTEXT, "Avg Speed: " + mAvgSpeed);

        // Pass speed to StatsFragment
        mStatsPasser.onCurrentSpeedChanged(speed);
        mStatsPasser.onAvgSpeedChanged(mAvgSpeed);
        if(speed > mMaxSpeed) {
            mMaxSpeed = speed;
            mStatsPasser.onMaxSpeedChanged(mMaxSpeed);
        }
        Toast.makeText(getApplicationContext(), "Speed: " + speed, Toast.LENGTH_SHORT).show();
    }

    /**
     * Track total distance covered
     */
    public void getDistance() {
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
                mTotalDistance += (mDistance/1000); // total km
                Log.i(TAG_CONTEXT, "Distance (km): " + mTotalDistance);
            }
        }

        // Pass total distance to MyStatsFragment
        mStatsPasser.onDistanceChanged(mTotalDistance);
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
     * Location Listener
     */
    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        mLocationPasser.onPassLocation(mLocation);
        getDistance();
        getTime();
        // add current time to times list
        mTimes.add(System.currentTimeMillis());
        getSpeed();
        saveLocation();
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
        Log.d(TAG_CONTEXT, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        stopLocationUpdates();
        Toast.makeText(getApplicationContext(),
                "Lost connection to network!", Toast.LENGTH_SHORT).show();
        Log.d(TAG_CONTEXT, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    /**
     * AsyncTask
     * Sending route and results to the server
     */
    private class SaveNewRoute extends AsyncTask<Void, Void, Integer> {

        int indicator;
        String message;

        // Show a progress Dialog before executing
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
        protected Integer doInBackground(Void... voids) {

            try {
                List<NameValuePair> params = new ArrayList<>();
                // Route parameters
                params.add(new BasicNameValuePair("user_id", String.valueOf(getUser().getId())));
                params.add(new BasicNameValuePair("grade", String.valueOf(routeGrade)));
                params.add(new BasicNameValuePair("terrain", String.valueOf(routeTerrain)));
                params.add(new BasicNameValuePair("latitudes", gson.toJson(routeLats)));
                params.add(new BasicNameValuePair("longitudes", gson.toJson(routeLngs)));
                params.add(new BasicNameValuePair("distance", String.valueOf(mTotalDistance)));
                params.add(new BasicNameValuePair("max_speed", String.valueOf(mMaxSpeed)));
                params.add(new BasicNameValuePair("avg_speed", String.valueOf(mAvgSpeed)));
                params.add(new BasicNameValuePair("time", String.valueOf(mTime)));

                JSONObject json = jsonParser.makeHttpRequest(getString(R.string.create_route_url), HttpMethod.POST, params);
                Log.d(TAG_CONTEXT, "JSON Parser: " + json);
                if(json != null) {
                    indicator = json.getInt(TAG_SUCCESS);
                    String TAG_MESSAGE = "message";
                    message = json.getString(TAG_MESSAGE);
                    String TAG_RESULT_ID = "result_id";
                    resultId = json.getInt(TAG_RESULT_ID);
                    String TAG_ROUTE_ID = "route_id";
                    routeId = json.getInt(TAG_ROUTE_ID);
                }

            } catch (JSONException e) {
                Log.e(TAG_CONTEXT, "JSONException " + e.getMessage());
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
                Log.i(TAG_CONTEXT, "Route saved!");
                Toast.makeText(getApplicationContext(), "Route saved!", Toast.LENGTH_SHORT).show();
                // Go to results activity
                // Intent intent = new Intent(getApplicationContext(), ResultsActivity.class);
                // startActivity(intent);
                saveNewRoute();
                saveResults();
            } else {
                Log.i(TAG_CONTEXT, "Route not saved!");
                Toast.makeText(getApplicationContext(), "Failed to save route!", Toast.LENGTH_SHORT).show();
            }
        }
    }




    /**
     * Update which fragment will be displayed based on the selected tab
     */
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    /**
     * Returns a fragment corresponding to selected section
     */
    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                default:
                case 0:
                    MyMapFragment mMapFragment = new MyMapFragment();
                    // Register Location Passer interface with Map Fragment
                    mLocationPasser = mMapFragment;
                    return mMapFragment;
                case 1:
                    MyStatsFragment mStatsFragment = new MyStatsFragment();
                    // Register Stats Passer interface with Stats Fragment
                    mStatsPasser = mStatsFragment;
                    return mStatsFragment;
            }
        }

        @Override
        public int getCount() {
            return 2; // 2 tabs
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                default:
                    return getString(R.string.title_map).toUpperCase();
                case 1:
                    return getString(R.string.title_statistics).toUpperCase();
            }
        }

    }

    /**
     * Update user
     */
    private class UpdateUser extends AsyncTask<Void, Integer, Integer> {

        int indicator;
        String message;

        // variables for constructor
        int id;
        float distance;
        long time;
        float maxSpeed;
        float averageSpeed;

        // Constructor
        public UpdateUser(int id, float distance, long time, float maxSpeed, float averageSpeed) {
            this.id = id;
            this.distance = distance;
            this.time = time;
            this.maxSpeed = maxSpeed;
            this.averageSpeed = averageSpeed;
        }

        // int indicator;

        @Override
        protected Integer doInBackground(Void... params) {

            List<NameValuePair> args = new ArrayList<>();
            args.add(new BasicNameValuePair("user_id", String.valueOf(id)));
            args.add(new BasicNameValuePair("distance", String.valueOf(distance)));
            args.add(new BasicNameValuePair("time", String.valueOf(time)));
            args.add(new BasicNameValuePair("max_speed", String.valueOf(maxSpeed)));
            args.add(new BasicNameValuePair("average_speed", String.valueOf(averageSpeed)));

            // send data to server
            // get JSON response from server
            JSONObject json = jsonParser.makeHttpRequest(getString(R.string.update_user), HttpMethod.POST, args);
            Log.d(TAG_CONTEXT, "JSON Parser: " + json);

            try {
                // success tag indicates level of success from the server
                indicator = json.getInt("success");
                message = json.getString("message");
            } catch (JSONException e) {
                Log.e(TAG_CONTEXT, "JSONException: " + e.getMessage());
            }
            return indicator;
        }

        protected void onPostExecute(Integer result) {
            Log.d(TAG_CONTEXT, "Success: " + indicator + ", Message: " + message);
            // dismiss Progress Dialog
            mProgressDialog.dismiss();
            // check indicator
            if(result == 1) {
                Log.i(TAG_CONTEXT, "Route saved!");
                // Go to results activity
                // Intent intent = new Intent(getApplicationContext(), ResultsActivity.class);
                // startActivity(intent);
            } else {
                Log.i(TAG_CONTEXT, "User could not be updated");
            }
        }

    }
}