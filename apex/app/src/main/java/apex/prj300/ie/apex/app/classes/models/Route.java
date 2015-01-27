package apex.prj300.ie.apex.app.classes.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import apex.prj300.ie.apex.app.classes.enums.Grade;
import apex.prj300.ie.apex.app.classes.enums.Terrain;

/**
 * Created by Enda on 21/01/2015.
 */
public class Route {

    /**
     * Route model
     */
    // nav property
    public int userID;
    // model attributes
    public Grade grade;
    public Terrain terrain;
    public List<Double> latitudes;
    public List<Double> longitudes;
    public float distance; // km
    public Date dateCreated;

    public Route() { }
    /**
     * Getter and setter methods
     */
    public Route(int userID, Grade grade, Terrain terrain, List<Double> latitudes, List<Double> longitudes, Float distance, Date dateCreated) {
        this.userID = userID;
        this.grade = grade;
        this.terrain = terrain;
        this.latitudes = latitudes;
        this.longitudes = longitudes;
        this.distance = distance;
        this.dateCreated = dateCreated;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public List<Double> getLatitudes() {
        return latitudes;
    }

    public void setLatitudes(List<Double> latitudes) {
        this.latitudes = latitudes;
    }

    public List<Double> getLongitudes() {
        return longitudes;
    }

    public void setLongitudes(List<Double> longitudes) {
        this.longitudes = longitudes;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}

