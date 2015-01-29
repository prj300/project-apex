package apex.prj300.ie.apex.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import apex.prj300.ie.apex.app.classes.enums.Grade;
import apex.prj300.ie.apex.app.classes.enums.Terrain;
import apex.prj300.ie.apex.app.classes.methods.JSONParser;
import apex.prj300.ie.apex.app.classes.models.User;

import static android.view.View.*;
import static com.google.android.gms.common.api.GoogleApiClient.*;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;


public class NewRouteActivity extends FragmentActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    public static FragmentManager fragmentManager;

    /**
     * JSONParser that will parse data to send to server
     */
    JSONParser jsonParser = new JSONParser();
    Gson gson = new Gson();

    /**
     * Static Variables
     */
    protected static final String TAG_CONTEXT = "NewRouteActivity";
    protected static final String TAG_DIALOG_SAVE_ROUTE = "Do you wish to save this route?";
    protected static final String TAG_DIALOG_YES = "OK";
    protected static final String TAG_DIALOG_NO = "No thanks";

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
    protected double mLatitude; // current latitude
    protected double mLongitude; // current longitude
    // Tracks status of location updates request
    protected Boolean mRequestingLocationUpdates;
    // Tracks whether route should be recorded or not
    protected Boolean mRecordRoute;

    // Stores user's geographical points
    protected List<LatLng> mLatLngs = new ArrayList<>();
    // Displays user's geographical points
    protected Polyline line;

    // Tracks distance covered
    protected float mDistance;
    protected float mTotalDistance;

    // Tracks time elapsed
    protected Long mStartTime; // (milliseconds)
    protected Long mEndTime;
    protected Long mTimeDifference;

    // Calendar to track date created
    protected static Calendar mCalendar = Calendar.getInstance();

    /**
     * User Model properties (as needed)
     */
    protected User user = new User();
    protected int mId;

    /**
     * Route Model Properties
     */
    protected Grade routeGrade;
    protected Terrain routeTerrain;
    protected List<Double> routeLats = new ArrayList<>();
    protected List<Double> routeLngs = new ArrayList<>();
    protected float routeDistance;
    protected Date dateCreated;

    /**
     * Results Model Properties
     */
    protected int routeId;
    protected int mExperience;
    protected float mMaxSpeed;
    // protected float routeDistance;
    protected float avgSpeed;
    protected Date mTime;
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
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        fragmentManager = getSupportFragmentManager();

        // Locate UI Widgets
        mButtonRecord = (ImageView) findViewById(R.id.btnRecord);
        mButtonStop = (ImageView) findViewById(R.id.btnStop);

        // Set Location Updates status to false
        mRequestingLocationUpdates = false;
        mRecordRoute = false;
        mTotalDistance = 0;

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {

            @Override
            public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

            }
        };

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getActionBar().setSelectedNavigationItem(position);
            }
        });

        // Add 2 tabs
        for(int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar
                .newTab()
                .setText(mSectionsPagerAdapter.getPageTitle(i))
                .setTabListener(tabListener));
        }

        // Create Listeners for Record/Stop buttons
        mButtonRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG_CONTEXT, "Start Recording...");
                mStartTime = System.currentTimeMillis(); // start timer
                startUpdatesButtonHandler();
            }
        });
        mButtonStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG_CONTEXT, "Stop recording.");
                mEndTime = System.currentTimeMillis(); // end timer
                stopUpdatesButtonHandler();
            }
        });

        buildGoogleApiClient();

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
        if(!mRequestingLocationUpdates) {
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

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG_CONTEXT, "Connected to GoogleApiClient");

        if(mLocation == null) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // If user presses Start button before GoogleApiClient connects
        // set mRequestingLocationUpdates to true
        if(mRequestingLocationUpdates) {
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
        // trackSpeed();
        saveLocation();
        updateUI();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        stopLocationUpdates();
        Log.i(TAG_CONTEXT, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }


    /**
     * Track total distance covered
     */
    private void trackDistance() {
        if(!mLatLngs.isEmpty()) {
            Location lastLocation = new Location("LastLocation");
            double x = mLatLngs.get(mLatLngs.size()-1).latitude; // previous latitude point in array
            double y = mLatLngs.get(mLatLngs.size()-1).longitude; // previous longitude point in array

            // set previous location parameters
            lastLocation.setLatitude(x);
            lastLocation.setLongitude(y);

            if(mLocation != lastLocation) {
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
    private void saveLocation() {
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
    private void updateUI() {
        LatLng latLng = new LatLng(mLatitude, mLongitude);
        // plot points on UI
        line = mMap.addPolyline(new PolylineOptions()
                .add(latLng)
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
        }
        */
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    Fragment fragment = new MyMapFragment();
                    Bundle args = new Bundle();
                    fragment.setArguments(args);
                    return fragment;
                case 1:
                    return new StatisticsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                default:
                    return getString(R.string.title_map).toUpperCase(l);
                case 1:
                    return getString(R.string.title_statistics).toUpperCase(l);
            }
        }
    }

    /**
     * A placeholder fragment containing map view.
     */
    public static class MyMapFragment extends Fragment {

        private GoogleMap mMap;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            initMap();
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_map, container, false);
            // Bundle args = getArguments();

            return rootView;
        }

        private GoogleMap initMap() {
            if(mMap == null && getActivity() != null
                    && getActivity().getSupportFragmentManager() != null) {
                SupportMapFragment smf = (SupportMapFragment)getActivity().
                        getSupportFragmentManager().findFragmentById(R.id.map);
                if(smf != null) {
                    mMap = smf.getMap();
                    mMap.setMyLocationEnabled(true);
                }
            }
            return mMap;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }
    }

    public static class StatisticsFragment extends Fragment {
        // public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_statistics, container, false);
            // Bundle args = getArguments();
            /*((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    Integer.toString(args.getInt(ARG_OBJECT)));*/
            return rootView;
        }
    }
}
