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
    private static final String LOGIN_URL = "http://10.0.2.2/android/apexdb/login_user.php";
    //register url
    private static final String REGISTER_URL = "http://10.0.2.2/android/apexdb/create_user.php";

    //JSON response
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_ID = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check if user is already logged in
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        // row count
        int count = db.rowCount();
        // if row exists
        if(count == 1) {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_login);

            mEmail = (EditText) findViewById(R.id.txtEmail);
            mPassword = (EditText) findViewById(R.id.txtPassword);
            mLogin = (Button) findViewById(R.id.btnLogin);
            mRegister = (Button) findViewById(R.id.btnRegister);

            mLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new LoginUser().execute();
                }
            });
            // onclick listener to begin register task
            mRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new RegisterUser().execute();
                }
            });
        }
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
     *
     * Background task for logging in
     *
     */
    private class LoginUser extends AsyncTask<String, String, String> {

        // before starting task show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(LoginActivity.this);
            mProgressDialog.setMessage("Logging In...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String...args) {
            // read inputs into strings
            String email = mEmail.getText().toString();
            String password = mPassword.getText().toString();

            // check if fields are empty
            if (email.isEmpty() || password.isEmpty()) {
                String message = "Required fields are missing";
                makeToast(message);
            } else {
                try {
                    // build parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("email", email));
                    params.add(new BasicNameValuePair("password", password));

                    // get JSON Object
                    JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, HTTP.POST, params);

                    Log.d("Response: ", json.toString());

                    // check success tag
                    try {
                        int success = json.getInt(TAG_SUCCESS);
                        String message = json.getString(TAG_MESSAGE);

                        if (success == 1) {
                            // login successful
                            int id = json.getInt(TAG_ID);
                            Login(id);

                        } else {
                            makeToast(message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        // after completing dismiss Progress Dialog
        protected void onPostExecute(String file_url) {
            mProgressDialog.dismiss();
        }
    }

    /**
     *
     * Background task for registering
     *
     */
    private class RegisterUser extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(LoginActivity.this);
            mProgressDialog.setMessage("Creating new user...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            if(mEmail.getText().equals("") || mPassword.getText().equals("")) {
                String message = "Required fields are missing";
                makeToast(message);
            } else {
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                try {
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("email", email));
                    params.add(new BasicNameValuePair("password", password));

                    // get JSON Object
                    JSONObject json = jsonParser.makeHttpRequest(REGISTER_URL, HTTP.POST, params);

                    Log.d("Response: ", json.toString());

                    // check for success tag
                    try {
                        int success = json.getInt(TAG_SUCCESS);
                        String message = json.getString(TAG_MESSAGE);

                        if (success == 1) {
                            // registration successful
                            int id = json.getInt(TAG_ID);
                            Login(id);
                        } else {
                            // registration failed
                            makeToast(message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        // after completing dismiss Progress Dialog
        protected void onPostExecute(String file_url) {
            mProgressDialog.dismiss();
        }
    }

    // toast alerts
    private void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // method to login user in local SQLite Database
    private void Login(int id) {
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        // logout any previous user
        db.resetTables();
        // add user to table
        db.addUser(id);

        // go to home activity
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);

        finish();
    }
}
