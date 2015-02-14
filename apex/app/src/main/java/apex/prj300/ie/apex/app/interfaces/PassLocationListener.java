package apex.prj300.ie.apex.app.interfaces;

import android.location.Location;

/**
 * Created by Enda on 11/02/2015.
 */
// Defining an interface to pass a location to MyMapFragment
public interface PassLocationListener {
    void onPassLocation(Location location);
}
