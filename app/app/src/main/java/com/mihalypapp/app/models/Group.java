package com.mihalypapp.app.models;

public class Group {
    private int id;
    private String type;
    private String teacherName;
    private String year;

    public Group(int id, String type, String teacherName, String year) {
        this.id = id;
        this.type = type;
        this.teacherName = teacherName;
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getDate() {
        return year;
    }
}
