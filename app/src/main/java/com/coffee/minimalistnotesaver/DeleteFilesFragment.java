package com.coffee.minimalistnotesaver;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coffee.minimalistnotesaver.Adapter.NoteAdapter;
import com.coffee.minimalistnotesaver.Model.NoteModel;

import java.io.File;
import java.util.ArrayList;

import static com.coffee.minimalistnotesaver.NotesActivity.files;
import static com.coffee.minimalistnotesaver.NotesActivity.isTaskDone;
import static com.coffee.minimalistnotesaver.NotesActivity.progressUpdateCall;

public class DeleteFilesFragment extends Fragment {

    private int i;
    private Handler mHandler;

    public interface onItemChanged {
        void notifyDataSetChanged();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof onItemChanged)) {
            throw new ClassCastException(context.toString() + "must implement onItemChanged");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mHandler = new Handler();
        deleteFiles();
    }

    /**
     * Delete all the selected note text files from application directory.
     */
    private void deleteFiles() {
        i = 0;
        ArrayList<NoteModel> switchArrayList = new ArrayList<>(NoteAdapter.selectedItems);
        Thread startDelete = new Thread(() -> {
            ArrayList<NoteModel> tempArrayList = new ArrayList<>();
            for (NoteModel noteItem : switchArrayList) {
                tempArrayList.add(noteItem);
                for (File value : files) {
                    if (value.getName().equals(noteItem.getNoteTitle() + ".txt")) {
                        boolean isDeleted = value.delete();
                        mHandler.post(() -> {
                            if (isDeleted) {
                                i++;
                                progressUpdateCall.onDeleteUpdateProgress(i, switchArrayList.size());
                            } else {
                                progressUpdateCall.onDeleteUpdateProgress(i, switchArrayList.size());
                            }
                        });
                        Log.d("TAG", "onActionItemClicked: File Deleted:" + isDeleted);
                    }
                }
            }
            NotesActivity.noteList.removeAll(tempArrayList);
            tempArrayList.clear();
            NotesActivity.saveData();
            mHandler.post(() -> {
                progressUpdateCall.onTaskDone();
                //NotesActivity.mAdapter.notifyDataSetChanged();
                ((onItemChanged) getActivity()).notifyDataSetChanged();
                NotesActivity.actionMode.finish();
                NotesActivity.isPerformingTask = false;
                isTaskDone = true;
            });
        });
        Thread.UncaughtExceptionHandler h = (t, e) -> Log.d(t.getName(), "uncaughtException: " + e);
        startDelete.setUncaughtExceptionHandler(h);
        startDelete.start();
    }
}
