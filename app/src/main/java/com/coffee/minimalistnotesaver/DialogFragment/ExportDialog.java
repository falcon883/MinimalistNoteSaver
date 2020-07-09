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

public class ExportDialog extends DialogFragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof ExportDialogListener)) {
            throw new ClassCastException(context.toString() + "must implement ExportDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.AlertDialogCustom)
                .setTitle("Are you Sure ?")
                .setMessage("Do you want to export the selected files ?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> ((ExportDialogListener) getActivity()).onExportYes())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create();
    }

    public interface ExportDialogListener {
        void onExportYes();
    }
}