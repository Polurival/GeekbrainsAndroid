package com.github.polurival.smsapp;

/**
 * Created by Polurival
 * on 02.10.2016.
 * <p/>
 * http://stackoverflow.com/a/4023737/5349748
 */
public class SmsEntity {

    private String address;
    private String body;

    public SmsEntity(String sender, String message) {
        this.address = sender;
        this.body = message;
    }

    public String getAddress() {
        return address;
    }

    public String getBody() {
        return body;
    }
}
