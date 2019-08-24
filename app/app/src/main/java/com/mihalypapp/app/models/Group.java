package com.mihalypapp.app.models;

public class Group {
    private int id;
    private String type;
    private String teacherName;
    private String date;

    public Group(int id, String type, String teacherName, String date) {
        this.id = id;
        this.type = type;
        this.teacherName = teacherName;
        this.date = date;
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
        return date;
    }
}
