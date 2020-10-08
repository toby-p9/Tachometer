package com.example.tachometer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    final double SPEED_KMH_TO_MPH = 1.0/1.609;
    final double SPEED_KMH_TO_MS = 1.0/3.6;
    double speed = 42.0;

    enum SPEED_TYPE{
        KMH, MPH, MS
    }

    SPEED_TYPE speed_type = SPEED_TYPE.KMH;
    TextView txt_speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt_speed = findViewById(R.id.speed);
        txt_speed.setText(String.valueOf(speed));

        final Intent second_activity = new Intent(this, MapActivity.class);
        Button but = findViewById(R.id.button);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(second_activity);
            }
        });
        }

    public void onClick(View v){
        TextView t = v.findViewById(R.id.speed_unit);
        switch(speed_type){
            case KMH:   t.setText(R.string.unit_mph);
                        txt_speed.setText(String.valueOf(speed*SPEED_KMH_TO_MPH));
                        speed_type = SPEED_TYPE.MPH;
                        break;
            case MPH:   t.setText(R.string.unit_m_s);
                        txt_speed.setText(String.valueOf(speed*SPEED_KMH_TO_MS));
                        speed_type = SPEED_TYPE.MS;
                        break;
            case MS:    t.setText(R.string.unit_km_h);
                        txt_speed.setText(String.valueOf(speed));
                        speed_type = SPEED_TYPE.KMH;
                        break;
        }
    }
}