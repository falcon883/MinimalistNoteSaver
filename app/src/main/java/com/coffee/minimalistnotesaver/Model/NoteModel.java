package com.coffee.minimalistnotesaver.Model;

public class NoteModel {

    private final long id;
    private String noteTitle, noteDate;

    public NoteModel(long id, String noteTitle, String noteDate) {
        this.id = id;
        this.noteTitle = noteTitle;
        this.noteDate = noteDate;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public String getNoteDate() {
        return noteDate;
    }

    public long getId() {
        return id;
    }
}