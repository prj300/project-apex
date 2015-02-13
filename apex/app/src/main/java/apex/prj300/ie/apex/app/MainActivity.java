package apex.prj300.ie.apex.app;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import apex.prj300.ie.apex.app.classes.db.UserDB;
import apex.prj300.ie.apex.app.classes.models.User;
import apex.prj300.ie.apex.app.fragments.HomeFragment;
import apex.prj300.ie.apex.app.fragments.MyRoutesFragment;
import apex.prj300.ie.apex.app.fragments.NavigationDrawerFragment;
import apex.prj300.ie.apex.app.fragments.NewRouteFragment;
import apex.prj300.ie.apex.app.interfaces.SignOutListener;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        HomeFragment.OnFragmentInteractionListener {

    private static final String TAG_CONTEXT = "MainActivity";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    // count to check if user is logged in
    private int count;

    SignOutListener mSignOutListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // first check if a user is logged in
        checkLoginStatus();
        // if user exists
        if(count > 0) {
            setContentView(R.layout.activity_home);

            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getFragmentManager().findFragmentById(R.id.navigation_drawer);
            mTitle = getTitle();

            // Set up the drawer.
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));

        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);

            finish();
        }
    }

    private void checkLoginStatus() {
        // checking if user is already logged in
        UserDB db = new UserDB(getApplicationContext());
        // row count
        count = db.rowCount();

        if(count > 0) {
            getUser();
        }
    }

    private void getUser() {
        UserDB db = new UserDB(getApplicationContext());

        /*
      User params
     */
        User user = db.getUser();
        Log.i("User: ", user.getEmail());
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Fragment fragment;
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            default:
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new NewRouteFragment();
                break;
            case 2:
                fragment = new MyRoutesFragment();
                break;
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_activity_home);
                break;
            case 2:
                mTitle = getString(R.string.action_start_recording);
                break;
            case 3:
                mTitle = getString(R.string.action_my_routes);
                break;
            case 4:
                mTitle = getString(R.string.action_find_routes);
        }
    }

    private void signOut() {
        Log.d(TAG_CONTEXT, "Logged out.");

        // make connection to database and clear tables
        UserDB db = new UserDB(getApplicationContext());
        db.resetTables();

        Toast.makeText(getApplicationContext(), "Logged Out.", Toast.LENGTH_SHORT).show();
        // redirect to login activity
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);

        finish();

    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.home, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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
        } else if(id == R.id.sign_out) {
            signOut();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
