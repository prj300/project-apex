package apex.prj300.ie.apex.app;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;

import java.sql.Time;

import apex.prj300.ie.apex.app.classes.db.UserDB;
import apex.prj300.ie.apex.app.classes.enums.Grade;
import apex.prj300.ie.apex.app.classes.models.User;



public class HomeActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, FindRouteFragment.OnFragmentInteractionListener {

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
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        user = db.getUser();
        Log.i("User: ", user.getEmail());
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Fragment fragment;
        FragmentManager fragmentManager = getFragmentManager();
        fragment = new FindRouteFragment();
        switch (position) {
            default:
            case 0:
                fragment = new FindRouteFragment();
                break;
            case 1:
                Intent i = new Intent(getApplicationContext(), NewRouteActivity.class);
                startActivity(i);
                break;
            case 2:
                fragment = new PlaceholderFragment();
                break;
            case 3:
                fragment = new FindRouteFragment();
                break;
            case 4:
                fragment = new PlaceholderFragment();
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
            case 5:
                mTitle = getString(R.string.action_sign_out);
                signOut(); // sign out
                break;
        }
    }

    private void signOut() {
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((HomeActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
