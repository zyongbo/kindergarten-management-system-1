package com.mihalypapp.app.models;


public class Message {
    private String userName;
    private String message;
    private String datetime;

    public Message(String userName, String message, String datetime) {
        this.userName = userName;
        this.message = message;
        this.datetime = datetime;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessage() {
        return message;
    }

    public String getDatetime() {
        return datetime;
    }
}
