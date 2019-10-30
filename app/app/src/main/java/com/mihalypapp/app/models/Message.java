package com.mihalypapp.app.models;


public class Message {
    private String userName;
    private String message;
    private String datetime;
    private String replyToMessage;
    private int hasReply;

    public Message(String userName, String message, String datetime) {
        this.userName = userName;
        this.message = message;
        this.datetime = datetime;
    }

    public Message(String userName, String message, String datetime, String replyToMessage, int hasReply) {
        this.userName = userName;
        this.message = message;
        this.datetime = datetime;
        this.replyToMessage = replyToMessage;
        this.hasReply = hasReply;
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

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getReplyToMessage() {
        return replyToMessage;
    }

    public void setReplyToMessage(String replyToMessage) {
        this.replyToMessage = replyToMessage;
    }

    public int getHasReply() {
        return hasReply;
    }

    public void setHasReply(int hasReply) {
        this.hasReply = hasReply;
    }
}
