package apex.prj300.ie.apex.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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
import android.widget.TextView;
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

    EditText mEmail;
    EditText mPassword;
    Button mRegister;
    Button mLogin;
    TextView mPasswordReset;

    String email;
    String password;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        mPasswordReset = (TextView) findViewById(R.id.textForgotPassword);
        // underline password reset link for emphasis
        mPasswordReset.setPaintFlags(mPasswordReset.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
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

        mPasswordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
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
    private class LoginUser extends AsyncTask<User, Void, JSONObject> {

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
        protected JSONObject doInBackground(User...args) {
            email = mEmail.getText().toString();
            password = mPassword.getText().toString();

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("tag", "login"));
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", password));

            // get JSON Object
            return jsonParser.makeHttpRequest(getString(R.string.user_controller), HttpMethod.POST, params);
        }

        // after completing dismiss Progress Dialog
        protected void onPostExecute(JSONObject json) {
            // dismiss progress dialog
            Log.d(TAG, "Response: " + json);
            mProgressDialog.dismiss();

            if(json != null)
            try {
                if(json.getBoolean("success")) {
                    // login successful
                    Log.d("Login", "Login successful");
                    Toast.makeText(getApplicationContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                    // now get user from json
                    getUserFromJson(json.getJSONObject("user"));
                } else {
                    // Login unsuccessful
                    Log.d("Login", "Login unsuccessful");
                    Toast.makeText(getApplicationContext(), json.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSONException: " + e.getMessage());
            } else {
                Toast.makeText(getApplicationContext(), "Server error.", Toast.LENGTH_LONG).show();
            }

        }

    }

    /**
     * Extract JSON to User model
     */
    private void getUserFromJson(JSONObject user) throws JSONException {
        Log.d("User", "User" + user);

        // get JSON values
        int userId = user.getInt("id");
        String email = user.getString("email");
        Grade grade = Grade.valueOf(user.getString("grade"));
        float distance = Float.valueOf(user.getString("total_distance"));
        long time = user.getLong("total_time");
        float maxSpeed = Float.valueOf(user.getString("max_speed"));
        float avgSpeed = Float.valueOf(user.getString("avg_speed"));

        // create user
        User mUser = new User(userId, email, grade, distance, time, maxSpeed, avgSpeed);
        // store user in SQLite database
        Login(mUser);
    }

    /**
     * Save user to SQLite database
     * Logged in
     */
    private void Login(User user) {
        // instantiate User Database to store User's details locally
        UserDB db = new UserDB(this);
        db.resetTables(); // clear previous table data
        db.addUser(user);
        Log.i(TAG, "Logged in as " + email);

        // Move to main activity
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


    /**
     * Background task for registering
     */
    protected class RegisterUser extends AsyncTask<User, Void, JSONObject> {

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
        protected JSONObject doInBackground(User... args) {

            email = mEmail.getText().toString();
            password = mPassword.getText().toString();

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("tag", "register"));
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("password", password));

            // get JSON Object
            return jsonParser.makeHttpRequest(getString(R.string.user_controller), HttpMethod.POST, params);
        }

        /**
         * Post server request logic takes place here
         */
        protected void onPostExecute(JSONObject json) {
            // dismiss progress dialog
            Log.d(TAG, "Response: " + json);
            mProgressDialog.dismiss();

            if(json != null) {
                try {
                    if (json.getBoolean("success")) {
                        // login successful
                        Log.d("Login", "Login successful");
                        Toast.makeText(getApplicationContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                        // now get user from json
                        getUserFromJson(json.getJSONObject("user"));
                    } else {
                        // Login unsuccessful
                        Log.d("Login", "Login unsuccessful");
                        Toast.makeText(getApplicationContext(), json.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException: " + e.getMessage());
                }
            } else {
                Toast.makeText(getApplicationContext(), "Server error.", Toast.LENGTH_LONG).show();
            }

        }

    }

}
