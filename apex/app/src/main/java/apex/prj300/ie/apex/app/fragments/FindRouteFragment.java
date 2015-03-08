package apex.prj300.ie.apex.app.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import apex.prj300.ie.apex.app.DiscoveryPointsActivity;
import apex.prj300.ie.apex.app.MainActivity;
import apex.prj300.ie.apex.app.NewRouteActivity;
import apex.prj300.ie.apex.app.R;
import apex.prj300.ie.apex.app.classes.db.WildAtlanticWayDB;
import apex.prj300.ie.apex.app.classes.enums.HttpMethod;
import apex.prj300.ie.apex.app.classes.enums.Terrain;
import apex.prj300.ie.apex.app.classes.methods.JSONParser;
import apex.prj300.ie.apex.app.classes.models.WayPoint;

import static com.google.android.gms.common.api.GoogleApiClient.*;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;

public class FindRouteFragment extends Fragment
        implements ConnectionCallbacks, OnConnectionFailedListener {

    JSONParser jsonParser = new JSONParser();
    private ProgressDialog mProgressDialog;
    private static Location mLastLocation;
    private static double mLatitude;
    private static double mLongitude;
    private GoogleApiClient mGoogleApiClient;

    private static final String TAG_CONTEXT = "FindRouteFragment";
    // counties on the Wild Atlantic Way Route
    private static String[] counties = new String[] { "Donegal", "Sligo", "Mayo", "Galway", "Clare", "Limerick", "Kerry", "Cork"};

    public FindRouteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        mGoogleApiClient.connect();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // get rootView
        View rootView = inflater.inflate(R.layout.fragment_find_route, container, false);
        ListView mListView = (ListView) rootView.findViewById(R.id.list_find_route);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, getResources()
                .getStringArray(R.array.route_options_array));

        // Display options in a list view
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG_CONTEXT, "Item selected: " + position);
                // Take actions based on selected list item
                switch (position) {
                    case 0:
                        if(isConnected()) {
                            if(locationEnabled()) {
                                findRouteByDistance();
                            } else {
                                Toast.makeText(getActivity(), "Turn on location.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "No network connection!",
                                    Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 1:
                        if(isConnected()) {
                            if(locationEnabled()) {
                                findRouteDiscoveryPoint();
                            } else {
                                Toast.makeText(getActivity(), "Turn on location.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "No network connection!",
                                    Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            }
        });
        return rootView;

    }

    /**
     * Dialog prompts user to filter by county
     */
    private void findRouteDiscoveryPoint() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_county);

        // Instantiate a NumberPicker
        // can be used with String also
        final NumberPicker np = new NumberPicker(getActivity());

        np.setMinValue(0);
        np.setMaxValue(counties.length-1);
        np.setWrapSelectorWheel(false);
        np.setDisplayedValues(counties);
        np.setValue(0);
        builder.setView(np);

        builder.setPositiveButton(R.string.title_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String county = (String) Array.get(counties, np.getValue());
                Log.d(TAG_CONTEXT, "County selected: " + county);
                if(isConnected()) {
                    new FindDiscoveryPoints("discovery", county).execute();
                } else {
                    Toast.makeText(getActivity(), "No network connection.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton(R.string.title_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG_CONTEXT, "Cancelled");
            }
        });
        builder.show();
    }


    /**
     * Check network connection before
     * attempting a network request
     */
    private boolean isConnected() {
        // Create a connectivity manager and
        // get the service type currently in use (Wi-fi, 3g etc)
        ConnectivityManager cm = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        // get network info from the defined service
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        // return a true or false statement based on connection status
        return (networkInfo != null)
                && networkInfo.isConnected()
                && networkInfo.isAvailable();
    }

    /**
     * Find a route by desired distance/length
     */
    private void findRouteByDistance() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.enter_distance_title);

        final NumberPicker np = new NumberPicker(getActivity());
        // populate an array with 100 numbers
        String[] nums = new String[200];
        for(int i=0;i<nums.length;i++)
            nums[i] = Integer.toString(i);
        np.setMinValue(0);
        np.setMaxValue(nums.length-1);
        np.setWrapSelectorWheel(false);
        np.setDisplayedValues(nums);
        np.setValue(100);
        builder.setView(np);

        builder.setPositiveButton(R.string.title_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG_CONTEXT, "Distance selected: " + np.getValue());
                new FindRouteByDistance((float) np.getValue(),
                        mLastLocation.getLatitude(),
                        mLastLocation.getLongitude(), "distance")
                        .execute();

            }
        });
        builder.setNegativeButton(R.string.title_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG_CONTEXT, "Cancelled");
            }
        });
        builder.show();

    }

    /**
     * Entry point to Google Play Service
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean locationEnabled() {
        LocationManager lm = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Get the user's last known location
     * Accuracy not required
     */
    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitude = mLastLocation.getLatitude();
            mLongitude = mLastLocation.getLongitude();
            Log.d(TAG_CONTEXT, "Lat: " + mLatitude +  " Lon: " + mLongitude);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    /**
     * Async Task to find route of a defined length
     */
    private class FindRouteByDistance extends AsyncTask<Void, Integer, JSONObject>{
        float picked;
        double latitude;
        double longitude;
        String tag;

        public FindRouteByDistance(float picked, double lat, double lon, String tag) {
            this.picked = picked;
            this.latitude = lat;
            this.longitude = lon;
            this.tag = tag;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show Progress Dialog before executing
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Finding a route..");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            List<NameValuePair> args = new ArrayList<>();
            args.add(new BasicNameValuePair("tag", tag));
            args.add(new BasicNameValuePair("distance", String.valueOf(picked)));
            args.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));
            args.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));

            // make HTTP request and get response
            return jsonParser.makeHttpRequest(getString(
                    R.string.route_controller), HttpMethod.POST, args);
        }

        protected void onPostExecute(JSONObject json) {
            mProgressDialog.dismiss();
            Log.d(TAG_CONTEXT, "JSON: " + json);
            try {
                Toast.makeText(getActivity(), json.getString("message"), Toast.LENGTH_SHORT).show();
                // if route was retrieved
                if(json.getBoolean("success")) {
                    getRouteFromJson(json.getJSONObject("route"));
                    getDiscoveryPointsFromJson(json.getJSONArray("discovery_points"));
                    // open new activity
                    startActivity(new Intent(getActivity(), NewRouteActivity.class));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Extract discovery points data
     */
    private void getDiscoveryPointsFromJson(JSONArray json) throws JSONException {
        ArrayList<WayPoint> discoveryPoints = new ArrayList<>();

        Log.d(TAG_CONTEXT, "Discovery Points: " + json.length());

        for(int i=0;i<json.length();i++) {
            int id = json.getJSONObject(i).getInt("id");
            String name = json.getJSONObject(i).getString("name");
            int locationId = json.getJSONObject(i).getInt("location_id");
            String county = json.getJSONObject(i).getString("county");
            double lat = json.getJSONObject(i).getDouble("lat");
            double lng = json.getJSONObject(i).getDouble("lng");

            discoveryPoints.add(new WayPoint(id, lat, lng, locationId, county, name));
        }
        saveDiscoveryPoints(discoveryPoints);

    }

    /**
     * Extract route data
     */
    private void getRouteFromJson(JSONObject json) {
        ArrayList<WayPoint> wayPoints = new ArrayList<>();
        try {
            // get json array from JSON route object
            JSONArray route = json.getJSONArray("route");
            // loop through list and add the lat longs into a way point array
            for(int i=0; i < route.length();i++) {
                int id = route.getJSONObject(i).getInt("id");
                double lat = route.getJSONObject(i).getDouble("lat");
                double lng = route.getJSONObject(i).getDouble("lng");
                wayPoints.add(new WayPoint(id, lat, lng));
            }
            saveRoute(wayPoints); // store way points in SQLite database
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Save route
     */
    private void saveRoute(ArrayList<WayPoint> wayPoints) {
        WildAtlanticWayDB db = new WildAtlanticWayDB(getActivity());
        db.resetTables(); // clear any previous data in tables
        db.addWaypoints(wayPoints);
        Log.i(TAG_CONTEXT, "Route saved");

    }

    /**
     * Returns list of way points for points of interest on WAW
     */
    private class FindDiscoveryPoints extends AsyncTask<Void, Void, JSONObject> {
        String discovery; // tag
        String county; // chosen county

        public FindDiscoveryPoints(String discovery, String county) {
            this.discovery = discovery;
            this.county = county;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show Progress Dialog before executing
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Finding discovery points..");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }
        @Override
        protected JSONObject doInBackground(Void... params) {

            // name value pairs that are sent to server
            ArrayList<NameValuePair> args = new ArrayList<>();
            args.add(new BasicNameValuePair("tag", discovery));
            args.add(new BasicNameValuePair("county", county));

            return jsonParser.makeHttpRequest(getString(R.string.route_controller), HttpMethod.POST, args);
        }

        /**
         * Take action from results of server request
         */
        protected void onPostExecute(JSONObject json) {
            mProgressDialog.dismiss();
            Log.i(TAG_CONTEXT, "Response: " + json);

            try {
                if(json.getBoolean("success")) {
                    getDiscoveryPointsFromJson(json.getJSONArray("discovery_points"));
                    startActivity(new Intent(getActivity(), DiscoveryPointsActivity.class));
                } else {
                    Toast.makeText(getActivity(), json.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Extract JSON data to SQLite database
     */

    private void saveDiscoveryPoints(ArrayList<WayPoint> discoveryPoints) {
        WildAtlanticWayDB db = new WildAtlanticWayDB(getActivity());
        db.addDiscoveryPoints(discoveryPoints);
    }
}
