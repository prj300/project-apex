package apex.prj300.ie.apex.app.classes.models;

/**
 * Created by Enda on 21/01/2015.
 */
public class Route {

    public double latitude;
    public double longitude;

    // constructor
    public Route(double lat, double lng) { }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double lat) {
        this.latitude = lat;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double lng) {
        this.longitude = lng;
    }
}
