package apex.prj300.ie.apex.app.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import apex.prj300.ie.apex.app.R;
import apex.prj300.ie.apex.app.classes.db.UserDB;
import apex.prj300.ie.apex.app.classes.methods.JSONParser;
import apex.prj300.ie.apex.app.classes.models.User;

public class MyRoutesFragment extends ListFragment {

    // Instantiate a new JSONParser class to handel incoming data
    JSONParser jsonParser = new JSONParser();
    Gson gson = new Gson();

    private ProgressDialog mProgressDialog;
    private Boolean isConnected;

    private static int userId;

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

        // Instantiate a user and get the user that is currently logged in
        User mUser;
        UserDB db = new UserDB(getActivity());

        mUser = db.getUser();
        userId = mUser.getId();
        // Now we will send a request to the server to get all routes with the user's ID

        new GetMyRoutes().execute();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_routes, container, false);
    }

    private class GetMyRoutes extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... params) {

            List<NameValuePair> args = new ArrayList<>();
            args.add(new BasicNameValuePair("user_id", String.valueOf(userId)));

            //JSONObject json = jsonParser.makeHttpRequest();

            return null;
        }

    }
}
