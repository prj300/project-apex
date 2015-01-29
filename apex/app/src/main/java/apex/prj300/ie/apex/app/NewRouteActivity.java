package apex.prj300.ie.apex.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.gson.Gson;

import apex.prj300.ie.apex.app.classes.enums.Grade;
import apex.prj300.ie.apex.app.classes.enums.Terrain;
import apex.prj300.ie.apex.app.classes.methods.JSONParser;
import apex.prj300.ie.apex.app.classes.models.User;

import static android.view.View.*;
import static com.google.android.gms.common.api.GoogleApiClient.*;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;


public class NewRouteActivity extends FragmentActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener{

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
    public static final long UPDATE_INTERVAL__MS = 1000;
    // Fastest rate for location updates
    public static final long FASTEST_UPDATE_INTERVAL_MS = UPDATE_INTERVAL__MS / 2;


    // Google Map
    private GoogleMap mMap;
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
    protected List<Double> routeLats;
    protected List<Double> routeLngs;
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

            }
        });

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
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
                    return new MyMapFragment();
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
                case 0:
                    return getString(R.string.title_map).toUpperCase(l);
                case 1:
                    return getString(R.string.title_statistics).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing map view.
     */
    public static class MyMapFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.fragment_map, container, false);
            // Bundle args = getArguments();
            /*((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    Integer.toString(args.getInt(ARG_OBJECT)));*/
            return rootView;
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
