package com.coffee.minimalistnotesaver;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.coffee.minimalistnotesaver.Adapter.NoteAdapter;
import com.coffee.minimalistnotesaver.DialogFragment.DeleteDialog;
import com.coffee.minimalistnotesaver.DialogFragment.ExportDialog;
import com.coffee.minimalistnotesaver.DialogFragment.LoadingDialog;
import com.coffee.minimalistnotesaver.DialogFragment.SettingDialog;
import com.coffee.minimalistnotesaver.Model.NoteModel;
import com.coffee.minimalistnotesaver.databinding.ActivityNotesBinding;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class NotesActivity extends AppCompatActivity
        implements NoteAdapter.MyCallBack, DeleteDialog.DeleteDialogListener,
        ExportDialog.ExportDialogListener, SettingDialog.SettingDialogListener, DeleteFilesFragment.onItemChanged {

    private ActivityNotesBinding binding;
    private static final String NOTE = "noteList";
    private static final String SHARED_PREFS = "sharedPreferences";
    public static SharedPreferences sharedPreferences;
    public static ArrayList<NoteModel> noteList = new ArrayList<>();
    RecyclerView.LayoutManager mLayoutManager;
    public static FastScrollRecyclerView notesList;
    public TextView emptyView;
    RecyclerView.Adapter mAdapter;
    static RecyclerViewDragDropManager dragDropManager;
    public static File[] files;
    boolean isInActionMode;
    public static boolean isDeleteorExport, isTaskDone, isPerformingTask;
    private Fragment ExportFrag, DeleteFrag;
    static String rootPath;
    public static ActionMode actionMode;
    private Handler mHandler;
    public static ProgressUpdateCall progressUpdateCall;
    private String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());

    /**
     * NoteAdapter callback method for recyclerview item longclick listener.
     */
    @Override
    public void onLongRecyclerClick(int position) {
        actionMode = startSupportActionMode(actionModeCallbacks);
    }

    public interface ProgressUpdateCall {
        void onUpdateProgress(int p, int f);

        void onDeleteUpdateProgress(int p, int f);

        void onTaskDone();
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        if (fragment instanceof LoadingDialog) {
            try {
                progressUpdateCall = (ProgressUpdateCall) fragment;
            } catch (ClassCastException e) {
                throw new ClassCastException(fragment.toString() + "must implement ProgressUpdateCall");
            }
            super.onAttachFragment(fragment);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotesBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        String optionalPath = "/MinimalistNoteSaver/SavedNotes/";
        String state = Environment.getExternalStorageState();

        // Make sure it's available
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            rootPath = getApplicationContext().getExternalFilesDir(optionalPath).getAbsolutePath();
        } else {
            // Load another directory, probably local memory
            Toast.makeText(getApplicationContext(), "Storage Not Accessible. Files Cannot Be Exported. Please Check If Your Storage Is Mounted", Toast.LENGTH_LONG).show();
        }

        if (savedInstanceState != null && savedInstanceState.getBoolean("ActionMode", false)) {
            Log.d("ActionMode", "savedInstanceState: Called");
            actionMode = startSupportActionMode(actionModeCallbacks);
            setRecyclerView();
            actionMode.setTitle("Selected: " + NoteAdapter.selectedItems.size());
            mLayoutManager.onRestoreInstanceState(savedInstanceState.getParcelable("RecyclerState"));
            mHandler = new Handler();
            if (savedInstanceState.getBoolean("PerformingTask", false)) {
                ExportFrag = getSupportFragmentManager().findFragmentById(android.R.id.content);
                DeleteFrag = getSupportFragmentManager().findFragmentByTag("DeleteFrag");
            }
        } else {
            sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            getFiles(this);
            loadData();
            setRecyclerView();
            mHandler = new Handler();
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("ActionMode", isInActionMode);
        outState.putBoolean("PerformingTask", isPerformingTask);
        outState.putParcelable("RecyclerState", mLayoutManager.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    /**
     * Custom Actionbar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * ActionBar Menu Item Onlick
     * before starting activity check if files[] array is not empty
     */
    public void openNoteMenu(MenuItem mi) {
        getFiles(this);
        Intent intent = new Intent(getApplicationContext(), NewNote.class);
        startActivity(intent);
        finish();
    }

    public void openAbout(MenuItem mi) {
        Intent intent = new Intent(getApplicationContext(), About.class);
        startActivity(intent);
    }

    /**
     * Check if the user created a new note or not.
     * If user created a new note then recreate the activity to display new note.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NewNote.REQUEST_CODE_NOTES) {
            if (resultCode == RESULT_OK) {
                boolean check = data.getBooleanExtra(NewNote.EXTRA_NOTES, false);
                if (check) {
                    recreate();
                }
            }
        }
    }

    /**
     * Contextual ActionMode for interacting with notes.
     */
    public ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.d("TAG", "onCreateActionMode: Called");
            NoteAdapter.multiSelect = true;
            isInActionMode = true;
            mode.getMenuInflater().inflate(R.menu.cab_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            Log.d("TAG", "onPrepareActionMode: Called");
            NoteAdapter.multiSelect = true;
            menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.all_selection).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.findItem(R.id.action_copy_text).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menu.findItem(R.id.action_export).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    new DeleteDialog().show(getSupportFragmentManager(), "Delete_Confirm_Dialog");
                    return true;

                case R.id.action_export:
                    Log.d("TAG", "onActionItemClicked: Export Called");
                    Dexter.withActivity(NotesActivity.this)
                            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    new ExportDialog().show(getSupportFragmentManager(), "Export_Conform_Dialog");
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    if (response.isPermanentlyDenied()) {
                                        new SettingDialog().show(getSupportFragmentManager(), "Setting_Dialog");
                                    }
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                    Toast.makeText(getApplicationContext(), "Permission Needed to Store Notes Locally", Toast.LENGTH_LONG).show();
                                    token.continuePermissionRequest();
                                }
                            })
                            .withErrorListener(error -> {
                                Toast.makeText(getApplicationContext(), "Error occurred! " + error.toString(), Toast.LENGTH_SHORT).show();
                                Log.d("TAG", "onError: " + error.toString());
                            }).check();

                    return true;

                case R.id.action_copy_text:
                    if (NoteAdapter.selectedItems.size() == 1) {
                        Log.d("TAG", "onActionItemClicked: Copy Text Called");

                        for (File file : files) {
                            if (file.getName().equals(NoteAdapter.selectedItems.get(0).getNoteTitle() + ".txt")) {
                                Thread readFile = new Thread(() -> {
                                    try {
                                        readFiles(file);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                                readFile.start();
                            }
                        }
                        mode.finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Select only one note to copy", Toast.LENGTH_SHORT).show();
                    }
                    return true;

                case R.id.all_selection:
                    if (item.getTitle().equals(getApplicationContext().getResources().getString(R.string.select_all))) {
                        NoteAdapter.selectedItems.clear();
                        NoteAdapter.selectItemId.clear();
                        NoteAdapter.selectedItems.addAll(noteList);
                        for (NoteModel model : NoteAdapter.selectedItems) {
                            NoteAdapter.selectItemId.add(model.getId());
                        }
                        item.setTitle(getApplicationContext().getResources().getString(R.string.deselect_all));
                        mode.setTitle("Selected: " + NoteAdapter.selectedItems.size());
                        mAdapter.notifyDataSetChanged();
                    } else {
                        NoteAdapter.selectedItems.removeAll(noteList);
                        NoteAdapter.selectItemId.clear();
                        item.setTitle(getApplicationContext().getResources().getString(R.string.select_all));
                        mode.setTitle("Selected: " + NoteAdapter.selectedItems.size());
                        mAdapter.notifyDataSetChanged();
                    }
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            NoteAdapter.multiSelect = false;
            Log.d("TAG", "onDestroyActionMode: " + NoteAdapter.selectedItems.toString() + NoteAdapter.selectItemId.toString());
            NoteAdapter.selectedItems.clear();
            NoteAdapter.selectItemId.clear();
            mAdapter.notifyDataSetChanged();
            isInActionMode = false;
            actionMode = null;
        }
    };

    /**
     * Export all the files to phones internal storage.
     */
    @Override
    public void onExportYes() {
        isDeleteorExport = false;
        isPerformingTask = true;
        isTaskDone = false;
        showLoadingBar();
        ExportFrag = new ExportFilesFragment();
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, ExportFrag).commit();
    }

    private void showLoadingBar() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("loadingDialog");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.show(fragmentTransaction, "loadingDialog");
    }

    /**
     * Delete all the selected note text files from application directory.
     */
    @Override
    public void onDeleteYes() {
        isDeleteorExport = true;
        isPerformingTask = true;
        isTaskDone = false;
        showLoadingBar();
        DeleteFrag = new DeleteFilesFragment();
        getSupportFragmentManager().beginTransaction().add(DeleteFrag, "DeleteFrag").commit();
    }

    /**
     * navigate user to app settings if permission is denied permanently
     */
    @Override
    public void onPermissionDenied() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    /**
     * Initialize RecyclerView.........
     */
    private void setRecyclerView() {
        emptyView = binding.emptyView;
        notesList = binding.notesList;

        dragDropManager = new RecyclerViewDragDropManager();

        mAdapter = new NoteAdapter(this, noteList, this, notesList, emptyView);
        RecyclerView.Adapter wrappedAdapter = dragDropManager.createWrappedAdapter(mAdapter);
        notesList.setAdapter(wrappedAdapter);
        mLayoutManager = new LinearLayoutManager(this);
        notesList.setLayoutManager(mLayoutManager);
        notesList.setHasFixedSize(true);
        ((SimpleItemAnimator) Objects.requireNonNull(notesList.getItemAnimator())).setSupportsChangeAnimations(false);

        if (emptyView != null && notesList.getAdapter() != null) {
            boolean emptyViewVisible = notesList.getAdapter().getItemCount() == 0;
            emptyView.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
            notesList.setVisibility(emptyViewVisible ? View.GONE : View.VISIBLE);
        }

        dragDropManager.attachRecyclerView(notesList);
        dragDropManager.setOnItemDragEventListener(new RecyclerViewDragDropManager.OnItemDragEventListener() {
            @Override
            public void onItemDragStarted(int position) {

            }

            @Override
            public void onItemDragPositionChanged(int fromPosition, int toPosition) {

            }

            @Override
            public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
                Log.d("TAG", "onItemDragFinished: " + fromPosition + " to " + toPosition);
                saveData();
            }

            @Override
            public void onItemDragMoveDistanceUpdated(int offsetX, int offsetY) {

            }
        });
    }

    /**
     * List all the text files in the application directory and store in ArrayList.
     * And Check if SharedPreferences is not null, if null then create new Arraylist for
     * RecyclerView and Save in SharedPreferences.
     */
    public void getFiles(Context context) {
        Thread getFiles = new Thread(() -> {
            String path = context.getFilesDir().getAbsolutePath();
            File dir = new File(path);
            files = dir.listFiles((dir1, name) -> name.endsWith(".txt"));
            Log.d("getFiles: files[]:", Arrays.toString(files));

            if (!sharedPreferences.contains(NOTE)) {
                Log.d("TAG", "getFiles: Saving Data");
                Open();
                saveData();
            }
        });
        getFiles.start();
    }

    /**
     * This function is only called when there are no existing SharedPreferences.
     * Or
     * Lets say if the user runs into a problem like, created note is not displayed in RecyclerView even after activity restart,
     * then it can be solved by clearing the SharedPreferences and loading files directly from app
     * directory and then save in ArrayList and SharedPreferences again.
     */
    public void Open() {
        if (files.length != 0) {
            Log.d("Open()", "Called");
            noteList.clear();
            int i = 0;
            for (File file : files) {
                String fileName = file.getName();
                if (FileExists(fileName)) {
                    FileInputStream in = null;
                    try {
                        in = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        Log.d("FileInputSteam: Open():", "Exception:" + e);
                        e.printStackTrace();
                    }
                    try {
                        assert in != null;
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("FileInputSteam: Open():", "Exception:" + e);
                    }
                    i += 1;
                    noteList.add(new NoteModel(i, file.getName().replace(".txt", ""), "Last Modified: " + currentDateTimeString));
                }
            }
            mHandler.post(() -> mAdapter.notifyDataSetChanged());
        }
    }

    /**
     * Check if the selected text file exists in the application directory or not.
     */
    public boolean FileExists(String fname) {
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    /**
     * Read the selected note text file using BufferedReader line by line and store in StringBuilder,
     * then copy the contents to clipboard.
     */
    private void readFiles(File file) throws IOException {
        StringBuilder text = new StringBuilder();

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null) {
            text.append(line);
            text.append('\n');
        }
        br.close();

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(file.getName(), text.toString());
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            mHandler.post(() -> Toast.makeText(getApplicationContext(), "Text Copied.", Toast.LENGTH_SHORT).show());
        } else {
            mHandler.post(() -> Toast.makeText(getApplicationContext(), "Error Copying.", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Save RecyclerView ArrayList in SharedPreferences in json format.
     * It is stored in SharedPreferences to maintain the note positions after drag and drop.
     */
    static void saveData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(noteList);
        editor.putString(NOTE, json);
        editor.apply();
    }

    /**
     * Get json from SharedPreferences and load in RecyclerView ArrayList.
     * If SharedPreferences is null then ArrayList is assigned default value as null.
     * If null then initialize ArrayList.
     */
    public void loadData() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(NOTE, null);
        Type type = new TypeToken<ArrayList<NoteModel>>() {
        }.getType();
        noteList = gson.fromJson(json, type);
        if (noteList == null) {
            noteList = new ArrayList<>();
            Log.d("TAG", "loadData: Open() called");
        }
    }
}