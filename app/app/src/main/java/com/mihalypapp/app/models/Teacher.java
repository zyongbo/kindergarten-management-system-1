package com.mihalypapp.app.models;

import androidx.annotation.NonNull;

public class Teacher {
    private int id;
    private String name;
    private String email;

    public Teacher(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }
}
