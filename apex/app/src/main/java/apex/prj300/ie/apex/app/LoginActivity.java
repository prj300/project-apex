package apex.prj300.ie.apex.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import apex.prj300.ie.apex.app.classes.enums.Grade;
import apex.prj300.ie.apex.app.classes.enums.HttpMethod;
import apex.prj300.ie.apex.app.classes.db.UserDB;
import apex.prj300.ie.apex.app.classes.methods.JSONParser;
import apex.prj300.ie.apex.app.classes.models.User;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends Activity {

    // progress dialog for AsyncTask
    private ProgressDialog mProgressDialog;
    JSONParser jsonParser = new JSONParser();
    private JSONObject json;

    EditText mEmail;
    EditText mPassword;
    Button mRegister;
    Button mLogin;

    private static final String TAG = "LoginActivity";

    // indicates success of JSON response
    private int indicator = 0;

    /**
     * User params
     */
    private User user;
    private int id;
    private String password;
    private static String email;
    private static Grade grade;
    private static float totalDistance;
    private static long totalTime;
    private static float maxSpeed;
    private static float avgSpeed;

    /**
     * JSON Node Responses
     */
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ID = "id";
    private static final String TAG_GRADE = "grade";
    private static final String TAG_EXPERIENCE = "experience";
    private static final String TAG_TOTAL_TIME = "totaltime";
    private static final String TAG_TOTAL_DISTANCE = "totaldistance";
    private static final String TAG_MAX_SPEED = "maxspeed";
    private static final String TAG_AVG_SPEED = "avgspeed";

    // Toast messages
    private static final String TAG_MISSING_FIELDS = "Required field(s) missing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mEmail = (EditText) findViewById(R.id.txtEmail);
        mPassword = (EditText) findViewById(R.id.txtPassword);
        mLogin = (Button) findViewById(R.id.btnLogin);
        mRegister = (Button) findViewById(R.id.btnRegister);

        // register button listeners
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEmail.getText().toString().equals("")
                        || mPassword.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Missing field(s)", Toast.LENGTH_SHORT).show();
                } else {
                    isNetworkAvailable();
                    if(!isNetworkAvailable()) {
                         Toast.makeText(getApplicationContext(), "No Network Connection", Toast.LENGTH_LONG).show();
                    } else {
                        new LoginUser().execute();
                    }
                }
            }
        });
        // onclick listener to begin register task
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEmail.getText().toString().equals("")
                        || mPassword.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Missing field(s)", Toast.LENGTH_SHORT).show();
                } else {
                    isNetworkAvailable();
                    if(!isNetworkAvailable()) {
                        Toast.makeText(getApplicationContext(), "No Network Connection", Toast.LENGTH_LONG).show();
                    } else {
                        new RegisterUser().execute();
                    }
                }
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    /**
     * Background task for logging in
     */
    private class LoginUser extends AsyncTask<User, Void, Integer> {

        // before starting task show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(LoginActivity.this);
            mProgressDialog.setMessage("Logging in...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected Integer doInBackground(User...args) {

            email = mEmail.getText().toString();
            password = mPassword.getText().toString();

            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("email", email));
                params.add(new BasicNameValuePair("password", password));

                // get JSON Object
                json = jsonParser.makeHttpRequest(getString(R.string.login_url), HttpMethod.POST, params);

                Log.d(TAG, "Response: " + json.toString());

                indicator = json.getInt(TAG_SUCCESS);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return indicator;
        }

        // after completing dismiss Progress Dialog
        protected void onPostExecute(Integer result) {
            Log.d(TAG, "Success: " + result);
            // dismiss progress dialog
            mProgressDialog.dismiss();
            // Login successful
            if(result == 1) {
                try {
                    // retrieve values from json response
                    GetJSONNodes(json);
                    Toast.makeText(getApplicationContext(), "Logged in as " + email, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Incorrect email/password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Login User
     */
    private void Login() {
        // Instantiate and build a new user
        user = new User(id, email, grade,
                totalDistance, totalTime,
                maxSpeed, avgSpeed);

        // instantiate User Database to store User's details locally
        UserDB db = new UserDB(this);

        // clear any previous data that may be in the database
        db.resetTables();
        db.addUser(user);

        // Move to home page
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        Log.i(TAG, "Logged in as " +  email);
        finish();

        db.close();

    }

    /**
     * Retrieve JSON Response nodes of user
     */
    private void GetJSONNodes(JSONObject json) throws JSONException {
        // registration successful
        id = json.getInt(TAG_ID);
        grade = Grade.valueOf(json.getString(TAG_GRADE));
        totalDistance = Float.valueOf(json.getString(TAG_TOTAL_DISTANCE));
        totalTime = Long.valueOf(json.getString(TAG_TOTAL_TIME));
        maxSpeed = Float.valueOf(json.getString(TAG_MAX_SPEED));
        avgSpeed = Float.valueOf(json.getString(TAG_AVG_SPEED));

        // Login User
        Login();

    }

    /**
     * Background task for registering
     */
    private class RegisterUser extends AsyncTask<User, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show Progress Dialog before executing
            mProgressDialog = new ProgressDialog(LoginActivity.this);
            mProgressDialog.setMessage("Creating new user...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected Integer doInBackground(User... args) {

            email = mEmail.getText().toString();
            password = mPassword.getText().toString();

            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("email", email));
                params.add(new BasicNameValuePair("password", password));

                // get JSON Object
                json = jsonParser.makeHttpRequest(getString(R.string.register_url), HttpMethod.POST, params);

                Log.d("Response: ", json.toString());

                // response indicator from JSON
                indicator = json.getInt(TAG_SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return indicator;
        }

        // after completing dismiss Progress Dialog
        protected void onPostExecute(Integer result) {
            Log.d("Success: ", result.toString());
            // dismiss progress dialog
            mProgressDialog.dismiss();
            if (result == 1) {
                try {
                    GetJSONNodes(json);
                    Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_LONG).show();
                    Log.i("Registered as ", email);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (result == -1) {
                Toast.makeText(getApplicationContext(), "A user with email "
                        + email + " already exists.", Toast.LENGTH_LONG).show();
            } else if (result == -3) {
                Toast.makeText(getApplicationContext(),
                        "Invalid e-mail format", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
            }
        }

    }

    /*
    // toast alerts
    private void popToast(String message, String length) {

        if(length.equals("short")) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }
    */
}
