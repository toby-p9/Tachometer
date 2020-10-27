package com.example.tachometer;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.nitri.gauge.Gauge;

public class Speed_Indicator extends Fragment {

    final double SPEED_KMH_TO_MPH = 1.0/1.609;
    final double SPEED_KMH_TO_MS = 1.0/3.6;
    double speed_kmh = 42.0;

    enum SPEED_TYPE{
        KMH, MPH, MS
    }

    SPEED_TYPE speed_type = SPEED_TYPE.KMH;
    TextView txt_speed;
    Gauge gauge;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_speed__indicator, container, false);
        txt_speed = view.findViewById(R.id.speed);
        gauge = view.findViewById(R.id.gauge);
        final TextView speed_unit = view.findViewById(R.id.speed_unit);
        speed_unit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(speed_type){
                    case KMH:   speed_unit.setText(R.string.unit_mph); //gauge is switched to miles per hour
                        txt_speed.setText(String.valueOf(Math.round(speed_kmh *SPEED_KMH_TO_MPH)));
                        speed_type = SPEED_TYPE.MPH;
                        gauge.setMaxValue(120);
                        gauge.setValuePerNick(1);
                        gauge.setTotalNicks(140);
                        gauge.moveToValue(Math.round(speed_kmh *SPEED_KMH_TO_MPH));
                        break;
                    case MPH:   speed_unit.setText(R.string.unit_m_s); //gauge is switched to meters per second
                        txt_speed.setText(String.valueOf(Math.round(speed_kmh *SPEED_KMH_TO_MS)));
                        speed_type = SPEED_TYPE.MS;
                        gauge.setMaxValue(50);
                        gauge.setValuePerNick(0.5f);
                        gauge.setTotalNicks(120);
                        gauge.moveToValue(Math.round(speed_kmh *SPEED_KMH_TO_MS));
                        break;
                    case MS:    speed_unit.setText(R.string.unit_km_h); //gauge is switched to kilometers per hour
                        txt_speed.setText(String.valueOf(Math.round(speed_kmh)));
                        speed_type = SPEED_TYPE.KMH;
                        gauge.setMaxValue(200);
                        gauge.setValuePerNick(2);
                        gauge.setTotalNicks(120);
                        gauge.moveToValue(Math.round(speed_kmh));
                        break;
                }
            }
        });
        return view;
    }

    public void setSpeed(double speed){
        speed_kmh = speed;
        int rounded_speed = (int) Math.round(speed);
        txt_speed.setText(String.valueOf(rounded_speed));
        gauge.moveToValue(rounded_speed);
    }
}