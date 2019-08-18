package com.mihalypapp.app.models;

public class ItemGroupCard {
    private int id;
    private int imageResource;
    private String teacherName;
    private String year;
    private String type;

    public ItemGroupCard(int id, int imageResource, String teacherName, String type, String year) {
        this.id = id;
        this.imageResource = imageResource;
        this.teacherName = teacherName;
        this.year = year;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getYear() {
        return year;
    }

    public String getType() {
        return type;
    }
}
