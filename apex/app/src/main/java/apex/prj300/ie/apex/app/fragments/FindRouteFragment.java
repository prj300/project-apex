package apex.prj300.ie.apex.app.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import apex.prj300.ie.apex.app.R;
import apex.prj300.ie.apex.app.classes.models.Result;

public class FindRouteFragment extends Fragment {


    private static final String TAG_CONTEXT = "FindRouteFragment";
    // Will contain the options of choosing a desired route
    private static ListView mListView;

    public FindRouteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // get rootView
        View rootView = inflater.inflate(R.layout.fragment_find_route, container, false);
        mListView = (ListView) rootView.findViewById(R.id.list_find_route);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, getResources()
                .getStringArray(R.array.route_options_array));

        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG_CONTEXT, "Item selected: " + position);
                // Take actions based on selected list item
                switch (position) {
                    case 0:
                        findRouteByDistance();
                    break;
                    case 1:
                    break;
                    case 2:
                    break;
                }
            }
        });
        return rootView;

    }

    /**
     * Find a route by desired distance/length
     */
    private void findRouteByDistance() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.enter_distance_title);
        NumberPicker np = new NumberPicker(getActivity());
        String[] nums = new String[100];
        for(int i=0;i<nums.length;i++)
            nums[i] = Integer.toString(i);

        np.setMinValue(1);
        np.setMaxValue(nums.length-1);
        np.setWrapSelectorWheel(false);
        np.setDisplayedValues(nums);
        np.setValue(25);
        builder.setView(np);
        builder.show();

    }


}
