package com.coffee.minimalistnotesaver.DialogFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.coffee.minimalistnotesaver.R;

import java.util.Objects;

public class WarnNoteSave extends DialogFragment {

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.AlertDialogCustom)
                .setTitle("Note not saved.")
                .setMessage("All the content will be lost if you don't save. Do you still want to exit ?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> getActivity().finish())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create();
    }
}