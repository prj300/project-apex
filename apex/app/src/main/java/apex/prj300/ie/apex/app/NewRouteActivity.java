package apex.prj300.ie.apex.app;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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
import apex.prj300.ie.apex.app.fragments.MyMapFragment;
import apex.prj300.ie.apex.app.fragments.MyStatsFragment;

import static android.view.View.*;
import static com.google.android.gms.common.api.GoogleApiClient.*;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;


public class NewRouteActivity extends FragmentActivity
        implements ActionBar.TabListener {

    private static final String TAG_CONTEXT = "NewRouteActivity";

    // Define an interface to pass a location to MyMapFragment
    public interface PassLocationListener {
        void onPassLocation(Location location);
    }

    protected PassLocationListener mLocationPasser;
    protected Boolean mRequestLocationUpdates;

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

        mButtonRecord = (ImageView) findViewById(R.id.btnRecord);
        mButtonStop = (ImageView) findViewById(R.id.btnStop);

        mRequestLocationUpdates = false;

        mButtonRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_CONTEXT, "Start Recording clicked.");
                mRequestLocationUpdates = true;
            }
        });

        mButtonStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_CONTEXT, "Stop Recording clicked.");
                if(mRequestLocationUpdates) {
                    mRequestLocationUpdates = false;
                }

            }
        });

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
                    mLocationPasser = mMapFragment; // Register Location Passer interface with Map Fragment
                    return mMapFragment;
                case 1:
                    return new MyStatsFragment();
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
}