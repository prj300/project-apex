package apex.prj300.ie.apex.app.classes.models;

import java.sql.Time;
import java.util.Date;

import apex.prj300.ie.apex.app.classes.enums.Grade;

/**
 * Created by Enda on 05/01/2015.
 */
public class User {

    public int id;
    public String email;
    public Grade grade;
    public int experience;
    public float totalDistance;
    public Time totalTime;
    public float maxSpeed;
    public float avgSpeed;

    public User() { }

    public User(int id, String email, Grade grade,
                int experience, float totalDistance,
                Time totalTime,
                float maxSpeed, float avgSpeed) {
        this.id = id; // 0
        this.email = email; // 1
        this.grade = grade; // 2
        this.experience = experience; // 3
        this.totalDistance = totalDistance; // 4
        this.totalTime = totalTime; // 5
        this.maxSpeed = maxSpeed; // 6
        this.avgSpeed = avgSpeed; // 7
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(float totalDistance) {
        this.totalDistance = totalDistance;
    }

    public Time getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Time totalTime) {
        this.totalTime = totalTime;
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
}
