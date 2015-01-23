package apex.prj300.ie.apex.app.classes.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.Date;

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
    public String route;
    public Float distance; // km
    public Date time;
    public Date dateCreated;

    /**
     * Gson converts route array to JSON
     */
    Gson gson = new Gson();
    public String jsonRoute = gson.toJson(route);
    /**
     * Getter and setter methods
     */
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

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * Constructor methods
     */

    // constructor
    public Route() { }

    // default constructor
    public Route(int userID, Grade grade, Terrain terrain, String route, Float distance, Date time, Date dateCreated) {
        this.userID = userID;
        this.grade = grade;
        this.terrain = terrain;
        this.route = route;
        this.distance = distance;
        this.time = time;
        this.dateCreated = dateCreated;
    }

}
