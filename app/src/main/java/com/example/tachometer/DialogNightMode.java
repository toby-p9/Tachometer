package com.example.tachometer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogNightMode extends DialogFragment {
    private DialogCallback callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (DialogCallback) context;
        } catch (ClassCastException e){
            throw new ClassCastException("Oh no, look at file LocationDialog - something went wrong.");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.diag_night_mode_title)
                .setItems(R.array.night_mode, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                            callback.nightMode(which);
                    }
                });
        return builder.create();
    }
}
