package com.mihalypapp.app.models;

public class Document {
    private int id;
    private String description;
    private String name;
    private String role;
    private String date;

    public Document(int id, String description, String name, String role, String date) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.role = role;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

