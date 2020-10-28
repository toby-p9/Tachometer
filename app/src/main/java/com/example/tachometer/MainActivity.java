package com.example.tachometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DialogCallback {

    //general setup
    Speed_Indicator fragment;
    FragmentManager mgr;
    SharedPreferences prefs;
    Button locationPerm;
    boolean speed_type_set = false;
    boolean perm_info_shown = false;

    private static final int Callback_Code = 1;

    //location stuff
    double longitudeGPS;
    double latitudeGPS;
    LocationManager lm;
    LocationListener locationListener = new MainActivity.MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mgr = getSupportFragmentManager();
        prefs = getSharedPreferences("internal data", MODE_PRIVATE);
        locationPerm = findViewById(R.id.permissionStatus);

        //add button to open MapActivity
        final Intent second_activity = new Intent(this, MapActivity.class);
        Button but = findViewById(R.id.buttonMapActivity);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(second_activity);
            }
        });

        //add onClickListener for permissions button
        locationPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionRequest();
            }
        });

        //add speed gauge fragment
        FragmentTransaction trans = mgr.beginTransaction();
        fragment = new Speed_Indicator();
        trans.add(R.id.Fragment_Speed_Indicator, fragment);
        trans.commitNow();
        //don't use the fragment here, it has not been created yet

        //inform user about location permission and ask for it
        if (prefs.getBoolean("first_launch", true)) {
            LocationDialog diag = new LocationDialog();
            diag.show(mgr, "hey");
        } else {
            permissionCheck();
        }

        /* location */
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(MainActivity.this, "Resume number "+ i++, Toast.LENGTH_SHORT).show();

        if (!prefs.getBoolean("first_launch", true)) { //if not first launch
            if (permissionCheck()) { //if we have permission, start listening
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            }
        } //first launch, do nothing

        //here, the GPS data should be fed into the speed gauge to determine the current velocity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lm.removeUpdates(locationListener);
    }

    public boolean permissionCheck(){ //checks whether fine location permission is granted and sets button accordingly
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int status = ContextCompat.checkSelfPermission( this, permission);
        if (status == PackageManager.PERMISSION_GRANTED){
            locationPerm.setText(R.string.loc_perm_but_pos); //necessary for opening activity with permissions enabled
            locationPerm.setEnabled(false);
            return true;
        } else{
            locationPerm.setText(R.string.loc_perm_but_neg);
            locationPerm.setEnabled(true);
            return false;
        }
    }

    @Override
    public void permissionRequest(){
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int status = ContextCompat.checkSelfPermission( this, permission);

        if (status != PackageManager.PERMISSION_GRANTED){
            if (shouldShowRequestPermissionRationale(permission)){ //this means permission has been denied but not "never ask"-ed
                if (!perm_info_shown){
                    //show it
                    LocationDialogDenied diagDen = new LocationDialogDenied();
                    diagDen.show(mgr, "hey1");
                    perm_info_shown = true;
                } else {
                    requestPermissions(new String[] {permission}, Callback_Code);
                }
            } else { //either first launch or "never ask"-ed
                if (!prefs.getBoolean("first_launch", true)){ //"never ask"-ed
                    LocationDialogDisabled diag = new LocationDialogDisabled();
                    diag.show(mgr, "hey2");
                    locationPerm.setEnabled(true);
                    locationPerm.setText(R.string.loc_perm_but_disabled);

                    //make button go to app settings so user can allow permission
                    final Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                    myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    locationPerm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(myAppSettings);
                        }
                    });
                } else{ //first launch of app, request normally
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("first_launch", false);
                    editor.apply();
                    requestPermissions(new String[] {permission}, Callback_Code);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Callback_Code && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            locationPerm.setText(R.string.loc_perm_but_pos);
            locationPerm.setEnabled(false);
        } else{ //permission not granted
            locationPerm.setText(R.string.loc_perm_but_neg);
            locationPerm.setEnabled(true);
        }
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                longitudeGPS = loc.getLongitude();
                latitudeGPS = loc.getLatitude();
                fragment.setSpeed(loc.getSpeed());
                if (!speed_type_set){
                    fragment.setSpeedUnit();
                    speed_type_set = true;
                }

                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Network Provider update", Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this,
                                "Stao: " + latitudeGPS + " : " + longitudeGPS,
                                Toast.LENGTH_SHORT).show();
                    }
                });*/
            }
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    } // class MyLocationListener
}
/*
try {
    Thread.sleep(5* 1000);
} catch (InterruptedException e) {
    e.printStackTrace();
}*/

/*
(new Handler()).postDelayed(new Runnable() {
    @Override
    public void run() {
        permission_check();
    }
}, 5000);
*/
