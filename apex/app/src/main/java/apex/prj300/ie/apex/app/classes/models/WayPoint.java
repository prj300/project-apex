package apex.prj300.ie.apex.app.classes.models;

/**
 * Created by Enda on 17/02/2015.
 */
public class WayPoint {


    public int id;
    public double latitude;
    public double longitude;
    public int locationId;
    public String county;
    public String name;

    public WayPoint(int id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Discovery Point constructor
     */
    public WayPoint(int id, double latitude, double longitude, int locationId, String county, String name) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationId = locationId;
        this.county = county;
        this.name = name;
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

    public int getLocation_id() {
        return locationId;
    }

    public void setLocation_id(int location_id) {
        this.locationId = location_id;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
