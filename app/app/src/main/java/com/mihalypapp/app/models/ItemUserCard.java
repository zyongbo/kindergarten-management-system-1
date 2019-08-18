package com.mihalypapp.app.models;

public class ItemUserCard {
    private int imageResource;
    private String name;
    private String email;

    public ItemUserCard(int imageResource, String name, String email) {
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
