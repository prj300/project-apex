package apex.prj300.ie.apex.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
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

import apex.prj300.ie.apex.app.classes.db.UserDB;
import apex.prj300.ie.apex.app.classes.enums.HttpMethod;
import apex.prj300.ie.apex.app.classes.methods.JSONParser;
import apex.prj300.ie.apex.app.classes.models.User;


public class ResetPasswordActivity extends Activity {

    /**
     * Static variables
     */
    private static final String TAG_CONTEXT = "ResetPasswordActivity";
    JSONParser jsonParser = new JSONParser();
    ProgressDialog mProgressDialog;
    EditText mTextEmail;
    Button mResetButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mTextEmail = (EditText)findViewById(R.id.txtEmail);
        mResetButton = (Button) findViewById(R.id.btnReset);

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mTextEmail.getText().toString().isEmpty()) {
                    if(networkAvailable()) {
                        new ResetPassword(mTextEmail.getText().toString()).execute();
                    } else {
                        Toast.makeText(getApplicationContext(), "No network connection", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Missing field(s)", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Check to see if there is an internet connection
     * If none return false
     */
    private boolean networkAvailable() {
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
        getMenuInflater().inflate(R.menu.menu_reset_password, menu);
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
     * Send email to server
     */
    private class ResetPassword extends AsyncTask<Void, Void, JSONObject>{
        String email;
        public ResetPassword(String email) {
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show Progress Dialog before executing
            mProgressDialog = new ProgressDialog(ResetPasswordActivity.this);
            mProgressDialog.setMessage("Sending password reset link...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }
        @Override
        protected JSONObject doInBackground(Void... params) {

            List<NameValuePair> args = new ArrayList<>();
            args.add(new BasicNameValuePair("tag", "reset"));
            args.add(new BasicNameValuePair("email", email));

            return jsonParser.makeHttpRequest(getString(R.string.user_controller), HttpMethod.POST, args);
        }

        /**
         * Post server request logic takes place here
         */
        protected void onPostExecute(JSONObject json) {
            // dismiss progress dialog
            Log.d(TAG_CONTEXT, "Response: " + json);
            mProgressDialog.dismiss();

            if(json != null) {
                try {
                    if (json.getBoolean("success")) {
                        Toast.makeText(getApplicationContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), json.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG_CONTEXT, "JSONException: " + e.getMessage());
                }
            } else {
                Toast.makeText(getApplicationContext(), "Server error.", Toast.LENGTH_LONG).show();
            }

        }
    }
}
