package com.example.mdp_group31.main;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.mdp_group31.MainActivity;
import com.example.mdp_group31.R;

public class LoadMapFragment extends DialogFragment {

    private static final String TAG = "LoadMapFragment";
    private SharedPreferences.Editor editor;

    SharedPreferences sharedPreferences;

    Button loadBtn, cancelBtn;
    String map;
    View rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        showLog("Entering onCreateView");
        rootView = inflater.inflate(R.layout.activity_load_map, container, false);
        super.onCreate(savedInstanceState);

        getDialog().setTitle("Load Map");
        sharedPreferences = getActivity().getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        loadBtn = rootView.findViewById(R.id.loadMapBtn);
        cancelBtn = rootView.findViewById(R.id.cancelLoadMapBtn);

        map = sharedPreferences.getString("mapChoice","");

        if (savedInstanceState != null)
            map = savedInstanceState.getString("mapChoice");

        final Spinner spinner = (Spinner) rootView.findViewById(R.id.mapDropdownSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.save_map_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked loadBtn");
                map = spinner.getSelectedItem().toString();
                editor.putString("mapChoice", map);
                String obsPos = sharedPreferences.getString(map,"");
                if(! obsPos.equals("")) {
                    String[] obstaclePosition = obsPos.split("\\|");
                    for (String s : obstaclePosition) {
                        String[] coords = s.split(",");
                        coords[3] = "OB" + coords[3];
                        ((MainActivity)getActivity()).getGridMap().setObstacleCoord(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), coords[3]);
                        String direction;
                        switch (coords[2]) {
                            case "E":
                                direction = "East";
                                break;
                            case "S":
                                direction = "South";
                                break;
                            case "W":
                                direction = "West";
                                break;
                            default:
                                direction = "North";
                        }
                        GridMap.IMAGE_BEARING[Integer.parseInt(coords[1]) - 1][Integer.parseInt(coords[0]) - 1] = direction;
                    }
                    ((MainActivity)getActivity()).getGridMap().invalidate();
                    showLog("Exiting Load Button");
                }
                getDialog().dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLog("Clicked cancelDirectionBtn");
                showLog( "Exiting cancelDirectionBtn");
                getDialog().dismiss();
            }
        });
        showLog("Exiting onCreateView");
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        showLog("Entering onSaveInstanceState");
        super.onSaveInstanceState(outState);
        loadBtn = rootView.findViewById(R.id.loadMapBtn);
        showLog("Exiting onSaveInstanceState");
        outState.putString(TAG, map);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        showLog("Entering onDismiss");
        super.onDismiss(dialog);
        showLog("Exiting onDismiss");
    }

    private void showLog(String message) {
        Log.d(TAG, message);
    }
}

