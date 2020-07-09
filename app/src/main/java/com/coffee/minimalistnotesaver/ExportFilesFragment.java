package com.coffee.minimalistnotesaver;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coffee.minimalistnotesaver.Adapter.NoteAdapter;
import com.coffee.minimalistnotesaver.Model.NoteModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.coffee.minimalistnotesaver.NotesActivity.files;

public class ExportFilesFragment extends Fragment {

    private int i;
    private boolean exported;
    private Handler mHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mHandler = new Handler();
        exportFiles();
    }

    private void exportFiles() {
        i = 0;
        ArrayList<NoteModel> tempArray = new ArrayList<>(NoteAdapter.selectedItems);
        Thread startExport = new Thread(() -> {
            for (NoteModel noteModel : tempArray) {
                for (File file : files) {
                    if (file.getName().equals(noteModel.getNoteTitle() + ".txt")) {
                        try {
                            copyNotes(file);
                            i++;
                            if (exported) {
                                mHandler.post(() -> NotesActivity.progressUpdateCall.onUpdateProgress(i, tempArray.size()));
                            } else {
                                mHandler.post(() -> NotesActivity.progressUpdateCall.onUpdateProgress(i, tempArray.size()));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            mHandler.post(() -> {
                NotesActivity.progressUpdateCall.onTaskDone();
                NotesActivity.actionMode.finish();
                NotesActivity.isTaskDone = true;
            });
        });
        Thread.UncaughtExceptionHandler h = (t, e) -> Log.d(t.getName(), "uncaughtException: " + e);
        startExport.setUncaughtExceptionHandler(h);
        startExport.start();
    }


    /**
     * Check Storage state.
     * If available then copy the selected note text file, get the phone's internal storage path,
     * create directory if doesn't exists, then copy file from application files directory to users phone internal storage.
     */
    private void copyNotes(File file) throws IOException {
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Toast.makeText(getContext(), "Storage not accessible due to some reasons. Try stopping other applications or Mount your storage correctly.", Toast.LENGTH_LONG).show();
        } else {
            // Open your local file as the input stream
            InputStream myInput = new FileInputStream(file);

            //other way of using external storage as Environment.getExternalStorageDirectory() is deprecated :)
            String rootPath = NotesActivity.rootPath;

            // extraPortion is extra part of file path
            String extraPortion = "Android/data/" + BuildConfig.APPLICATION_ID
                    + File.separator + "files" + File.separator;
            // Remove extraPortion
            rootPath = rootPath.replace(extraPortion, "");

            File dir = new File(rootPath);
            // Path to the just created empty file
            String outFileName;

            if (dir.exists()) {
                outFileName = dir + "/" + file.getName();
            } else {
                boolean dirStatus = dir.mkdirs();
                outFileName = dir + "/" + file.getName();
                Log.d("TAG", "copyNotes: mkdir" + dirStatus + "\n" + outFileName);
            }
            Log.d("PATH", "copyNotes: " + outFileName);

            // Open the empty file as the output stream
            boolean newFile = new File(outFileName).createNewFile();
            Log.d("TAG", "copyNotes: " + newFile);
            exported = newFile;
            OutputStream myOutput = new FileOutputStream(outFileName);

            // transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            // Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();

            mHandler.post(() -> Toast.makeText(getContext(), "File Copied at: " + dir.toString(), Toast.LENGTH_LONG).show());
        }
    }

    /**
     * Check if storage has readonly access.
     * &
     * Check if storage is available and not being used.
     */
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

}