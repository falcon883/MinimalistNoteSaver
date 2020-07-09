package com.coffee.minimalistnotesaver.DialogFragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.coffee.minimalistnotesaver.R;

public class OverwriteDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom)
                .setCancelable(false)
                .setTitle("Overwrite Note?")
                .setMessage("This note name already exists, do you want to overwrite the note?")
                .setPositiveButton("Yes", (dialog, which) -> ((OverwriteDialogListener) getActivity()).onOverwriteYes())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof OverwriteDialogListener)) {
            throw new ClassCastException(context.toString() + "must implement OverwriteDialogListener");
        }
    }

    public interface OverwriteDialogListener {
        void onOverwriteYes();
    }
}