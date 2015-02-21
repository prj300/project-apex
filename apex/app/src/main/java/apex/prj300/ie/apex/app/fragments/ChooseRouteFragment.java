package apex.prj300.ie.apex.app.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import apex.prj300.ie.apex.app.R;
import apex.prj300.ie.apex.app.classes.db.RouteDB;
import apex.prj300.ie.apex.app.classes.models.Route;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseRouteFragment extends Fragment {

    private static final String TAG_CONTEXT = "ChooseRouteFragment";
    private ListView mListView;

    public ChooseRouteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_choose_route, container, false);
        mListView = (ListView) view.findViewById(R.id.listView);
        displayRoutes();
        return view;
    }

    /**
     * Display all the routes from the local database in a ListView
     */
    private void displayRoutes() {
        RouteDB db = new RouteDB(getActivity());

        // Populate an array with routes from the database
        ArrayList<Route> routes = db.getRoutes();

        ArrayAdapter<Route> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, routes);

        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG_CONTEXT, "Item selected: " + position);
            }
        });
    }


}
