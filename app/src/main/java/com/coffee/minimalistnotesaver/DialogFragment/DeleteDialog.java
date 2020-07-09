package com.coffee.minimalistnotesaver.DialogFragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.coffee.minimalistnotesaver.R;

import java.util.Objects;

public class DeleteDialog extends DialogFragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof DeleteDialogListener)) {
            throw new ClassCastException(context.toString() + " must implement DeleteDialogListener");
        }
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.AlertDialogCustom)
                .setTitle("Are you sure ?")
                .setMessage("Do you want to delete the selected files ?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> ((DeleteDialogListener) getActivity()).onDeleteYes())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create();
    }

    public interface DeleteDialogListener {
        void onDeleteYes();
    }
}