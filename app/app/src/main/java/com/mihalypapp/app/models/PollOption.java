package com.mihalypapp.app.models;

import androidx.annotation.NonNull;

public class PollOption {
    private int optionId;
    private int pollId;
    private String option;

    public PollOption(int optionId, int pollId, String option) {
        this.optionId = optionId;
        this.pollId = pollId;
        this.option = option;
    }

    public int getOptionId() {
        return optionId;
    }

    public void setOptionId(int optionId) {
        this.optionId = optionId;
    }

    public int getPollId() {
        return pollId;
    }

    public void setPollId(int pollId) {
        this.pollId = pollId;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    @NonNull
    @Override
    public String toString() {
        return option;
    }
}
