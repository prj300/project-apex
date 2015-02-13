package apex.prj300.ie.apex.app.interfaces;

import java.sql.Time;

/**
 * Created by Enda on 11/02/2015.
 */

/**
 * Interface to pass map statistics between parent activity and StatsFragment
 */
public interface PassStatsListener {
    void onMaxSpeedChanged(float maxSpeed);
    void onCurrentSpeedChanged(float currentSpeed);
    void onAvgSpeedChanged(float avgSpeed);
    void onDistanceChanged(float distance);
    void onTimeChanged(Time time);
}
