package com.mihalypapp.app.models;

public class Poll {
    private int pollID;
    private int groupID;
    private String question;
    private String date;
    private String status;
    private String children;

    public Poll(int pollID, int groupID, String question, String date, String status) {
        this.pollID = pollID;
        this.groupID = groupID;
        this.question = question;
        this.date = date;
        this.status = status;
    }

    public int getPollID() {
        return pollID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPollID(int pollID) {
        this.pollID = pollID;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getChildren() {
        return children;
    }

    public void setChildren(String children) {
        this.children = children;
    }
}
