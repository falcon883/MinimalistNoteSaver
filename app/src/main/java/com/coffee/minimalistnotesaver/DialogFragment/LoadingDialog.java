package com.coffee.minimalistnotesaver.DialogFragment;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.coffee.minimalistnotesaver.NotesActivity;
import com.coffee.minimalistnotesaver.R;
import com.coffee.minimalistnotesaver.databinding.LoadingbarBinding;

public class LoadingDialog extends DialogFragment implements NotesActivity.ProgressUpdateCall {

    private LoadingbarBinding binding;
    private AlertDialog loadingDialog;

    @Override
    public void onUpdateProgress(int p, int f) {
        binding.fileProg.setText(getString(R.string.file_exported, p, f));
    }

    @Override
    public void onDeleteUpdateProgress(int p, int f) {
        binding.fileProg.setText(getString(R.string.file_deleted, p, f));
    }

    @Override
    public void onTaskDone() {
        binding.closeDialog.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        loadingDialog = new AlertDialog.Builder(getActivity()).create();
        return loadingDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LoadingbarBinding.inflate(getLayoutInflater());
        binding.closeDialog.setOnClickListener(v -> loadingDialog.dismiss());

        if (NotesActivity.isDeleteorExport) {
            binding.avi.setIndicator("PacmanIndicator");
        } else {
            binding.avi.setIndicator("BallScaleMultipleIndicator");
        }
        Log.d("LoadingDialog", "onCreateView: Called");
        if (NotesActivity.isTaskDone) {
            binding.closeDialog.setVisibility(View.VISIBLE);
        }
        View dview = binding.getRoot();
        setCancelable(false);
        loadingDialog.setView(dview);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDetach() {
        NotesActivity.progressUpdateCall = null;
        super.onDetach();
    }
}