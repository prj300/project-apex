package apex.prj300.ie.apex.app.fragments;

import android.app.Fragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import apex.prj300.ie.apex.app.R;
import apex.prj300.ie.apex.app.classes.db.RouteDB;
import apex.prj300.ie.apex.app.classes.db.UserDB;
import apex.prj300.ie.apex.app.classes.enums.Grade;
import apex.prj300.ie.apex.app.classes.enums.HttpMethod;
import apex.prj300.ie.apex.app.classes.enums.Terrain;
import apex.prj300.ie.apex.app.classes.methods.JSONParser;
import apex.prj300.ie.apex.app.classes.models.Route;
import apex.prj300.ie.apex.app.classes.models.User;

public class MyRoutesFragment extends Fragment {

    // Instantiate a new JSONParser class to handel incoming data
    JSONParser jsonParser = new JSONParser();

    private ProgressDialog mProgressDialog;
    private Boolean isConnected;

    private static ListView mListView;


    /**
     * Static variables
     */
    // App section
    private static final String TAG_CONTEXT = "MyRoutesFragment";

    public MyRoutesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Only execute a request to the server if
        // there are less than two rows in the SQLite database
        // This is to distinguish any unnecessary requests, e.g.
        // if a user created a route it will be stored locally
        // but may never have requested their routes from the server before
        /*
        if(numberOfRoutes() > 1) {
            displayRoutes();
        } else {
            clearTables();
            new GetMyRoutes().execute();
        }
        */
        clearTables();
        new GetMyRoutes().execute();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_my_routes, container, false);
        mListView = (ListView) rootView.findViewById(R.id.list_my_routes);

        return rootView;

    }


    /**
     * Method opens connection to UserDB
     * Returns user that is currently logged in
     */
    public User getUser() {
        // Instantiate a user and get the user that is currently logged in
        User user;
        UserDB db = new UserDB(getActivity());
        user = db.getUser();
        return user;
    }

    /**
     * Clear all previous data in table
     * This is to prevent a conflict of
     * unique primary keys in the tables
     * The new data will replace the old data
     */
    private void clearTables() {
        RouteDB db = new RouteDB(getActivity());
        db.resetTables();
    }

    /**
     * Get number of rows in SQLite RouteDB
     */

    private int numberOfRoutes() {
        RouteDB db = new RouteDB(getActivity());
        // call a row count query from SQLite database
        return db.rowCount(getUser().getId());
    }


    /**
     * Get list of routes from database
     */
    private ArrayList<Route> getRoutes() {
        RouteDB db = new RouteDB(getActivity());
        return db.getRoutes();
    }



    /**
     * AsyncTask executes in background to get routes from server
     */
    private class GetMyRoutes extends AsyncTask<Void, Integer, Integer> {

        // JSON Object holds JSON response from server
        JSONObject json;
        int indicator;
        String TAG_SUCCESS = "success";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show Progress Dialog before executing
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Downloading route information..");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // Attempt server request
            try {
                List<NameValuePair> args = new ArrayList<>();
                // We only need the user ID to get their routes
                args.add(new BasicNameValuePair("user_id", String.valueOf(getUser().getId())));

                // put response into a JSONObject
                json = jsonParser.makeHttpRequest(getString(R.string.get_my_routes), HttpMethod.POST, args);

                Log.d(TAG_CONTEXT, "Response: " + json.toString());

                if(json != null) {
                    indicator = json.getInt(TAG_SUCCESS);
                } else {
                    indicator = -10;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return indicator;
        }


        /**
         * Once the server request is done take action based on response
         */
        protected void onPostExecute(Integer result) {
            Log.d(TAG_CONTEXT, "Result: " + result);
            // dismiss Progress Dialog
            mProgressDialog.dismiss();

            if(result == 1) {
                // routes available
                // save data to SQLite db
                saveRoutes(json);
            } else {
                Toast.makeText(getActivity(), "No routes available", Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * Save routes from JSON response into local SQLite database
     */
    private void saveRoutes(JSONObject json) {
        // Instantiate new RouteDB
        RouteDB db = new RouteDB(getActivity());
        Route route;

        try {
            JSONArray routes = json.getJSONArray("routes");
            Log.d("routes", routes.toString());
            Log.d("number of routes", String.valueOf(routes.length()));

            // loop through list of JSON routes
            // Convert/cast and create a new Route
            for(int i=0;i < routes.length(); i++) {
                JSONObject jsonRoute = routes.getJSONObject(i);
                int routeId = jsonRoute.getInt("route_id");
                Grade grade = Grade.valueOf(jsonRoute.getString("grade"));
                Terrain terrain = Terrain.valueOf(jsonRoute.getString("terrain"));
                float distance = Float.valueOf(jsonRoute.getString("distance"));
                Date dateCreated = Date.valueOf(jsonRoute.getString("date_created"));

                route = new Route(routeId, getUser().getId(), grade, terrain, distance, dateCreated);
                // add route to database
                db.addRoute(route);
            }
            Log.d(TAG_CONTEXT, String.valueOf(routes.length()) + " routes added to SQLiteDB");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // now let's display these routes
        displayRoutes();

    }

    /**
     * Dynamically display "My Routes" in a list on this fragment
     */
    private void displayRoutes() {
        // get routes from database
        ArrayList<Route> mRoutes = getRoutes();

        ArrayAdapter<Route> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, mRoutes);

        mListView.setAdapter(adapter);
    }
}
