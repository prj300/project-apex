package apex.prj300.ie.apex.app.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import apex.prj300.ie.apex.app.NewRouteActivity;
import apex.prj300.ie.apex.app.R;

import static apex.prj300.ie.apex.app.NewRouteActivity.*;


/**
 * Created by Enda on 09/02/2015.
 */
public class MyMapFragment extends Fragment implements
        PassLocationListener {

    private static final String TAG_CONTEXT = "MyMapFragment";
    private SupportMapFragment fragment;
    private GoogleMap mMap;

    /**
     * Create view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    /**
     * Create activity
     */
    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);

        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if(fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mMap == null) {
            mMap = fragment.getMap();
            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * Instead of registering a LocationListener
     * on both this fragment's activity and
     * NewRouteActivity, the location will be passed from the
     * Main Activity to this fragment. Virtually the same thing
     * but ensures the values stay the same/are not duplicates
     */
    @Override
    public void onPassLocation(Location location) {
        Log.d(TAG_CONTEXT, "My Location: " + location);
    }
}
