package com.example.tachometer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

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

import static android.widget.Toast.makeText;

public class MapActivity extends AppCompatActivity {

    int maxSpeed = 0;
    int avgSpeed = 0;
    boolean updateUI = true;
    int SLEEP_DURATION = 2000;
    TextView maxSpeedView;
    TextView avgSpeedView;
    TextView longView;
    TextView latView;
    Button buttonBack;
    Button buttonReset;
    Button buttonSave;
    Button buttonTest;
    /* Persistent Data */
    SharedPreferences prefs;
    String prefsName = "internalData";
    /* Location Stuff */
    double longitudeGPS = 0.0;
    double latitudeGPS = 0.0;
    double longitudeOLD = 0.0;
    double latitudeOLD = 0.0;

    LocationManager lm;
    LocationListener locationListener = new MyLocationListener();
//    String[] permissions = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};

    /* UI */
    private Handler mainHandler = new Handler();

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                longitudeGPS = Math.round(loc.getLongitude() * 10000) / 10000.0;
                latitudeGPS = Math.round(loc.getLatitude() * 10000) / 10000.0;
                avgSpeed = ((int) loc.getSpeed() + avgSpeed) / 2;
                if (maxSpeed < avgSpeed) {
                    maxSpeed = avgSpeed;
                }
                longView.setText(String.valueOf(longitudeGPS));
                latView.setText(String.valueOf(latitudeGPS));
                maxSpeedView.setText(String.valueOf(maxSpeed));
                avgSpeedView.setText(String.valueOf(avgSpeed));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(MapActivity.this, "Network Provider update", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            makeText(getBaseContext(), "Provider : " + provider + " deactivated", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            makeText(getBaseContext(), "Provider : " + provider + " activated", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String statusString = "";
            switch (status) {
                case LocationProvider.AVAILABLE:
                    statusString = "available";
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    statusString = "out of service";
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusString = "temporarily unavailable";
                    break;
            }
//            Toast.makeText(getBaseContext(), provider + " " + statusString, Toast.LENGTH_SHORT).show();
        }
    } // class MyLocationListener


    class BackgroundRunnableProvider implements Runnable {
        BackgroundRunnableProvider() {
        }

        @Override
        public void run() {
            while (updateUI) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        /* checks if provider available */
                        if (isLocationEnabled()) {
//                            Toast.makeText(getBaseContext(), "Provider vorhanden", Toast.LENGTH_SHORT).show();
                        } else {
                            makeText(getBaseContext(), "Provider nicht vorhanden", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                sleep();
            }
        }

        private void sleep() {
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    } // runnable


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Fragment fragment = new Fragment();
        Bundle extras = new Bundle();
        extras.putDouble("lat", latitudeGPS);
        extras.putDouble("long", longitudeGPS);
        fragment.setArguments(extras);

        /* Intents */
        final Intent go_back_to_main = new Intent(this, MainActivity.class);

        /* Data Persistence */
        prefs = getSharedPreferences(prefsName, MODE_PRIVATE);
        readPrefValues();

        /* Views */
        maxSpeedView = findViewById(R.id.textView_max_speed_value);
        maxSpeedView.setText(String.valueOf(maxSpeed));
        maxSpeedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxSpeed++;
                maxSpeedView.setText(String.valueOf(maxSpeed));
            }
        });
        avgSpeedView = findViewById(R.id.textView_avg_speed_value);
        avgSpeedView.setText(String.valueOf(avgSpeed));
        avgSpeedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avgSpeed++;
                avgSpeedView.setText(String.valueOf(avgSpeed));
            }
        });

        longView = findViewById(R.id.textView_map_long);
        longView.setText(String.valueOf(longitudeGPS));
        latView = findViewById(R.id.textView_map_lat);
        latView.setText(String.valueOf(latitudeGPS));

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
                avgSpeed = 0;
                maxSpeed = 0;
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
                makeText(context, "Data saved", Toast.LENGTH_SHORT).show();
            }
        });
        buttonTest = findViewById(R.id.button_map_test);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocationEnabled()) {
                    makeText(getBaseContext(), "Provider vorhanden", Toast.LENGTH_SHORT).show();
                } else {
                    makeText(getBaseContext(), "Provider nicht vorhanden", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /* location */
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        /* UI update */
        BackgroundRunnableProvider runnable = new BackgroundRunnableProvider();         /* check provider */
        new Thread(runnable).start();
    } //onCreate


    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getBaseContext(), R.string.loc_perm_map_go_back, Toast.LENGTH_SHORT).show();
        } else {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000, 0, locationListener);
        }
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

    public double getLatitude() {
        return latitudeGPS;
    }

    public double getLongitude() {
        return longitudeGPS;
    }

    public int calculateSpeed() {
        double distLong = Math.abs(longitudeGPS - longitudeOLD) * 111000.0;
        double distLat = Math.abs(latitudeGPS - latitudeOLD) * 111000.0;
        double temp = Math.sqrt( Math.pow(distLong, 2) + Math.pow(distLat, 2) ) / SLEEP_DURATION * 1000;
        avgSpeed = (int) temp;
        longitudeOLD = longitudeGPS;
        latitudeOLD = latitudeGPS;
        return avgSpeed ;
    }


} // MapActivity



