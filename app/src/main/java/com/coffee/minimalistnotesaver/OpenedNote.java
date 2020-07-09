package com.coffee.minimalistnotesaver;

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
import androidx.fragment.app.FragmentManager;

import com.coffee.minimalistnotesaver.DialogFragment.WarnNoteSave;
import com.coffee.minimalistnotesaver.Model.NoteModel;
import com.coffee.minimalistnotesaver.databinding.ActivityOpenedNoteBinding;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class OpenedNote extends AppCompatActivity implements NoteTaskFragment.noteTaskCall {
    private static final String TAG = "OpenedNote";
    public ActivityOpenedNoteBinding binding;
    public static String noteTitle;
    public int textLength;
    private boolean isSaved;
    private Handler mHandler;
    String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOpenedNoteBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        isSaved = false;
        if (getIntent().hasExtra("noteTitle")) {
            noteTitle = getIntent().getStringExtra("noteTitle");
            Objects.requireNonNull(getSupportActionBar()).setTitle(noteTitle.replace(".txt", ""));
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mHandler = new Handler();

        startUpdateText();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note, menu);
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

    public void saveNote(MenuItem mi) {
        if (TextUtils.isEmpty(binding.openNoteText.getText())) {
            Toast.makeText(getApplicationContext(), "Cannot Save Empty Note", Toast.LENGTH_SHORT).show();
        } else {
            Save(noteTitle);
        }
    }

    private void startUpdateText() {
        FragmentManager fm = getSupportFragmentManager();
        NoteTaskFragment fragment = (NoteTaskFragment) fm.findFragmentByTag("task_fragment");
        Bundle title = new Bundle();
        title.putString("noteTitle", getIntent().getStringExtra("noteTitle"));
        if (fragment == null) {
            fragment = new NoteTaskFragment();
            fragment.setArguments(title);
            fm.beginTransaction().add(fragment, "task_fragment").commit();
        }
    }

    @Override
    public void onPostExecute(String s) {
        binding.openNoteText.setText(NoteTaskFragment.noteText);
        textLength = NoteTaskFragment.noteText.length();
    }

    public void Save(String fileName) {
        Thread save = new Thread(() -> {
            Log.d(TAG, "Save: running");
            try {
                OutputStreamWriter out = new OutputStreamWriter(openFileOutput(fileName, 0));
                final String notetext = binding.openNoteText.getText().toString();
                out.write(notetext);
                out.flush();
                out.close();

                mHandler.post(() -> {
                    isSaved = true;
                    Toast.makeText(this, "Note Saved!", Toast.LENGTH_SHORT).show();
                });
            } catch (Throwable t) {
                mHandler.post(() -> {
                    Toast.makeText(this, "Error Saving File. Please report if error persists.", Toast.LENGTH_LONG).show();
                    isSaved = false;
                    Log.d(TAG, "Save Exception: " + t.toString());
                });
            }
        });
        save.start();
    }

    @Override
    public void onBackPressed() {
        if (textLength != binding.openNoteText.getText().length() && !isSaved) {
            new WarnNoteSave().show(getSupportFragmentManager(), "Warn_Note_Dialog");
        } else if (isSaved) {
            int i = -1;
            ArrayList<NoteModel> tempArray = new ArrayList<>();
            for (NoteModel noteModel : NotesActivity.noteList) {
                i++;
                if (noteModel.getNoteTitle().equals(noteTitle.replace(".txt", ""))) {
                    tempArray.add(new NoteModel(noteModel.getId(), noteModel.getNoteTitle(), "Last Modified: " + currentDateTimeString));
                    NotesActivity.noteList.set(i, tempArray.get(0));
                    tempArray.clear();
                    NotesActivity.saveData();
                    //NotesActivity.mAdapter.notifyDataSetChanged();
                }
            }
            finish();
        } else {
            finish();
        }

    }
}
