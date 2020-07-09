package com.coffee.minimalistnotesaver.SharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.coffee.minimalistnotesaver.R;

public class SaveNoteText {

    private static final String EDIT1 = "edit1";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SaveNoteText(Context context) {
        // Sharedpref file name
        final String PREF_NAME = context.getString(R.string.unsavedNote);
        // Shared pref mode
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getNoteText() {
        return pref.getString(EDIT1, "");
    }

    public void setNoteText(String text) {
        editor = pref.edit();
        editor.putString(EDIT1, text);
        editor.apply();
    }

    public void clearNoteText() {
        editor = pref.edit();
        editor.clear();
        editor.apply();
    }
}