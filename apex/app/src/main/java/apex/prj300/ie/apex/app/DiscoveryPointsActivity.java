package apex.prj300.ie.apex.app;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import apex.prj300.ie.apex.app.classes.db.WildAtlanticWayDB;
import apex.prj300.ie.apex.app.classes.models.WayPoint;

public class DiscoveryPointsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery_points);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        ArrayList<WayPoint> discoveryPoints = getDiscoveryPoints();

        for(int i=0;i<discoveryPoints.size();i++) {
            mMap.addMarker(new MarkerOptions()
                    .title(discoveryPoints.get(i).getName())
                    .position(new LatLng(discoveryPoints.get(i)
                            .getLatitude(), discoveryPoints.get(i)
                            .getLongitude())));
        }
    }

    public ArrayList<WayPoint> getDiscoveryPoints() {
        WildAtlanticWayDB db = new WildAtlanticWayDB(this);
        return db.getDiscoveryPoints();
    }
}
