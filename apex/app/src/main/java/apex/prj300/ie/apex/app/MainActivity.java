package apex.prj300.ie.apex.app;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;


import apex.prj300.ie.apex.app.classes.db.ResultsDB;
import apex.prj300.ie.apex.app.classes.db.RouteDB;
import apex.prj300.ie.apex.app.classes.db.UserDB;
import apex.prj300.ie.apex.app.classes.models.User;
import apex.prj300.ie.apex.app.fragments.FindRouteFragment;
import apex.prj300.ie.apex.app.fragments.HomeFragment;
import apex.prj300.ie.apex.app.fragments.MyResultsFragment;
import apex.prj300.ie.apex.app.fragments.NavigationDrawerFragment;
import apex.prj300.ie.apex.app.fragments.NewRouteFragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // if user is not logged in redirect to login activity
        if(loggedIn() < 1) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            setContentView(R.layout.activity_home);

            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getFragmentManager().findFragmentById(R.id.navigation_drawer);
            mTitle = getTitle();

            // Set up the drawer.
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));
        }
    }

    /**
     * Returns login status
     */
    private int loggedIn() {
        UserDB db = new UserDB(this);
        return db.rowCount();
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
                fragment = new FindRouteFragment();
                break;
            case 2:
                fragment = new MyResultsFragment();
                break;
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private void signOut() {
        Log.d(TAG_CONTEXT, "Logged out.");

        // make connection to database and clear tables
        UserDB userDB = new UserDB(getApplicationContext());
        userDB.resetTables();
        ResultsDB resultsDB = new ResultsDB(getApplicationContext());
        resultsDB.resetTables();
        RouteDB routeDB = new RouteDB(getApplicationContext());
        routeDB.resetTables();

        Toast.makeText(getApplicationContext(), "Logged Out", Toast.LENGTH_SHORT).show();
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
