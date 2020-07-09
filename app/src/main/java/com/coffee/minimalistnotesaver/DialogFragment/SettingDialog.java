package com.coffee.minimalistnotesaver.DialogFragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.coffee.minimalistnotesaver.R;

import java.util.Objects;

public class SettingDialog extends DialogFragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof SettingDialogListener)) {
            throw new ClassCastException(context.toString() + "must implement SettingDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.AlertDialogCustom)
                .setTitle("Need Permissions")
                .setMessage("This app needs permission to use this feature. You can grant them in app settings.")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    ((SettingDialogListener) getActivity()).onPermissionDenied();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create();
    }

    public interface SettingDialogListener {
        void onPermissionDenied();
    }
}