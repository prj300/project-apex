package apex.prj300.ie.apex.app.classes.models;

/**
 * Created by Enda on 25/01/2015.
 */
public class LatLong {

    public double Latitude;
    public double Longitude;

    public LatLong(double latitude, double longitude) {
        Latitude = latitude;
        Longitude = longitude;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLatitude(double latitude) {
        Latitude = latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }
}
