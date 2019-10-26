package com.mihalypapp.app.models;

import androidx.annotation.NonNull;

public class Child {
    private int id;
    private int imageResource;
    private String name;
    private String groupType;
    private String parentName;
    private String parentEmail;
    private String teacherName;
    private int teacherId;
    private int absences;
    private String isCheckedToday;
    private int groupId;
    private int parentId;
    private int mealSubscription;

    public Child(){

    }

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

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
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

    public int getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public String getParentEmail() {
        return parentEmail;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public void setParentEmail(String parentEmail) {
        this.parentEmail = parentEmail;
    }

    public void setAbsences(int absences) {
        this.absences = absences;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getMealSubscription() {
        return mealSubscription;
    }

    public void setMealSubscription(int mealSubscription) {
        this.mealSubscription = mealSubscription;
    }



    @NonNull
    @Override
    public String toString() {
        return getName() + " (" + Integer.valueOf(getAbsences()).toString() + ")";
    }
}
