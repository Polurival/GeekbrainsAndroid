package com.github.polurival.geocoderapp;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Polurival
 * on 10.09.2016.
 */
public class Marker {

    private String address;
    private LatLng latLng;

    public Marker(LatLng latLng, String address) {
        this.latLng = latLng;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
