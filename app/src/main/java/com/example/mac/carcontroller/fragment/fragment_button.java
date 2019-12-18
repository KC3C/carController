package com.example.mac.carcontroller.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;

import com.example.mac.carcontroller.MainActivity;
import com.example.mac.carcontroller.R;

public class fragment_button extends Fragment {
    private Button btn_forward, btn_left, btn_right, btn_back, btn_stop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_button, container, false);
        btn_forward = view.findViewById(R.id.bforward);
        btn_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).Forward();
            }
        });

        btn_left = view.findViewById(R.id.bleft);
        btn_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).Left();
            }
        });

        btn_right = view.findViewById(R.id.bright);
        btn_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).Right();
            }
        });

        btn_back = view.findViewById(R.id.bback);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).Back();
            }
        });

        btn_stop = view.findViewById(R.id.bstop);
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).Stop();
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity)getActivity()).Stop();
    }
}
