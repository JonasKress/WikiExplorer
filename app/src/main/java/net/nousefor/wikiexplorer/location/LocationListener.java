package net.nousefor.wikiexplorer.location;

import android.content.Context;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationListener {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private long interval;


    public LocationListener(Context context, LocationCallback locationCallback, long interval) {

        this.interval = interval;
        startLocationUpdates(context, locationCallback);
    }

    private void startLocationUpdates(Context context, LocationCallback locationCallback) {
        this.locationCallback = locationCallback;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(interval);
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    public void stop() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
