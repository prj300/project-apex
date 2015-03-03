package apex.prj300.ie.apex.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import apex.prj300.ie.apex.app.StartRouteActivity;
import apex.prj300.ie.apex.app.R;

public class NewRouteFragment extends Fragment {

    public NewRouteFragment() {
        // Required empty public constructor
    }

    /**
     * This fragment is a launchpad to the NewRouteActivity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(getActivity(), StartRouteActivity.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start_route, container, false);
    }


}
