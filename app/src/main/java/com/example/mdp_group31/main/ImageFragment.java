package com.example.mdp_group31.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mdp_group31.R;

public class ImageFragment extends Fragment {
    SharedPreferences sharedPreferences;
    ImageView obs1, obs2, obs3, obs4, obs5, obs6, obs7, obs8;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate
        View root = inflater.inflate(R.layout.activity_image, container, false);

        // get shared preferences
        sharedPreferences = getActivity().getSharedPreferences("Shared Preferences",
                Context.MODE_PRIVATE);

        obs1 = root.findViewById(R.id.image_1);
        obs2 = root.findViewById(R.id.image_2);
        obs3 = root.findViewById(R.id.image_3);
        obs4 = root.findViewById(R.id.image_4);
        obs5 = root.findViewById(R.id.image_5);
        obs6 = root.findViewById(R.id.image_6);
        obs7 = root.findViewById(R.id.image_7);
        obs8 = root.findViewById(R.id.image_8);

        return root;

    }
}
