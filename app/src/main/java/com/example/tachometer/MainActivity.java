package com.example.tachometer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.Console;

import de.nitri.gauge.Gauge;

public class MainActivity extends AppCompatActivity {

    private static final int Callback_Code = 1;
    Speed_Indicator fragment;
    FragmentManager mgr;

    static boolean user_agreed = false;
    static boolean perm_check_succ = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mgr = getSupportFragmentManager();

        //add button to open MapActivity
        final Intent second_activity = new Intent(this, MapActivity.class);
        Button but = findViewById(R.id.button);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(second_activity);
            }
        });

        //add speed gauge fragment
        FragmentTransaction trans = mgr.beginTransaction();
        fragment = new Speed_Indicator();
        trans.add(R.id.Fragment_Speed_Indicator, fragment);
        trans.commitNow();
        //don't use the fragment here, it has not been created yet

        //inform user about location permission and ask for it
        LocationDialog diag = new LocationDialog();
        diag.show(mgr, "hey");

        //TODO: find a way to wait with permission check until user has checked "Okay" on dialog
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                permission_check();
            }
        }, 5000);
        permission_check();
    }


    @Override
    protected void onStart() {
        super.onStart();
        fragment.setSpeed(50.0);
        //here, the GPS data should be fed into the speed gauge to determine the current velocity
    }

    static public void userAgreed(){
        user_agreed = true;
    }

    public void permission_check(){
        //Ask for location permission
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int status = ContextCompat.checkSelfPermission( this, permission);
        requestPermissions(new String[] {permission}, Callback_Code);

        //use this code whenever you need to use the permission API
        if (status != PackageManager.PERMISSION_GRANTED){
            if (shouldShowRequestPermissionRationale(permission)){
                //user has denied permission, but not clicked "never ask again"
                // TODO: explain why we need location permission
            } else {
                //user has checked "never ask again"
                //TODO: tell user that he can enable permission for app in settings app
                //and cannot use the tachometer/map functionality otherwise
            }
        }
        requestPermissions(new String[] {permission}, Callback_Code);
    }
}
