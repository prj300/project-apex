package apex.prj300.ie.apex.app.classes.models;

import java.util.Date;

/**
 * Created by Enda on 26/01/2015.
 */
public class Results {

    public int userId;
    public int routeId;
    public int experience;
    public float distance;
    public float maxSpeed;
    public float avgSpeed;
    public Date time;
    public Date dateCreated;

    public Results() { }

    public Results(int userId, int routeId, int experience, float distance, float maxSpeed, float avgSpeed, Date time, Date dateCreated) {
        this.userId = userId;
        this.routeId = routeId;
        this.experience = experience;
        this.distance = distance;
        this.maxSpeed = maxSpeed;
        this.avgSpeed = avgSpeed;
        this.time = time;
        this.dateCreated = dateCreated;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(float avgSpeed) {
        this.avgSpeed = avgSpeed;
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
}
