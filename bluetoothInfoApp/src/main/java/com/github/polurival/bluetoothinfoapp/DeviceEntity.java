package com.github.polurival.bluetoothinfoapp;

/**
 * Created by Polurival
 * on 02.10.2016.
 * <p/>
 * http://stackoverflow.com/a/4023737/5349748
 */
public class DeviceEntity {

    private String address;
    private String name;

    public DeviceEntity(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }
}
