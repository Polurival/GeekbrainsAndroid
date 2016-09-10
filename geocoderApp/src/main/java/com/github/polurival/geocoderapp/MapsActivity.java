package com.github.polurival.geocoderapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final static String TAG = "MapsActivity";
    private static final int ACCESS_LOCATION_PERMISSIONS_REQUEST = 0;
    private Resources res;

    private GoogleMap mMap;
    private LocationManager mLocManager;
    private Geocoder geo;

    private EditText addressET;
    private TextView coordinatesTV;
    private Button addMarkerByLocBtn;

    private Marker marker;

    private DBHelper dbHelper;
    Cursor markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        checkPermissions();

        geo = new Geocoder(this);
        mLocManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        res = getResources();
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            setMapLocationEnabled();
        }
    }

    private void setMapLocationEnabled() {
        if (ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void checkPermissions() {
        Log.d(TAG, "checkPermissions");
        if (ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_LOCATION_PERMISSIONS_REQUEST);
        }
    }

    private void initViews() {
        addressET = (EditText) findViewById(R.id.addressET);
        coordinatesTV = (TextView) findViewById(R.id.coordinatesTV);
        addMarkerByLocBtn = (Button) findViewById(R.id.addMarkerByLocBtn);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        mMap = googleMap;
        addMarkersFromDB();

        setMapLocationEnabled();
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationButtonClickListener(myLocationBtnClickListener);
        mMap.setOnMarkerClickListener(onMarkerClickListener);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void addMarkersFromDB() {
        dbHelper = new DBHelper(this);
        markers = dbHelper.selectMarkers();

        double latitude;
        double longitude;
        String address;
        for (markers.moveToFirst(); !markers.isAfterLast(); markers.moveToNext()) {
            latitude = markers.getDouble(markers.getColumnIndex(DBHelper.COLUMN_LATITUDE));
            longitude = markers.getDouble(markers.getColumnIndex(DBHelper.COLUMN_LONGITUDE));
            address = markers.getString(markers.getColumnIndex(DBHelper.COLUMN_ADDRESS));
            mMap.addMarker(
                    new MarkerOptions().position(new LatLng(latitude, longitude)).title(address));
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.findLocByAddressBtn:
                marker = getMarker();
                if (marker != null) {
                    hideKeyboard();
                    commonActionsForLatLng();
                }
                break;
            case R.id.addMarkerByLocBtn:
                if (marker != null) {
                    hideKeyboard();
                    insertMarker();
                    addMarkerByLocBtn.setEnabled(false);
                } else {
                    Log.d(TAG, res.getString(R.string.null_address_list));
                }
        }
    }

    private void setCoordinates(double latitude, double longitude) {
        coordinatesTV.setText(
                String.format(res.getString(R.string.coordinates), latitude, longitude));
    }

    @Nullable
    private Marker getMarker() {
        List<Address> addressList = null;
        String locAddress = addressET.getText().toString();
        try {
            addressList = geo.getFromLocationName(locAddress, 3);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isListEmpty(addressList)) return null;
        Address a = addressList.get(0);
        LatLng latLng = new LatLng(a.getLatitude(), a.getLongitude());

        return new Marker(latLng, locAddress);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void moveCamera(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getLatLng(), 15F));
    }

    private void insertMarker() {
        Cursor c = dbHelper.selectMarker(marker.getLatLng().hashCode());
        if (!c.moveToFirst()) {
            dbHelper.insertMarker(marker);
            mMap.addMarker(new MarkerOptions()
                    .position(marker.getLatLng())
                    .title(marker.getAddress()));
        } else {
            Log.d(TAG, res.getString(R.string.marker_already_exist));
        }
    }

    private final GoogleMap.OnMyLocationButtonClickListener myLocationBtnClickListener
            = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                return false;
            }

            final Location myLoc = mLocManager.getLastKnownLocation(
                    LocationManager.PASSIVE_PROVIDER);
            if (myLoc != null) {

                List<Address> myAddressList = null;
                try {
                    myAddressList =
                            geo.getFromLocation(myLoc.getLatitude(), myLoc.getLongitude(), 3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isListEmpty(myAddressList)) return false;

                Address a = myAddressList.get(0);
                StringBuilder builder = new StringBuilder();
                final String sep = ", ";
                builder.append(a.getCountryName()).append(sep)
                        .append(a.getAdminArea()).append(sep)
                        .append(a.getThoroughfare()).append(sep)
                        .append(a.getSubThoroughfare());

                LatLng myLatLng = new LatLng(myLoc.getLatitude(), myLoc.getLongitude());
                marker = new Marker(myLatLng, builder.toString());
                addressET.setText(marker.getAddress());
                commonActionsForLatLng();
            }

            return true;
        }
    };

    private boolean isListEmpty(List<Address> myAddressList) {
        if (myAddressList == null || myAddressList.isEmpty()) {
            Log.d(TAG, res.getString(R.string.null_address_list));
            return true;
        }
        return false;
    }

    private void commonActionsForLatLng() {
        setCoordinates(marker.getLatLng().latitude, marker.getLatLng().longitude);
        moveCamera(marker);
        addMarkerByLocBtn.setEnabled(true);
    }

    private final GoogleMap.OnMarkerClickListener onMarkerClickListener =
            new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(com.google.android.gms.maps.model.Marker m) {
                    mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(m.getPosition(), 15F));

                    addressET.setText(m.getTitle());
                    setCoordinates(m.getPosition().latitude, m.getPosition().longitude);
                    marker = new Marker(
                            new LatLng(m.getPosition().latitude, m.getPosition().longitude),
                            m.getTitle());
                    return true;
                }
            };
}
