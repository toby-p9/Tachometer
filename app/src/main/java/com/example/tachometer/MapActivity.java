package com.example.tachometer;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;



public class MapActivity extends AppCompatActivity {

    int maxSpeed = 100;
    int avgSpeed = 100;
    boolean updateUI = true;

    TextView maxSpeedView;
    TextView avgSpeedView;
    Button buttonBack;
    Button buttonReset;
    Button buttonSave;
    Button buttonTest;
    /* Persistent Data */
    SharedPreferences prefs;
    String prefsName = "internalData";
    /* Location Stuff */
    double longitudeGPS, latitudeGPS;
    LocationManager lm;
    LocationListener locationListener = new MyLocationListener();
    /* UI */
    private Handler mainHandler = new Handler();


    private class MyLocationListener implements LocationListener {
        @Override

        public void onLocationChanged(Location loc) {
            if (loc != null) {
                longitudeGPS = loc.getLongitude();
                latitudeGPS = loc.getLatitude();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MapActivity.this, "Network Provider update", Toast.LENGTH_SHORT).show();
                        Toast.makeText(MapActivity.this,
                                "Stao: " + latitudeGPS + " : " + longitudeGPS,
                                Toast.LENGTH_SHORT).show();
                    }
                });
             }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getBaseContext(), "Provider : " + provider + " deactivated", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getBaseContext(), "Provider : " + provider + " activated", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String statusString = "";
            switch (status) {
                case LocationProvider.AVAILABLE:
                    statusString = "available";
                case LocationProvider.OUT_OF_SERVICE:
                    statusString = "out of service";
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusString = "temporarly unavailable";
            }
            Toast.makeText(getBaseContext(), provider + " " + statusString, Toast.LENGTH_SHORT).show();
        }


    } // class MyLocationListener


    class BackgroundRunnable implements Runnable {
        BackgroundRunnable() {
        }

        @Override
        public void run() {
            while (updateUI) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        /* checks if provider available */
                        if (isLocationEnabled()){
                            Toast.makeText(getBaseContext(), "Provider vorhanden", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getBaseContext(), "Provider nicht vorhanden", Toast.LENGTH_SHORT).show();
                        }
                        /* checks UI update */
                        // avgSpeed += 1;
//                        avgSpeedView.setText(String.valueOf(avgSpeed));
                    }
                });
                sleep();
            }


        }

        private void sleep() {
            try {
                Thread.sleep(60* 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    } // runnable


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        /* Data Persistence */
        prefs = getSharedPreferences(prefsName, MODE_PRIVATE);
        readPrefValues();

        /* Intents */
        final Intent go_back_to_main = new Intent(this, MainActivity.class);

        /* Views */
        maxSpeedView = findViewById(R.id.textView_max_speed_value);
        maxSpeedView.setText(String.valueOf(maxSpeed));
        avgSpeedView = findViewById(R.id.textView_avg_speed_value);
        avgSpeedView.setText(String.valueOf(avgSpeed));

        /* Buttons */
        buttonBack = findViewById(R.id.button_map_back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(go_back_to_main);
            }
        });

        buttonReset = findViewById(R.id.button_map_reset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxSpeed = 0;
                avgSpeed = 0;
                maxSpeedView.setText(String.valueOf(maxSpeed));
                avgSpeedView.setText(String.valueOf(avgSpeed));
            }
        });

        buttonSave = findViewById(R.id.button_map_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePrefValues();
                Context context = getApplicationContext();
                Toast.makeText(context, "Data saved", Toast.LENGTH_SHORT).show();
            }
        });

        buttonTest = findViewById(R.id.button_map_test);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxSpeed += +1;
                maxSpeedView.setText(String.valueOf(maxSpeed));
            }
        });

        /* location */
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        /* UI update */
        BackgroundRunnable runnable = new BackgroundRunnable();
        new Thread(runnable).start();


    } //onCreate

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // requestPermissions( new String[] {permission} , CALLBACK_CODE);

            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permission, int[] grantResults);


            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePrefValues();
    }

    @Override
    protected void onStop() {
        super.onStop();
        savePrefValues();
    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
        lm.removeUpdates(locationListener);
    }

    private void readPrefValues() {
        prefs = getSharedPreferences(prefsName, MODE_PRIVATE);
        maxSpeed = prefs.getInt("maxspeed", 0);
        avgSpeed = prefs.getInt("avgspeed", 0);
    }

    private void savePrefValues() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("maxspeed", Integer.parseInt(Integer.toString(maxSpeed)));
        editor.putInt("avgspeed", Integer.parseInt(Integer.toString(avgSpeed)));
        editor.commit();
    }

    private boolean isLocationEnabled() {
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


} // class
