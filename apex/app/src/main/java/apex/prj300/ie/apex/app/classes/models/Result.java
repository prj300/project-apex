package apex.prj300.ie.apex.app.classes.models;

import java.sql.Date;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

/**
 * Created by Enda on 26/01/2015.
 */
public class Result {

    public int resultId;
    public int userId;
    public int routeId;
    public float distance;
    public float maxSpeed;
    public float avgSpeed;
    public long time;
    public Date dateCreated;

    public Result() { }

    public Result(int resultId, int userId, int routeId, float distance, float maxSpeed, float avgSpeed, long time, Date dateCreated) {
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

    public String timeString() {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(time),
                TimeUnit.MILLISECONDS.toMinutes(time) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(time) % TimeUnit.MINUTES.toSeconds(1));
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

    @Override
    public String toString() {
        return String.format("Time: %s, Distance: %s, Average Speed: %s " +
                "\nDate: %s", timeString(), getDistance(), getAvgSpeed(), getDateCreated());
    }
}
