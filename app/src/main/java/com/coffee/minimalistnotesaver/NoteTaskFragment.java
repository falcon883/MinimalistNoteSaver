package com.coffee.minimalistnotesaver;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class NoteTaskFragment extends Fragment {

    static String noteText;
    private static String noteTitle;
    private static noteTaskCall noteTask;

    interface noteTaskCall {
        void onPostExecute(String s);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        noteTitle = getArguments().getString("noteTitle");
        new NoteTask().execute();
    }

    private static class NoteTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            if (noteTitle != null) {
                for (File value : NotesActivity.files) {
                    if (value.getName().equals(noteTitle)) {
                        Log.d("TAG", "doInBackground: " + noteTitle);
                        StringBuilder text = new StringBuilder();
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(value));
                            String line;

                            while ((line = br.readLine()) != null) {
                                text.append(line);
                                text.append('\n');
                            }
                            br.close();
                            noteText = text.toString();
                        } catch (IOException e) {
                            Log.d("File Close", "No. Reason:" + e);
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            noteTask.onPostExecute(s);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        noteTask = (NoteTaskFragment.noteTaskCall) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        noteTask = null;
    }
}
