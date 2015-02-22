package apex.prj300.ie.apex.app.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
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


import com.google.android.gms.location.LocationListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import apex.prj300.ie.apex.app.R;
import apex.prj300.ie.apex.app.StartRouteActivity;
import apex.prj300.ie.apex.app.classes.db.RouteDB;
import apex.prj300.ie.apex.app.classes.enums.Grade;
import apex.prj300.ie.apex.app.classes.enums.HttpMethod;
import apex.prj300.ie.apex.app.classes.enums.Terrain;
import apex.prj300.ie.apex.app.classes.methods.JSONParser;
import apex.prj300.ie.apex.app.classes.models.Route;

public class FindRouteFragment extends Fragment implements LocationListener {

    JSONParser jsonParser = new JSONParser();
    private ProgressDialog mProgressDialog;
    private static Location mLocation;

    private static final String TAG_CONTEXT = "FindRouteFragment";

    public FindRouteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                        findRouteByDistance();
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
                        mLocation.getLatitude(),
                        mLocation.getLongitude())
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

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
    }

    /**
     * Async Task to find route of a defined length
     */
    private class FindRouteByDistance extends AsyncTask<Void, Void, JSONObject>{
        float picked;
        double latitude;
        double longitude

        public FindRouteByDistance(float picked, double lat, double lon) {
            this.picked = picked;
            this.lat = latitude;
            this.lon = longitude;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show Progress Dialog before executing
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Downloading results..");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            List<NameValuePair> args = new ArrayList<>();
            args.add(new BasicNameValuePair("distance", String.valueOf(picked)));

            // make HTTP request and get response
            return jsonParser.makeHttpRequest(getString(
                    R.string.find_route_by_distance), HttpMethod.POST, args);
        }

        protected void onPostExecute(JSONObject json) {
            mProgressDialog.dismiss();
            try {
                // No routes available
                if(json.getInt("success") == 0) {
                    Toast.makeText(getActivity(), "No routes available!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Save routes to SQLite database
                    saveRoutes(json);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save routes
     */
    private void saveRoutes(JSONObject json) {
        RouteDB db = new RouteDB(getActivity());

        try {
            JSONArray routes = json.getJSONArray("routes");
            Log.d("routes", routes.toString());
            Log.d("number of routes", String.valueOf(routes.length()));

            // Only attempt to save routes if there are any
            if(routes.length() > 0) {
                // clear previous routes
                db.resetTables();
                // loop through list of JSON routes
                // Convert/cast and create a new Route from these parameters
                for(int i=0; i < routes.length();i++) {
                    JSONObject jsonResults = routes.getJSONObject(i);
                    int routeId = jsonResults.getInt("route_id");
                    int userId = jsonResults.getInt("user_id");
                    Grade grade = Grade.valueOf(jsonResults.getString("grade"));
                    Terrain terrain = Terrain.valueOf(jsonResults.getString("terrain"));
                    float distance = Float.valueOf(jsonResults.getString("distance"));
                    Date dateCreated = Date.valueOf(jsonResults.getString("date_created"));

                    Route route = new Route(routeId, userId, grade, terrain, distance, dateCreated);
                    db.addRoute(route);
                }
                Log.d(TAG_CONTEXT, db.getRoutes().size() + " routes saved.");
                ChooseRouteFragment fragment = new ChooseRouteFragment();
                getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
