package com.mihalypapp.app.models;

public class Group {
    private int id;
    private String type;
    private String teacherName;
    private String year;
    private int imageResource;
    private int size;
    private int teacherId;

    public Group(int id, String type, String teacherName, String year, int imageResource) {
        this.id = id;
        this.type = type;
        this.teacherName = teacherName;
        this.year = year;
        this.imageResource = imageResource;
    }

    public Group(int id, String type, String teacherName, int size, String year) {
        this.id = id;
        this.type = type;
        this.teacherName = teacherName;
        this.year = year;
        this.size = size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Group(int id, String type, String teacherName, String year) {
        this.id = id;
        this.type = type;
        this.teacherName = teacherName;
        this.year = year;
    }

    public Group(int id, String type, String year, int imageResource) {
        this.id = id;
        this.type = type;
        this.year = year;
        this.imageResource = imageResource;
    }

    public int getSize() {
        return size;
    }

    public int getId() {
        return id;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public String getType() {
        return type;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getYear() {
        return year;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}
