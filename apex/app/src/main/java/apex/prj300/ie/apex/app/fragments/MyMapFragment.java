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

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import apex.prj300.ie.apex.app.R;
import apex.prj300.ie.apex.app.classes.db.RouteDB;
import apex.prj300.ie.apex.app.interfaces.PassLocationListener;

import static apex.prj300.ie.apex.app.NewRouteActivity.*;


/**
 * Created by Enda on 09/02/2015.
 */
public class MyMapFragment extends Fragment implements PassLocationListener {

    private static final String TAG_CONTEXT = "MyMapFragment";
    private SupportMapFragment fragment;
    private GoogleMap mMap;
    // ArrayList to store all LatLng points received from parent activity
    private static ArrayList<LatLng> mLatLngs = new ArrayList<>();

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
            // Google Map = fragment's Map
            mMap = fragment.getMap();
            mMap.setMyLocationEnabled(true);
            // mMap.getMyLocation(); returns null
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
        LatLng mLatLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        updateCamera(mLatLng);
        updateUI(location);
    }

    /**
     * Update Map Interface in real-time
     * Follow user as they move
     */
    private void updateUI(Location location) {
        // Create a new LatLng from location passed from Parent Activity
        LatLng mLatLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        // Add mLatLng to list
        mLatLngs.add(mLatLng);

        // Plot array on map
            mMap.addPolyline(new PolylineOptions()
                    .addAll(mLatLngs)
                    .width(6f)
                    .color(Color.BLUE)
                    .geodesic(true));

    }

    /**
     * Update the camera to follow the user
     */
    private void updateCamera(LatLng latLng) {
        CameraUpdate cameraUpdate;

        // Move the camera to specified zoom level and location
        cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16.5f);
        mMap.animateCamera(cameraUpdate);

        // Move the camera to location at whatever zoom
        // level camera is currently at. This only happens
        // if list of LatLngs is not empty
        if(!mLatLngs.isEmpty()) {
            // Zoom level is at level currently specified by user/device
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
                    mMap.getCameraPosition().zoom);
            mMap.animateCamera(cameraUpdate);
        }
    }
}
