package apex.prj300.ie.apex.app.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import apex.prj300.ie.apex.app.R;
import apex.prj300.ie.apex.app.classes.db.ResultsDB;
import apex.prj300.ie.apex.app.classes.db.RouteDB;
import apex.prj300.ie.apex.app.classes.db.UserDB;
import apex.prj300.ie.apex.app.classes.enums.Grade;
import apex.prj300.ie.apex.app.classes.enums.HttpMethod;
import apex.prj300.ie.apex.app.classes.enums.Terrain;
import apex.prj300.ie.apex.app.classes.methods.JSONParser;
import apex.prj300.ie.apex.app.classes.models.Result;
import apex.prj300.ie.apex.app.classes.models.Route;
import apex.prj300.ie.apex.app.classes.models.User;

public class MyResultsFragment extends Fragment {

    // Instantiate a new JSONParser class to handel incoming data
    JSONParser jsonParser = new JSONParser();

    private ProgressDialog mProgressDialog;

    private static ListView mListView;

    android.app.FragmentManager fragmentManager;

    /**
     * Static variables
     */
    // App section
    private static final String TAG_CONTEXT = "MyResultsFragment";

    public MyResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getFragmentManager();

        // if there are results in the database to retrieve
        if(getResults().size() > 0) {
            displayResults(getResults());
        } else {
            if(isConnected()) {
                new GetMyResults(getUser().getId()).execute();
            } else {
                Toast.makeText(getActivity(), "No network connection!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Get the list of results from the local db
     */
    private ArrayList<Result> getResults() {
        ResultsDB db = new ResultsDB(getActivity());
        return db.getResults(getUser().getId());
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
     * AsyncTask executes in background to get routes from server
     */
    private class GetMyResults extends AsyncTask<Void, Integer, JSONObject> {

        int userId;

        public GetMyResults(int userId) {
            this.userId = userId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show Progress Dialog before executing
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Finding suitable routes..");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject json = null;
            // Attempt server request
            try {
                List<NameValuePair> args = new ArrayList<>();
                // We only need the user ID to get their routes
                args.add(new BasicNameValuePair("user_id", String.valueOf(userId)));

                // put response into a JSONObject
                json = jsonParser.makeHttpRequest(getString(R.string.get_results), HttpMethod.POST, args);

                Log.d(TAG_CONTEXT, "Response: " + json.toString());


            } catch (Exception e) {
                e.printStackTrace();
            }

            return json;
        }


        /**
         * Once the server request is done take action based on response
         */
        protected void onPostExecute(JSONObject json) {
            // dismiss Progress Dialog
            mProgressDialog.dismiss();
            try {
                Log.d(TAG_CONTEXT, "Result: " + json.getString("message"));
                if(json.getInt("success") == 1) {
                    // routes available
                    // save data to SQLite db
                    saveRoutes(json);
                } else {
                    Toast.makeText(getActivity(), "No results available!", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Save routes from JSON response into local SQLite database
     */
    private void saveRoutes(JSONObject json) {
        // Instantiate new RouteDB
        ResultsDB db = new ResultsDB(getActivity());

        try {
            JSONArray results = json.getJSONArray("results");
            Log.d("results", results.toString());
            Log.i("number of results", String.valueOf(results.length()));

            // only execute insert if the array has values
            if(results.length() > 0) {
                // loop through list of JSON results
                // Convert/cast and create a new result from these parameters
                for (int i = 0; i < results.length(); i++) {
                    JSONObject jsonResults = results.getJSONObject(i);
                    int resultId = jsonResults.getInt("result_id");
                    int routeId = jsonResults.getInt("route_id");
                    float distance = Float.valueOf(jsonResults.getString("distance"));
                    float maxSpeed = Float.valueOf(jsonResults.getString("max_speed"));
                    float avgSpeed = Float.valueOf(jsonResults.getString("avg_speed"));
                    long time = jsonResults.getLong("time");
                    Date dateCreated = Date.valueOf(jsonResults.getString("date_created"));

                    Result result = new Result(resultId, getUser().getId(), routeId, distance,
                            maxSpeed, avgSpeed, time, dateCreated);

                    // add result to the database
                    db.addResult(result);
                }
            }
            Log.d(TAG_CONTEXT, String.valueOf(results.length()) + " routes added to SQLiteDB");
        } catch (JSONException e) {
            Log.e(TAG_CONTEXT, "JSONException: " + e.getMessage());
        }

        if(getResults().size() > 0) {
            // display results
            displayResults(getResults());
        } else {
            Toast.makeText(getActivity(), "No results available!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Dynamically display "My Routes" in a list on this fragment
     */
    private void displayResults(final ArrayList<Result> results) {

        Log.d(TAG_CONTEXT, "Displaying " + results.size() + " routes.");

        ArrayAdapter<Result> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, results);

        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG_CONTEXT, "Route selected: " + position);
                Result selectedResult = results.get(position);
                // Start a fragment with
                // MyResultFragment myResultFragment = new MyResultFragment();
                // getFragmentManager().beginTransaction().replace(R.id.container, myResultFragment).commit();
            }
        });
    }
}
