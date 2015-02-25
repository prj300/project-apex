package apex.prj300.ie.apex.app.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
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

import java.util.ArrayList;
import java.util.List;

import apex.prj300.ie.apex.app.R;
import apex.prj300.ie.apex.app.StartRouteActivity;
import apex.prj300.ie.apex.app.classes.db.RouteDB;
import apex.prj300.ie.apex.app.classes.enums.HttpMethod;
import apex.prj300.ie.apex.app.classes.methods.JSONParser;

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
                            findRouteByDistance();
                        } else {
                            Toast.makeText(getActivity(), "No network connection!",
                                    Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        });
        return rootView;

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
        String[] nums = new String[100];
        for(int i=0;i<nums.length;i++)
            nums[i] = Integer.toString(i);
        np.setMinValue(0);
        np.setMaxValue(nums.length);
        np.setWrapSelectorWheel(false);
        np.setDisplayedValues(nums);
        np.setValue(25);
        builder.setView(np);

        builder.setPositiveButton(R.string.title_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG_CONTEXT, "Distance selected: " + np.getValue());
                new FindRouteByDistance((float) np.getValue(),
                        mLastLocation.getLatitude(),
                        mLastLocation.getLongitude())
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

        public FindRouteByDistance(float picked, double lat, double lon) {
            this.picked = picked;
            this.latitude = lat;
            this.longitude = lon;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show Progress Dialog before executing
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Finding a route..");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setProgress(0);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            List<NameValuePair> args = new ArrayList<>();
            args.add(new BasicNameValuePair("tag", "dist"));
            args.add(new BasicNameValuePair("distance", String.valueOf(picked)));
            args.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));
            args.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));

            // make HTTP request and get response
            return jsonParser.makeHttpRequest(getString(
                    R.string.index), HttpMethod.POST, args);
        }

        protected void onProgressUpdate(Integer... progress) {
            mProgressDialog.incrementProgressBy(1);
        }


        protected void onPostExecute(JSONObject json) {
            mProgressDialog.dismiss();
            if(json != null) {
                Log.d(TAG_CONTEXT, "Response: " + json);
                try {
                    JSONObject route = json.getJSONObject("route");
                    saveRoute(route);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(getActivity(), "Could not connect to server.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Save routes
     */
    private void saveRoute(JSONObject route) {
        RouteDB db = new RouteDB(getActivity());

        try {
            JSONArray jsonLats = route.getJSONArray("lats");
            JSONArray jsonLongs = route.getJSONArray("longs");
            Log.d("Length: ", String.valueOf(route.length() ));

            Log.d("distance", String.valueOf(route.getString("distance")));
            ArrayList<Double> lats = new ArrayList<>();
            ArrayList<Double> longs = new ArrayList<>();

            // Only attempt to save routes if there are any
            if(jsonLats.length() > 0 && (jsonLongs.length() > 0)) {
                // clear previous routes
                db.resetTables();
                // loop through list of JSON routes
                // Convert/cast and create a new Route from these parameters
                for(int i=0; (i < jsonLats.length()) && i < jsonLongs.length();i++) {
                    double lat = jsonLats.getDouble(i);
                    double lon = jsonLongs.getDouble(i);
                    lats.add(lat);
                    longs.add(lon);
                }
                db.addLatsLong(1, lats, longs);
                startActivity(new Intent(getActivity(), StartRouteActivity.class));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
