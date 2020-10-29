package com.example.tachometer;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.nitri.gauge.Gauge;

public class Speed_Indicator extends Fragment {

    final double SPEED_MS_TO_KMH = 3.6;
    final double SPEED_MS_TO_MPH = SPEED_MS_TO_KMH/1.609;

    double speed_m_s = 42.0;

    enum SPEED_TYPE{
        KMH, MPH, MS
    }

    SPEED_TYPE speed_type = SPEED_TYPE.KMH;
    TextView txt_speed;
    TextView speed_unit;
    Gauge gauge;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_speed__indicator, container, false);

        txt_speed = view.findViewById(R.id.speed);
        gauge = view.findViewById(R.id.gauge);
        speed_unit = view.findViewById(R.id.speed_unit);
        speed_unit.setText("...");
        speed_unit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(speed_type){
                    case KMH:
                        speed_type = SPEED_TYPE.MPH; //gauge is switched to miles per hour
                        speed_unit.setText(R.string.unit_mph);
                        gauge.setMaxValue(120);
                        gauge.setValuePerNick(1);
                        gauge.setTotalNicks(140);
                        updateSpeedShown();
                        break;

                    case MPH:
                        speed_type = SPEED_TYPE.MS; //gauge is switched to meters per second
                        speed_unit.setText(R.string.unit_m_s);
                        gauge.setMaxValue(50);
                        gauge.setValuePerNick(0.5f);
                        gauge.setTotalNicks(120);
                        updateSpeedShown();
                        break;

                    case MS:
                        speed_type = SPEED_TYPE.KMH; //gauge is switched to kilometers per hour
                        speed_unit.setText(R.string.unit_km_h);
                        gauge.setMaxValue(200);
                        gauge.setValuePerNick(2);
                        gauge.setTotalNicks(120);
                        updateSpeedShown();
                        break;
                }
            }
        });
        return view;
    }

    public void setSpeed(double speed){ //accepts a speed value in m/s
        speed_m_s = speed;
        updateSpeedShown();
    }

    public void setSpeedUnit(){
        speed_unit.setText(R.string.unit_km_h);
    }

    private void updateSpeedShown(){
        int rounded_speed;
        switch(speed_type){
            case KMH:
                rounded_speed = (int) Math.round(speed_m_s*SPEED_MS_TO_KMH);
                break;
            case MPH:
                rounded_speed = (int) Math.round(speed_m_s*SPEED_MS_TO_MPH);
                break;
            case MS:
            default:
                rounded_speed = (int) Math.round(speed_m_s);
        }
        txt_speed.setText(String.valueOf(rounded_speed));
        gauge.moveToValue(rounded_speed);
    }
}