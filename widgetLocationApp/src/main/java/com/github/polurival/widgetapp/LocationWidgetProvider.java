package com.github.polurival.widgetapp;

import android.Manifest;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.permissioneverywhere.PermissionEverywhere;
import com.permissioneverywhere.PermissionResponse;
import com.permissioneverywhere.PermissionResultCallback;

import java.util.List;


/**
 * Created by Polurival
 * on 23.09.2016.
 * <p/>
 * http://www.edumobile.org/android/gps-app-widget-example-in-android-programming/
 * https://github.com/kaknazaveshtakipishi/PermissionEverywhere
 */
public class LocationWidgetProvider extends AppWidgetProvider {

    private static final int REQ_CODE = 0;

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            PermissionEverywhere.getPermission(context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_CODE,
                    "Please, give permissions!",
                    "This app needs a location permission",
                    R.mipmap.ic_launcher)
                    .enqueue(new PermissionResultCallback() {
                        @Override
                        public void onComplete(PermissionResponse permissionResponse) {
                            Toast.makeText(context, "Permission is Granted " +
                                    permissionResponse.isGranted(), Toast.LENGTH_SHORT)
                                    .show();
                            startWidgetService(context);
                        }
                    });
        } else {
            startWidgetService(context);
        }
    }

    private void startWidgetService(Context context) {
        context.startService(new Intent(context, GPSWidgetService.class));
    }

    public static class GPSWidgetService extends Service {

        private LocationManager manager = null;

        private LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                AppLog.logString("Service.onLocationChanged()");

                updateCoordinates(location.getLatitude(), location.getLongitude());

                stopSelf();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();

            AppLog.logString("Service.onCreate()");

            manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }

        @Override
        public void onDestroy() {
            stopListening();

            AppLog.logString("Service.onDestroy()");

            super.onDestroy();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            waitForGPSCoordinates();

            AppLog.logString("Service.onStartCommand()");

            return super.onStartCommand(intent, flags, startId);
        }

        private void waitForGPSCoordinates() {
            startListening();
        }

        private void startListening() {
            AppLog.logString("Service.startListening()");

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            final Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);

            final String bestProvider = manager.getBestProvider(criteria, true);

            if (bestProvider != null && bestProvider.length() > 0) {
                manager.requestLocationUpdates(bestProvider, 500, 10, listener);
            } else {
                final List<String> providers = manager.getProviders(true);

                for (final String provider : providers) {
                    manager.requestLocationUpdates(provider, 500, 10, listener);
                }
            }
        }

        private void stopListening() {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            try {
                if (manager != null && listener != null) {
                    manager.removeUpdates(listener);
                }
                manager = null;
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        private void updateCoordinates(double latitude, double longitude) {
            Geocoder coder = new Geocoder(this);
            List<Address> addresses;
            String info = "";

            AppLog.logString("Service.updateCoordinates()");
            AppLog.logString(info);

            try {
                addresses = coder.getFromLocation(latitude, longitude, 2);

                if (addresses != null && addresses.size() > 0) {
                    int addressCount = addresses.get(0).getMaxAddressLineIndex();

                    if (addressCount != -1) {
                        for (int index = 0; index < addressCount; ++index) {
                            info += addresses.get(0).getAddressLine(index);

                            if (index < addressCount) {
                                info += ", ";
                            }
                        }
                    } else {
                        info += addresses.get(0).getFeatureName() + ", " +
                                addresses.get(0).getSubAdminArea() + ", " +
                                addresses.get(0).getAdminArea();
                    }

                    AppLog.logString(addresses.get(0).toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (info.length() <= 0) {
                info = "lat " + latitude + ", lon " + longitude;
            } else {
                info += ("\n" + "(lat " + latitude + ", lon " + longitude + ")");
            }

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.gpswidget);

            views.setTextViewText(R.id.txtInfo, info);

            ComponentName thisWidget =
                    new ComponentName(GPSWidgetService.this, LocationWidgetProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
            manager.updateAppWidget(thisWidget, views);
        }
    }
}
