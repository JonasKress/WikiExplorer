package net.nousefor.wikiexplorer.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import net.nousefor.wikiexplorer.R;
import net.nousefor.wikiexplorer.api.SparqlQuery;
import net.nousefor.wikiexplorer.location.LocationListener;
import net.nousefor.wikiexplorer.model.Item;
import net.nousefor.wikiexplorer.notification.ItemNotificationsGeneratorThread;
import net.nousefor.wikiexplorer.notification.Notifications;

import java.util.List;

public class BackgroundService extends Service {

    static BackgroundService instance = null;

    public static final String EXTRA_SPARQL = "SPARQL";
    LocationListener locationListener;
    ItemNotificationsGeneratorThread itemNotificationsGeneratorThread;
    Notifications notifications;
    String sparql;

    boolean isDestroyed = false;
    int maximumNotifications = 0;
    int locationUpdateInterval = 5;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setSparql(String sparql) {
        this.sparql = sparql;
    }


    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null || sparql == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                if (isDestroyed)
                    break;

                Log.d("main", "Lat: " + location.getLatitude() + "Long: " + location.getLongitude());
                SparqlQuery query = new SparqlQuery(getString(R.string.SPARQLEndpoint));
                List<Item> resultSet = query.execute(sparql, location);

                if (isDestroyed)
                    break;

                if (maximumNotifications > 0 && resultSet.size() > maximumNotifications)
                    resultSet = resultSet.subList(0, maximumNotifications + 1);

                itemNotificationsGeneratorThread.addItems(resultSet);
            }
        }
    };

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;
        if (intent != null)
            sparql = intent.getStringExtra(EXTRA_SPARQL);

        updatePreferences();
        notifications = new Notifications(this);
        locationListener = new LocationListener(BackgroundService.this, locationCallback, locationUpdateInterval * 60 * 1000);
        itemNotificationsGeneratorThread = new ItemNotificationsGeneratorThread(getBaseContext());
        itemNotificationsGeneratorThread.start();

        startForeground(1, notifications.createBackgroundServiceNotification());
        return START_STICKY;
    }

    public void updatePreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        locationUpdateInterval = sharedPreferences.getInt(getString(R.string.notifications_location_update_interval), locationUpdateInterval);
        maximumNotifications = sharedPreferences.getInt(getString(R.string.notifications_amount_max), maximumNotifications);

        if (itemNotificationsGeneratorThread != null)
            itemNotificationsGeneratorThread.updatePreferences();
    }

    @Override
    public void onDestroy() {
        if (locationListener != null)
            locationListener.stop();

        if (itemNotificationsGeneratorThread != null)
            itemNotificationsGeneratorThread.setDestroyed(true);

        isDestroyed = true;
        instance = null;
    }


    public static BackgroundService getInstance() {
        return instance;
    }


}
