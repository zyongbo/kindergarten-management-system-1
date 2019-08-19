package com.mihalypapp.app.models;

public class UserCard {
    private int imageResource;
    private String name;
    private String email;

    public UserCard(int imageResource, String name, String email) {
        this.imageResource = imageResource;
        this.name = name;
        this.email = email;
    }

    public int getImageResource() {
        return imageResource;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
