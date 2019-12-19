package com.example.mac.carcontroller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mac.carcontroller.R;
import com.example.mac.carcontroller.MainActivity;
import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

public class JoyStickFragment extends Fragment {
    private static final String TAG = JoyStickFragment.class.getCanonicalName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_joystick, container, false);
        Joystick joystick = (Joystick) view.findViewById(R.id.joystick);
        joystick.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {

            }

            @Override
            public void onDrag(float degrees, float offset) {
                int direction = getTheDirection(degrees);
                switch (direction) {
                    case 1: {
                        if (getActivity() != null) {
                            ((MainActivity) getActivity()).Forward();
                        }
                        break;
                    }
                    case 2: {
                        if (getActivity() != null) {
                            ((MainActivity) getActivity()).Left();
                        }
                        break;
                    }
                    case 3: {
                        if (getActivity() != null) {
                            ((MainActivity) getActivity()).Back();
                        }
                        break;
                    }
                    case 4: {
                        if (getActivity() != null) {
                            ((MainActivity) getActivity()).Right();
                        }
                        break;
                    }
                    case -1: {
                        if (getActivity() != null) {
                            ((MainActivity) getActivity()).Stop();
                        }
                        break;
                    }
                }

            }

            @Override
            public void onUp() {
                //((MainActivity) getActivity()).stop();
            }
        });
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity)getActivity()).Stop();
    }
    private int getTheDirection(float degrees) {
        if(65 < degrees && degrees < 115) {
            return 1;
        }
        else if ((155 < degrees && degrees < 180) || (-180 < degrees && degrees < -155) ){
            return 2;
        }
        else if (-115 < degrees && degrees < -65){
            return 3;
        }
        else if (-25 < degrees && degrees < 25){
            return 4;
        }
        else return -1;
    }
}
