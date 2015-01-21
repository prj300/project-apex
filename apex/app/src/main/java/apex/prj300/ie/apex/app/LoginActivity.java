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
import apex.prj300.ie.apex.app.classes.enums.HttpMethod;
import apex.prj300.ie.apex.app.classes.methods.UserDB;
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

    // login url
    private static final String LOGIN_URL = "http://192.168.1.11/android/apexdb/login_user.php";
    //register url
    private static final String REGISTER_URL = "http://192.168.1.11/android/apexdb/create_user.php";

    // JSON response
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ID = "id";

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
            String email = mEmail.getText().toString();
            String password = mPassword.getText().toString();

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
                        // registration successful
                        int id = json.getInt(TAG_ID);
                        Login(id);
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
            String email = mEmail.getText().toString();
            String password = mPassword.getText().toString();

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
                        int id = json.getInt(TAG_ID);
                        Login(id);
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

    // method to login user in local SQLite Database
    private void Login(int id) {
        UserDB db = new UserDB(getApplicationContext());

        // logout any previous user
        db.resetTables();
        // add user to table
        //Toast.makeText(getApplicationContext(), user.getId(), Toast.LENGTH_LONG).show();
        db.addUser(new User(id));
        Log.d("User: ", Integer.toString(id));

        // go to home activity
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        // finish up with this activity
        finish();
    }
}
