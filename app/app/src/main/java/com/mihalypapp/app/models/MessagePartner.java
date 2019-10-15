package com.mihalypapp.app.models;

public class MessagePartner {
    private int partnerId;
    private String partnerName;
    private int imageResource;
    private String datetime;

    public MessagePartner(int partnerId, String receiverName, String datetime, int imageResource) {
        this.partnerName = receiverName;
        this.partnerId = partnerId;
        this.imageResource = imageResource;
        this.datetime = datetime;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public String getDatetime() {
        return datetime;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public int getImageResource() {
        return imageResource;
    }
}
