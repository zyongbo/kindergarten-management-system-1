package com.mihalypapp.app.models;

public class Child {
    private int id;
    private int imageResource;
    private String name;
    private String groupType;
    private String parentName;
    private String parentEmail;

    public Child(int id, int imageResource, String name, String groupType, String parentName, String parentEmail) {
        this.id = id;
        this.imageResource = imageResource;
        this.name = name;
        this.groupType = groupType;
        this.parentName = parentName;
        this.parentEmail = parentEmail;
    }

    public Child(int imageResource, String name, String groupType, String parentName, String parentEmail) {
        this.imageResource = imageResource;
        this.name = name;
        this.groupType = groupType;
        this.parentName = parentName;
        this.parentEmail = parentEmail;
    }

    public Child(int id, int imageResource, String name, String groupType) {
        this.id = id;
        this.imageResource = imageResource;
        this.name = name;
        this.groupType = groupType;
    }

    public int getImageResource() {
        return imageResource;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGroupType() {
        return groupType;
    }

    public String getParentName() {
        return parentName;
    }

    public String getParentEmail() {
        return parentEmail;
    }
}
