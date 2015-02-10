package apex.prj300.ie.apex.app.fragments;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import apex.prj300.ie.apex.app.R;

import static apex.prj300.ie.apex.app.NewRouteActivity.*;


/**
 * Created by Enda on 09/02/2015.
 */
public class MyMapFragment extends Fragment implements PassLocationListener {

    private static final String TAG_CONTEXT = "MyMapFragment";
    private SupportMapFragment fragment;
    private GoogleMap mMap;
    private Location mLocation;
    private Boolean isRecording;
    Polyline mLine;

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

            mMap = fragment.getMap();

        }

    }


    @Override
    public void onResume() {
        super.onResume();
        if(mMap == null) {
            mMap = fragment.getMap();
            mMap.setMyLocationEnabled(true);
            // mMap.getMyLocation(); returns null
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom
                    (new LatLng(54.25075931364725, -8.437499997500026), 2.5f));
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
    public void onPassLocation(Location location, Boolean recording) {
        mLocation = location;
        // decides whether or not a route is being recorded
        isRecording = recording;
        Log.d(TAG_CONTEXT, "My Location: " + location);
        updateUI();
    }

    /**
     * Update Map Interface in real-time
     * Follow user as they move
     */
    private void updateUI() {
        LatLng mLatLng = new LatLng(mLocation.getLatitude(),
                mLocation.getLongitude());

        // Only draw Polyline if the user is recording
        if(isRecording) {
            mLine = mMap.addPolyline(new PolylineOptions()
                    .add(mLatLng)
                    .width(6f)
                    .color(Color.CYAN)
                    .geodesic(true));
        }

        updateCamera(mLatLng);
    }

    private void updateCamera(LatLng mLatLng) {
        // Move camera to updated position on map
        CameraUpdateFactory.newLatLngZoom(mLatLng,
                mMap.getCameraPosition().zoom);
    }
}
