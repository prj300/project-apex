package apex.prj300.ie.apex.app.classes.models;

import java.sql.Date;

/**
 * Created by Enda on 26/01/2015.
 */
public class Results {

    public int resultId;
    public int userId;
    public int routeId;
    public float distance;
    public float maxSpeed;
    public float avgSpeed;
    public long time;
    public Date dateCreated;

    public Results() { }

    public Results(int resultId, int userId, int routeId, float distance, float maxSpeed, float avgSpeed, long time, Date dateCreated) {
        this.resultId = resultId;
        this.userId = userId;
        this.routeId = routeId;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
