package com.mihalypapp.app.models;

import androidx.annotation.NonNull;

public class Child {
    private int id;
    private int imageResource;
    private String name;
    private String groupType;
    private String parentName;
    private String parentEmail;
    private int absences;
    private String isCheckedToday;
    private int groupId;

    public Child(int id, int groupId) {
        this.id = id;
        this.groupId = groupId;
    }

    public int getGroupId() {
        return groupId;
    }

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

    public Child(int id, int imageResource, String name, int absences) {
        this.id = id;
        this.imageResource = imageResource;
        this.name = name;
        this.absences = absences;
    }

    public Child(int id, int imageResource, String name, int absences, String isCheckedToday) {
        this.id = id;
        this.imageResource = imageResource;
        this.name = name;
        this.absences = absences;
        this.isCheckedToday = isCheckedToday;
    }

    public Child(int id, int imageResource, String name, String groupType, int absences) {
        this.id = id;
        this.imageResource = imageResource;
        this.name = name;
        this.groupType = groupType;
        this.absences = absences;
    }

    public void setIsCheckedToday(String isCheckedToday) {
        this.isCheckedToday = isCheckedToday;
    }

    public String getIsCheckedToday() {
        return isCheckedToday;
    }

    public int getAbsences() {
        return absences;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGroupType() {
        if (this.groupType.equals("null"))
            return "";
        return groupType;
    }

    public String getParentName() {
        return parentName;
    }

    public String getParentEmail() {
        return parentEmail;
    }

    @NonNull
    @Override
    public String toString() {
        return getName() + " (" + Integer.valueOf(getAbsences()).toString() + ")";
    }
}
