package com.example.tachometer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class LocationDialog extends DialogFragment {
    private DialogCallback callback;
    private SharedPreferences prefs;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (DialogCallback) context;
        } catch (ClassCastException e){
            throw new ClassCastException("Oh no, look at file LocationDialog - something went wrong.");
        }
        prefs = context.getSharedPreferences("internal data", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.loc_perm_dia_expl)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callback.permissionRequest(); //call permission check
                    }
                });
        return builder.create();
    }
}
