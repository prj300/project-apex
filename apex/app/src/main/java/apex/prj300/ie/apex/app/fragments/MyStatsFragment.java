package apex.prj300.ie.apex.app.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.sql.Time;

import apex.prj300.ie.apex.app.R;
import apex.prj300.ie.apex.app.interfaces.PassStatsListener;

/**
 * Created by Enda on 09/02/2015.
 */
public class MyStatsFragment extends Fragment implements PassStatsListener {

    /**
     * Static variables
     */
    private static final String TAG_CONTEXT = "MyStatsFragment";

    /**
     * UI Widgets
     */
    protected TextView mTextMaxSpeed;
    protected TextView mTextAvgSpeed;
    protected TextView mTextDistance;
    protected TextView mTextTime;

    /**
     * Create View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View mView = inflater.inflate(R.layout.fragment_stats, container, false);
        return mView;
    }



    @Override
    public void onMaxSpeedChanged(float maxSpeed) {

    }

    @Override
    public void onAvgSpeedChanged(float avgSpeed) {

    }

    @Override
    public void onDistanceChanged(float distance) {

    }

    @Override
    public void onTimeChanged(Time time) {

    }
}
