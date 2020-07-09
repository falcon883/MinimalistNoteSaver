package com.coffee.minimalistnotesaver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.coffee.minimalistnotesaver.DialogFragment.OverwriteDialog;
import com.coffee.minimalistnotesaver.DialogFragment.SavePromptDialog;
import com.coffee.minimalistnotesaver.Model.NoteModel;
import com.coffee.minimalistnotesaver.SharedPreferences.SaveNoteText;
import com.coffee.minimalistnotesaver.databinding.ActivityNewNoteBinding;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

public class NewNote extends AppCompatActivity implements OverwriteDialog.OverwriteDialogListener,
        SavePromptDialog.SavePromptListener {

    public static final int REQUEST_CODE_NOTES = 1;
    public static final String EXTRA_NOTES = "extraNotes";
    private static final String TAG = "NewNote";
    private ActivityNewNoteBinding binding;
    SharedPreferences sharedPreferences;
    private static final String NOTE = "noteList";
    private static final String SHARED_PREFS = "sharedPreferences";
    private ArrayList<NoteModel> noteList = new ArrayList<>(NotesActivity.noteList);
    private ArrayList<Long> noteIds = new ArrayList<>();
    public ArrayList<String> fileNames = new ArrayList<>();
    String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
    String nName;
    boolean newAdded;
    OutputStreamWriter out;
    Handler mHandler;
    SaveNoteText saveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewNoteBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        if (savedInstanceState != null) {
            nName = savedInstanceState.getString("noteName");
        }

        for (NoteModel note : noteList) {
            noteIds.add(note.getId());
        }
        Collections.sort(noteIds);

        mHandler = new Handler();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        saveData = new SaveNoteText(this);
        binding.newNoteText.setText(saveData.getNoteText());

        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        checkFileName();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("noteName", nName);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note, menu);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (saveData == null) {
            saveData = new SaveNoteText(this);
            saveData.setNoteText(binding.newNoteText.getText() + "");
            binding.newNoteText.setText("");
        } else {
            saveData.setNoteText(binding.newNoteText.getText() + "");
            binding.newNoteText.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (saveData == null) {
            saveData = new SaveNoteText(this);
            saveData.setNoteText(binding.newNoteText.getText() + "");
            binding.newNoteText.setText("");
        } else {
            saveData.setNoteText(binding.newNoteText.getText() + "");
            binding.newNoteText.setText("");
        }
    }

    public void saveNote(MenuItem mi) {
        if (TextUtils.isEmpty(binding.newNoteText.getText())) {
            Toast.makeText(getApplicationContext(), "Cannot save empty note", Toast.LENGTH_SHORT).show();
        } else {
            new SavePromptDialog().show(getSupportFragmentManager(), "SavePromptDialog");
        }
    }

    @Override
    public void onSaveYes(String noteName) {
        String extension = ".txt";
        Save(noteName + extension);
    }

    public void Save(String fileName) {
        long Cid = 0;
        if (noteIds.size() != 0) {
            Cid = noteIds.get(noteIds.size() - 1);
        }
        long unique = Cid + 1;

        Thread save = new Thread(() -> {
            Log.d(TAG, "NewNote: Save(): running");

            try {
                final String notetext = binding.newNoteText.getText().toString();
                if (fileNames.contains(fileName)) {
                    nName = fileName;
                    mHandler.post(() -> new OverwriteDialog().show(getSupportFragmentManager(), "OverwriteDialog"));
                } else {
                    out = new OutputStreamWriter(openFileOutput(fileName, 0));
                    out.write(notetext);
                    out.flush();
                    out.close();
                    noteList.add(new NoteModel(unique, fileName.replace(".txt", ""), "Last Modified: " + currentDateTimeString));
                    saveData();
                    newAdded = true;
                    mHandler.post(() -> {
                        Toast.makeText(this, "Note Saved!", Toast.LENGTH_SHORT).show();
                        binding.newNoteText.setText("");
                        saveData.clearNoteText();
                    });
                    reActivity();
                }
            } catch (Throwable t) {
                mHandler.post(() -> {
                    Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Save Exception: " + t.toString());
                });
            }
        });
        save.start();
    }

    @Override
    public void onOverwriteYes() {
        try {
            final String notetext = binding.newNoteText.getText().toString();
            out = new OutputStreamWriter(openFileOutput(nName, 0));
            out.write(notetext);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        newAdded = false;
        Toast.makeText(this, "File Overwritten", Toast.LENGTH_SHORT).show();
        binding.newNoteText.setText("");
        saveData.clearNoteText();
        reActivity();
    }

    private void saveData() {
        Thread savePref = new Thread(() -> {
            Log.d(TAG, "saveData(): running");
            sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(noteList);
            editor.putString(NOTE, json);
            editor.apply();
        });
        savePref.start();

    }

    private void checkFileName() {
        Thread thread = new Thread(() -> {

            if (NotesActivity.files != null) {
                for (int i = 0; i < NotesActivity.files.length; i++) {
                    fileNames.add(NotesActivity.files[i].getName());
                    Log.d(TAG, "Save: " + fileNames.size());
                }
                Log.d(TAG, "onCreate: " + fileNames.toString());
            }
        });
        thread.start();
    }

    private void reActivity() {
        Intent resultIntent = new Intent(getApplicationContext(), NotesActivity.class);
        resultIntent.putExtra(EXTRA_NOTES, newAdded);
        setResult(RESULT_OK, resultIntent);
        startActivityForResult(resultIntent, REQUEST_CODE_NOTES);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        reActivity();
    }
}