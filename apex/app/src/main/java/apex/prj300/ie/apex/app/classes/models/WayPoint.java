package apex.prj300.ie.apex.app.classes.models;

/**
 * Created by Enda on 17/02/2015.
 */
public class WayPoint {

    public int id;
    public double latitude;
    public double longitude;

    public WayPoint(int id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
