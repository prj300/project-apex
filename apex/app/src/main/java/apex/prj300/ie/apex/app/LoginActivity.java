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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

                    if(success == 1) {
                        // login successful

                    } else if(success == 0) {
                        // login failed - incorrect username/password
                        Toast.makeText(getApplicationContext(),
                                "Incorrect email/password", Toast.LENGTH_SHORT).show();
                    } else if(success == -1) {
                        // login failed - required fields missing
                        Toast.makeText(getApplicationContext(),
                                "Required field(s) missing", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (Exception e){
                e.printStackTrace();

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

                    if (success == 1) {
                        // registration successful
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);

                        finish();
                    } else {
                        // registration failed
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        // after completing dismiss Progress Dialog
        protected void onPostExecute(String file_url) {
            mProgressDialog.dismiss();
        }
    }
}
