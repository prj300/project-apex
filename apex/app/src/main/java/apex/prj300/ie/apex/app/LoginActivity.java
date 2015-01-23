package apex.prj300.ie.apex.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import java.util.Date;
import java.util.List;
import java.sql.Time;


public class LoginActivity extends Activity {

    // progress dialog for AsyncTask
    private ProgressDialog mProgressDialog;
    JSONParser jsonParser = new JSONParser();

    EditText mEmail;
    EditText mPassword;
    Button mRegister;
    Button mLogin;

    // login url
    private static final String LOGIN_URL = "http://192.168.1.8/android/apexdb/login_user.php";
    //register url
    private static final String REGISTER_URL = "http://192.168.1.8/android/apexdb/create_user.php";

    /**
     * User params
     */
    private int id;
    private String password;
    private String email;
    private Grade grade;
    private int experience;
    private float totalDistance;
    private Time totalTime;
    private int totalCalories;
    private float maxSpeed;
    private float avgSpeed;

    /**
     * JSON Node Responses
     */
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ID = "id";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_GRADE = "grade";
    private static final String TAG_EXPERIENCE = "experience";
    private static final String TAG_TOTAL_TIME = "totaltime";
    private static final String TAG_TOTAL_DISTANCE = "totaldistance";
    private static final String TAG_TOTAL_CALORIES = "totalcalories";
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
                    Log.d("Input: ", TAG_MISSING_FIELDS);
                    popToast(TAG_MISSING_FIELDS, "short");
                } else {
                    new LoginUser().execute();
                }
            }
        });
        // onclick listener to begin register task
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEmail.getText().toString().equals("")
                        || mPassword.getText().toString().equals("")) {
                    Log.d("Input: ", TAG_MISSING_FIELDS);
                    popToast(TAG_MISSING_FIELDS, "short");
                } else {
                    new RegisterUser().execute();
                }
            }
        });
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
    private class LoginUser extends AsyncTask<String, Void, Integer> {


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
        protected Integer doInBackground(String... args) {
            int indicator = 0;
            email = mEmail.getText().toString();
            password = mPassword.getText().toString();

            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("email", email));
                params.add(new BasicNameValuePair("password", password));

                // get JSON Object
                JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, HttpMethod.POST, params);

                Log.d("Response: ", json.toString());

                indicator = json.getInt(TAG_SUCCESS);
                // check for success tag
                try {
                    if (indicator == 1) {
                        GetJSONNodes(json);
                        Login();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return indicator;
        }

        // after completing dismiss Progress Dialog
        protected void onPostExecute(Integer result) {
            Log.d("Success:", result.toString());
            // dismiss progress dialog
            mProgressDialog.dismiss();
            if(result == 1) {
                popToast("Logged in", "short");
            } else if(result == -1) {
                popToast("Username/password incorrect", "short");
            } else {
                popToast("Login failed", "short");
            }

        }

    }

    /**
     * Login User
     */
    private void Login() {
        // Instantiate and build a new user
        User user = new User(id, email, grade,
                experience, totalDistance,
                totalTime, totalCalories,
                maxSpeed, avgSpeed);

        // instantiate User Database to store User's details locally
        UserDB db = new UserDB(this);

        // clear any previous data that may be in the database
        db.resetTables();
        db.addUser(user);

        // Move to home page
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);

        finish();

    }

    /**
     * Retrieve JSON Response nodes of user
     */
    private void GetJSONNodes(JSONObject json) throws JSONException {
        // registration successful
        id = json.getInt(TAG_ID);
        grade = Grade.valueOf(json.getString(TAG_GRADE));
        experience = json.getInt(TAG_EXPERIENCE);
        totalDistance = Float.valueOf(json.getString(TAG_TOTAL_DISTANCE));
        totalTime = Time.valueOf(json.getString(TAG_TOTAL_TIME));
        totalCalories = json.getInt(TAG_TOTAL_CALORIES);
        maxSpeed = Float.valueOf(json.getString(TAG_MAX_SPEED));
        avgSpeed = Float.valueOf(json.getString(TAG_AVG_SPEED));

    }

    /**
     * Background task for registering
     */
    private class RegisterUser extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(LoginActivity.this);
            mProgressDialog.setMessage("Creating new user...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        protected Integer doInBackground(String... args) {
            int indicator = 0;
            email = mEmail.getText().toString();
            password = mPassword.getText().toString();

            try {
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("email", email));
                params.add(new BasicNameValuePair("password", password));

                // get JSON Object
                JSONObject json = jsonParser.makeHttpRequest(REGISTER_URL, HttpMethod.POST, params);

                Log.d("Response: ", json.toString());

                indicator = json.getInt(TAG_SUCCESS);
                // check for success tag
                try {
                    if (indicator == 1) {
                        // registration successful
                        GetJSONNodes(json);
                        Login();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
            if(result == 1) {
                popToast("Registration successful", "short");
            }
            else if(result == -1){
                popToast("A user with this email already exists", "short");
            } else {
                popToast("Registration failed", "short");
            }

        }
    }

    // toast alerts
    private void popToast(String message, String length) {

        if(length.equals("short")) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
